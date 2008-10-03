package org.codehaus.stm.multiversionedstm2;

import org.codehaus.stm.IllegalPointerException;
import org.codehaus.stm.transaction.AbortedException;
import org.codehaus.stm.transaction.TransactionStatus;
import org.codehaus.stm.util.CheapLatch;
import org.codehaus.stm.util.Latch;

import static java.lang.String.format;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class MultiversionedStm {

    private final AtomicReference<Snapshot> activeSnapshotReference = new AtomicReference(new Snapshot());
    private final AtomicLong nextFreeAddress = new AtomicLong();

    public long getSnapshotSize() {
        return activeSnapshotReference.get().roots.size();
    }

    public long getActiveVersion() {
        return activeSnapshotReference.get().version;
    }

    public Transaction startTransaction() {
        return new MultiversionedTransaction(activeSnapshotReference.get());
    }

    public Transaction startTransaction(Transaction beforeTransaction) throws InterruptedException {
        if (beforeTransaction == null)
            throw new NullPointerException();

        if (!(beforeTransaction instanceof MultiversionedTransaction))
            throw new IllegalArgumentException();

        Snapshot activeSnapshot = activeSnapshotReference.get();

        MultiversionedTransaction t = (MultiversionedTransaction) beforeTransaction;
        if (t.hydratedCitizens.isEmpty())
            return startTransaction();

        Latch latch = new CheapLatch();
        for (DehydratedCitizen dehydratedCitizen : t.hydratedCitizens.keySet()) {
            long ptr = t.snapshot.invertedRoots.get(dehydratedCitizen);
            DehydratedCitizen activeDehydratedCitizen = activeSnapshot.roots.get(ptr);
            if (activeDehydratedCitizen == null) {
                //the object the thread is waiting for doesn't exist anymore.
                throw new RuntimeException();
            } else if (activeDehydratedCitizen == dehydratedCitizen) {
                //no write has been made yet, so we can add the listener 
                dehydratedCitizen.addListener(latch);

                DehydratedCitizen newDehydratedCitizen = activeSnapshotReference.get().roots.get(ptr);
                if (newDehydratedCitizen == null) {
                    throw new RuntimeException();
                } else {
                    latch.open();
                }

            } else {
                //a write already is made, we can stop adding listeners and start with the transaction
                latch.open();
            }

            if (latch.isOpen())
                break;
        }
        latch.await();
        return startTransaction();
    }

    public class MultiversionedTransaction implements Transaction {
        private TransactionStatus transactionStatus = TransactionStatus.active;

        private final Map<Long, Citizen> freshCitizens = new HashMap<Long, Citizen>();
        private final IdentityHashMap<Citizen, Long> invertedFreshCitizens = new IdentityHashMap<Citizen, Long>();

        private final Map<DehydratedCitizen, Citizen> hydratedCitizens = new HashMap<DehydratedCitizen, Citizen>();
        private final Snapshot snapshot;

        public MultiversionedTransaction(Snapshot snapshot) {
            this.snapshot = snapshot;
        }

        public TransactionStatus getStatus() {
            return transactionStatus;
        }


        public long attachRoot(Object item) {
            ensureActive();

            if (item == null)
                throw new NullPointerException();

            if (!(item instanceof Citizen))
                throw new IllegalArgumentException();

            Citizen citizen = (Citizen) item;
            DehydratedCitizen initialDehydratedCitizen = citizen.getInitial();
            if (initialDehydratedCitizen == null) {
                //it is a fresh citizen.

                Long ptr = invertedFreshCitizens.get(citizen);
                //the citizen was already attached. 
                if (ptr != null)
                    return ptr;

                //the citizen was not attached before.
                ptr = nextFreeAddress.incrementAndGet();
                freshCitizens.put(ptr, citizen);
                invertedFreshCitizens.put(citizen, ptr);
                return ptr;
            } else {
                //it is not a fresh citizen.

                Long ptr = snapshot.invertedRoots.get(initialDehydratedCitizen);
                if (ptr == null) {
                    //todo
                    throw new RuntimeException();
                }
                return ptr;
            }
        }

        public Citizen hydrate(DehydratedCitizen dehydratedCitizen) {
            ensureActive();

            if (dehydratedCitizen == null)
                return null;

            Citizen citizen = hydratedCitizens.get(dehydratedCitizen);
            if (citizen != null)
                return citizen;

            citizen = dehydratedCitizen.hydrate(this);
            hydratedCitizens.put(dehydratedCitizen, citizen);
            return citizen;
        }

        public Citizen readRoot(long ptr) {
            ensureActive();

            Citizen citizen = freshCitizens.get(ptr);
            if (citizen != null)
                return citizen;

            DehydratedCitizen dehydratedCitizen = snapshot.roots.get(ptr);
            if (dehydratedCitizen == null)
                throw new IllegalPointerException(ptr);

            return hydrate(dehydratedCitizen);
        }

        public void deleteRoot(long ptr) {
            ensureActive();
            throw new RuntimeException();
        }

        private void ensureActive() {
            if (transactionStatus != TransactionStatus.active)
                throw new IllegalStateException();
        }

        public void abort() {
            switch (transactionStatus) {
                case active:
                    transactionStatus = TransactionStatus.aborted;
                    break;
                case committed:
                    throw new IllegalStateException("Can't abort an already committed transaction");
                case aborted:
                    //if the transaction already is aborted, this abort can be ignored.
                    break;
                default:
                    throw new IllegalStateException(format("Unhandled state %s", transactionStatus));
            }
        }

        public void commit() {
            switch (transactionStatus) {
                case active:
                    Snapshot beforeCommitSnapshot;
                    Snapshot afterCommitSnapshot;
                    do {
                        beforeCommitSnapshot = activeSnapshotReference.get();
                        afterCommitSnapshot = createSnapshot(beforeCommitSnapshot);
                        if (afterCommitSnapshot == null) {
                            abort();
                            throw new AbortedException();
                        }
                    } while (!activeSnapshotReference.compareAndSet(beforeCommitSnapshot, afterCommitSnapshot));

                    transactionStatus = TransactionStatus.committed;

                    if (hydratedCitizensNeedingNotification != null) {
                        for (DehydratedCitizen dehydratedCitizen : hydratedCitizensNeedingNotification)
                            dehydratedCitizen.notifyListeners();
                    }
                    break;
                case committed:
                    //if the transaction already is committed, this commit can be ignored.
                    break;
                case aborted:
                    throw new IllegalStateException("Can't commit an already aborted transaction");
                default:
                    throw new IllegalStateException(format("Unhandled state %s", transactionStatus));
            }
        }

        private List<DehydratedCitizen> hydratedCitizensNeedingNotification;

        private Snapshot createSnapshot(Snapshot activeSnapshot) {
            if (hydratedCitizens.isEmpty() && freshCitizens.isEmpty()) {
                //the transaction has not been used, so the orignal snapshot can be returned.
                return snapshot;
            }

            long newversion = activeSnapshot.version + 1;
            boolean hasWrites = false;

            Map<Long, DehydratedCitizen> newRoots = new HashMap<Long, DehydratedCitizen>();                        
            newRoots.putAll(activeSnapshot.roots);

            //add the fresh citizens
            for (Map.Entry<Long, Citizen> entry : freshCitizens.entrySet()) {
                long ptr = entry.getKey();
                Citizen citizen = entry.getValue();
                DehydratedCitizen dehydratedCitizen = citizen.dehydrate();                                                
                newRoots.put(ptr, dehydratedCitizen);
                hasWrites = true;
            }

            for (Map.Entry<DehydratedCitizen, Citizen> entry : hydratedCitizens.entrySet()) {
                Citizen citizen = entry.getValue();
                DehydratedCitizen oldDehydratedCitizen = entry.getKey();
                long ptr = snapshot.invertedRoots.get(oldDehydratedCitizen);
                DehydratedCitizen newDehydratedCitizen = citizen.dehydrate();
                if (newDehydratedCitizen != oldDehydratedCitizen) {
                    newRoots.put(ptr, newDehydratedCitizen);
                    hasWrites = true;

                    if (hydratedCitizensNeedingNotification == null)
                        hydratedCitizensNeedingNotification = new LinkedList<DehydratedCitizen>();
                    hydratedCitizensNeedingNotification.add(oldDehydratedCitizen);
                }
            }

            return hasWrites ? new Snapshot(newversion, newRoots) : snapshot;
        }
    }
}
