package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.Stm;
import org.codehaus.multiverse.transaction.AbortedException;
import org.codehaus.multiverse.transaction.Transaction;
import org.codehaus.multiverse.transaction.TransactionStatus;
import org.codehaus.multiverse.util.Latch;
import org.codehaus.multiverse.util.ArrayIterator;
import static org.codehaus.multiverse.util.PtrUtils.checkPtr;

import static java.lang.String.format;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Default {@link org.codehaus.multiverse.multiversionedstm.MultiversionedStm} implementation.
 * <p/>
 * Objecten met final velden zijn niet interessant voor transacties, Dus kan heel wat aan geoptimaliseerd worden.
 * Die hoeven ook niet geinstrumteerd te worden? Daar gaat het een beetje om.
 * <p/>
 * <p/>
 * reminder:
 * pages.. een page is een setje met cellen (en een cell kan gezien worden als een database row.. )
 * <p/>
 * Doordat je nu een array gebruikt voor de content van een object, zou je deze array bv ook kunnen uitbreiden
 * met een lock. Ieder record heeft dan net zoals bij MVCC databases automatisch al een lock en die hoeven niet
 * gemanaged to worden door een centrale lock manager (zoals SQL Server 2000). Naarmate deze lock manager meer locks
 * moet gaan mananagen, gaat het locks upgraden record->page->table en hiermee verhinder concurrency. De lock
 * en eventueel extra informatie kan allemaal aan het einde van de array toegevoegd worden.
 * <p/>
 * <p/>
 * je zou bij het privatizen (zonder delete!) van een object uit de stm, meteen alle niet traced velden
 * direct uit het stm lezen. Dit scheelt dat je voor alle niet getraceerde objecten (moeten immutable objecten zijn)
 * iedere keer moet gaan checken. Je gaat de status van dat veld ook niet updaten. Pas bij het wegschrijven
 * kijk je of het veld dirty is. Dit betekend dat ieder traced een extra veld per niet veld dat niet traced is,
 */
public final class MultiversionedStm implements Stm<MultiversionedStm.MultiversionedTransaction> {

    //statistics.
    private final AtomicLong startedCount = new AtomicLong();
    private final AtomicLong committedCount = new AtomicLong();
    private final AtomicLong abortedCount = new AtomicLong();
    private final AtomicLong readonlyCount = new AtomicLong();

    private final AtomicLong activeVersion = new AtomicLong();
    private final MultiversionedHeap heap;

    private final Lock commitLock = new ReentrantLock();

    public MultiversionedStm() {
        this(new MultiversionedHeap());
    }

    public MultiversionedStm(MultiversionedHeap heap) {
        if (heap == null) throw new NullPointerException();
        this.heap = heap;
    }

    public MultiversionedHeap getMemory() {
        return heap;
    }

    public long getActiveVersion() {
        return activeVersion.longValue();
    }

    /**
     * Returns the current number of active transactions (so are started, but have not committed, or rolled back). The value
     * is a rough estimation. The returned value will always be larger or equal to zero.
     *
     * @return
     */
    public long getActiveCount() {
        //since no locking is done, it could be that value are read from different points in time in the stm.
        long count = getStartedCount() - (getCommittedCount() + getAbortedCount());
        return count < 0 ? 0 : count;
    }

    /**
     * Returns the number of transactions that have aborted.
     *
     * @return the number of transactions that have started.
     */
    public long getStartedCount() {
        return startedCount.longValue();
    }

    public long getReadonlyCount() {
        return readonlyCount.longValue();
    }

    /**
     * Returns the number of transactions that have committed.
     *
     * @return the number of transactions that have committed.
     */
    public long getCommittedCount() {
        return committedCount.longValue();
    }

    /**
     * Returns the number of transactions that have aborted.
     *
     * @return the number of transactions that have aborted.
     */
    public long getAbortedCount() {
        return abortedCount.longValue();
    }

    public MultiversionedTransaction startTransaction() {
        return new MultiversionedTransaction();
    }

    public MultiversionedTransaction startTransaction(Transaction base) throws InterruptedException {
        if (base == null) throw new NullPointerException();
        if (!(base instanceof MultiversionedTransaction)) throw new IllegalArgumentException();
        MultiversionedTransaction transaction = (MultiversionedTransaction) base;
        Latch latch = heap.listen(transaction.getReadAddresses(), transaction.getVersion());
        latch.await();
        return startTransaction();
    }

    public MultiversionedTransaction tryStartTransaction(Transaction base, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        throw new RuntimeException();
    }

    public String getStatistics() {
        StringBuffer sb = new StringBuffer();
        sb.append("stm.transaction.activecount: ").append(getActiveCount()).append("\n");
        sb.append("stm.transaction.startedcount: ").append(getStartedCount()).append("\n");
        sb.append("stm.transaction.committedcount: ").append(getCommittedCount()).append("\n");
        sb.append("stm.transaction.abortedcount: ").append(getAbortedCount()).append("\n");
        sb.append("stm.transaction.readonlycount: ").append(getReadonlyCount()).append("\n");
        sb.append("stm.heap.cellcount: ").append(heap.getCellCount()).append("\n");
        sb.append("stm.heap.versioncount: ").append(heap.getVersionCount()).append("\n");
        sb.append("stm.heap.writecount: ").append(heap.getWriteCount()).append("\n");
        sb.append("stm.heap.readcount: ").append(heap.getReadCount()).append("\n");
        return sb.toString();
    }

    public class MultiversionedTransaction implements Transaction {

        private volatile TransactionStatus status = TransactionStatus.active;
        private final long startOfTransactionVersion;
        private final Map<Long, Citizen> newlybornCitizens = new HashMap<Long, Citizen>();
        private final IdentityHashMap<Citizen, Long> invertedNewlybornCitizens = new IdentityHashMap<Citizen, Long>();
        private final Map<Long, Citizen> dehydratedCitizens = new Hashtable<Long, Citizen>();
        private long numberOfWrites = 0;

        public MultiversionedTransaction() {
            startOfTransactionVersion = getActiveVersion();
            startedCount.incrementAndGet();
        }

        public long getNumberOfWrites() {
            return numberOfWrites;
        }

        public long getVersion() {
            return startOfTransactionVersion;
        }

        public TransactionStatus getStatus() {
            return status;
        }

        public long[] getReadAddresses() {
            long[] result = new long[dehydratedCitizens.size()];
            int index = 0;
            for (Long address : dehydratedCitizens.keySet()) {
                result[index] = address;
                index++;
            }
            return result;
        }

        public void deleteRoot(long ptr) {
            throw new RuntimeException();
        }

        public Object readRoot(long ptr) {
            //preconditions
            checkPtr(ptr);
            assertTransactionActive();

            //if the object already is loaded in this Transaction, the same object should
            //be returned every time.
            Citizen found = dehydratedCitizens.get(ptr);
            if (found != null)
                return found;

            //if the object was newly born, this value should be returned.
            found = newlybornCitizens.get(ptr);
            if (found != null)
                return found;

            //so the object doesn't exist in the current transaction, lets look in the heap
            Object[] content = heap.read(ptr, startOfTransactionVersion);

            try {
                DehydratedCitizen dehydratedCitizen = (DehydratedCitizen) content[0];
                Citizen citizen = dehydratedCitizen.hydrate(ptr, this);
                dehydratedCitizens.put(ptr, citizen);
                return citizen;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create privatized Citizen instance", e);
            }
        }

        public long attachRoot(Object root) {
            if (root == null)
                throw new NullPointerException();
            if (!(root instanceof Citizen))
                throw new IllegalArgumentException();
            assertTransactionActive();

            Citizen rootCitizen = (Citizen) root;
            ensureNoAttachConflicts(new ArrayIterator<Citizen>(rootCitizen));
            attachAll(new ArrayIterator<Citizen>(rootCitizen));
            return rootCitizen.___getPointer();
        }

        private void ensureNoAttachConflicts(Iterator<Citizen>... roots) {
            for (Iterator<Citizen> it = new HydratedCitizenIterator(roots); it.hasNext();) {
                Citizen citizen = it.next();
                Transaction transaction = citizen.___getTransaction();
                if (transaction != this && transaction != null)
                    throw new IllegalStateException(format("object already is attached to another transaction %s", citizen));
            }
        }

        private void attachAll(Iterator<Citizen> roots) {
            for (Iterator<Citizen> it = new HydratedCitizenIterator(roots); it.hasNext();) {
                Citizen citizen = it.next();
                if (citizen.___getTransaction() == null) {
                    long ptr = heap.createHandle();
                    citizen.___setPointer(ptr);
                    citizen.___onAttach(this);
                    newlybornCitizens.put(ptr, citizen);
                    invertedNewlybornCitizens.put(citizen, ptr);
                }
            }
        }

        private void assertTransactionActive() {
            if (status != TransactionStatus.active)
                throw new IllegalStateException();
        }

        public void commit() {
            switch (status) {
                case active:
                    attachAll();
                    commitLock.lock();
                    try {
                        if (hasWriteConflicts()) {
                            abort();
                            throw new AbortedException();
                        }
                        updateStm();
                    } finally {
                        commitLock.unlock();
                    }


                    status = TransactionStatus.committed;
                    System.out.println(getStatistics());
                    break;
                case committed:
                    //ignore, transaction already is committed, can't do any harm
                    break;
                default:
                    throw new IllegalStateException();
            }
        }

        private void attachAll() {
            ensureNoAttachConflicts();
            attachAll(newlybornCitizens.values().iterator());
        }

        private void ensureNoAttachConflicts() {
            Iterator<Citizen> dehydratedIterator = dehydratedCitizens.values().iterator();
            Iterator<Citizen> freshIterator = newlybornCitizens.values().iterator();
            ensureNoAttachConflicts(dehydratedIterator, freshIterator);
        }

        private boolean hasWriteConflicts() {
            for (Citizen citizen : dehydratedCitizens.values()) {
                if (citizen.___isDirty() && hasConflictingWrite(citizen.___getPointer())) {
                    return true;
                }
            }

            return false;
        }

        private void updateStm() {
            updateHeap();

            if (numberOfWrites > 0)
                activeVersion.incrementAndGet();
            else
                readonlyCount.incrementAndGet();

            committedCount.incrementAndGet();
        }

        private void updateHeap() {
            Iterator<Citizen> dehydratedIterator = dehydratedCitizens.values().iterator();
            Iterator<Citizen> freshIterator = newlybornCitizens.values().iterator();
            Iterator<Citizen> it = new HydratedCitizenIterator(dehydratedIterator, freshIterator);
            for (; it.hasNext();) {
                Citizen citizen = it.next();
                if (citizen.___isDirty()) {
                    DehydratedCitizen dehydratedCitizen = citizen.___dehydrate();
                    write(citizen.___getPointer(), new Object[]{dehydratedCitizen});
                }
            }
        }


        /**
         * Checks if this Transaction conflicts with a write.
         *
         * @param ptr the address to check
         * @return true if there is a conflict, false otherwise.
         */
        private boolean hasConflictingWrite(long ptr) {
            long actualVersion = heap.getActualVersion(ptr);
            return startOfTransactionVersion + 1 <= actualVersion;
        }

        private void write(long ptr, Object[] content) {
            numberOfWrites++;
            heap.write(ptr, startOfTransactionVersion + 1, content);
        }

        public void abort() {
            switch (status) {
                case active:
                    status = TransactionStatus.aborted;
                    abortedCount.incrementAndGet();
                    break;
                case committed:
                    throw new IllegalStateException();
                case aborted:
                    //ignore, transaction already is aborted.
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
    }
}

