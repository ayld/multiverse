package org.codehaus.stm.multiversionedstm;

import org.codehaus.stm.Stm;
import org.codehaus.stm.transaction.AbortedException;
import org.codehaus.stm.transaction.Transaction;
import org.codehaus.stm.transaction.TransactionStatus;
import org.codehaus.stm.util.Latch;
import static org.codehaus.stm.util.PtrUtils.checkPtr;

import static java.lang.String.format;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Default {@link org.codehaus.stm.multiversionedstm.MultiversionedStm} implementation.
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
     * Returns the number of active transactions (so are started, but have not committed, or rolled back). The value
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
        MultiversionedTransaction mtransaction = (MultiversionedTransaction) base;
        Latch latch = heap.listen(mtransaction.getReadAddresses(), mtransaction.getVersion());
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
        private final long version;
        private final Map<Long, Citizen> newlybornCitizens = new HashMap<Long, Citizen>();
        private final IdentityHashMap<Citizen, Long> invertedNewlybornCitizens = new IdentityHashMap<Citizen, Long>();
        private final Map<Long, Citizen> dehydratedCitizens = new Hashtable<Long, Citizen>();
        private long numberOfWrites = 0;

        public MultiversionedTransaction() {
            version = getActiveVersion();
            startedCount.incrementAndGet();
        }

        public long getNumberOfWrites() {
            return numberOfWrites;
        }

        public long getVersion() {
            return version;
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

            found = newlybornCitizens.get(ptr);
            if (found != null)
                return found;

            //you always get a not null value. When a non existing value is used, an IllegalPointerException
            // or IllegalVersionException is thrown.
            Object[] content = heap.read(ptr, version);

            try {
                DehydratedCitizen dehydratedCitizen = (DehydratedCitizen) content[0];
                Citizen citizen = dehydratedCitizen.hydrate(ptr, this);
                dehydratedCitizens.put(ptr, citizen);
                return citizen;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create privatized Citizen instance", e);
            }
        }

        public long attachRoot(Object newlyborn) {
            if (newlyborn == null)
                throw new NullPointerException();
            if (!(newlyborn instanceof Citizen))
                throw new IllegalArgumentException();
            assertTransactionActive();

            Citizen newlybornCitizen = (Citizen) newlyborn;
            //if the object already is attached, it should be ignored,
            if (invertedNewlybornCitizens.containsKey(newlybornCitizen))
                return newlybornCitizen.___getPointer();

            if (newlybornCitizen.___getTransaction() == null) {
                long ptr = heap.createHandle();
                newlybornCitizen.___setPointer(ptr);
                newlybornCitizen.___onAttach(this);
                newlybornCitizens.put(ptr, newlybornCitizen);
                invertedNewlybornCitizens.put(newlybornCitizen, ptr);

                for (Iterator<Citizen> citizenIterator = newlybornCitizen.___findNewlyborns(); citizenIterator.hasNext();) {
                    attachRoot(citizenIterator.next());
                }

                return ptr;
            } else {
                throw new IllegalStateException("object already is attached to another transaction");
            }
        }

        private void assertTransactionActive() {
            if (status != TransactionStatus.active)
                throw new IllegalStateException();
        }

        public TransactionStatus getStatus() {
            return status;
        }

        public void commit() {
            switch (status) {
                case active:
                    CommitTask task = new CommitTask(this);
                    if (task.execute()) {
                        committedCount.incrementAndGet();
                        status = TransactionStatus.committed;
                        System.out.println(getStatistics());
                    } else {
                        abort();
                        throw new AbortedException();
                    }
                    break;
                case committed:
                    //ignore
                    break;
                default:
                    throw new IllegalStateException();
            }
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

        public boolean hasConflictingWrite(long ptr) {
            long actualVersion = heap.getActualVersion(ptr);
            return version + 1 <= actualVersion;
        }

        public void write(long ptr, Object[] content) {
            numberOfWrites++;
            heap.write(ptr, version + 1, content);
        }
    }


    class CommitTask {
        final MultiversionedTransaction transaction;
        IdentityHashMap<Citizen, ValidateStatus> validationResults;

        CommitTask(MultiversionedTransaction transaction) {
            this.transaction = transaction;
        }

        /**
         * Returns true if the commit was a success, false if a failure.
         *
         * @return
         */
        boolean execute() {
            commitLock.lock();
            try {
                ValidateStatus validateStatus = validate();
                switch (validateStatus) {
                    case noReadsOrWrites:
                        return true;
                    case onlyReads:
                        readonlyCount.incrementAndGet();
                        return true;
                    case hasNonconflictingWrites:
                        makePermanent();
                        if (transaction.numberOfWrites > 0)
                            activeVersion.incrementAndGet();

                        return true;
                    case hasConflictingWrites:
                        return false;
                    default:
                        throw new RuntimeException(format("unhandled validateStatus %s", validateStatus));
                }
            } finally {
                commitLock.unlock();
            }
        }

        private ValidateStatus validate() {
            //only rematerialized objects need to be checked for conflicting changes
            if (transaction.dehydratedCitizens.isEmpty() && transaction.newlybornCitizens.isEmpty()) {
                return ValidateStatus.noReadsOrWrites;
            }

            validationResults = new IdentityHashMap<Citizen, ValidateStatus>();

            for (Citizen t : transaction.newlybornCitizens.values())
                validationResults.put(t, ValidateStatus.hasNonconflictingWrites);

            boolean writesFound = !transaction.newlybornCitizens.isEmpty();
            for (Map.Entry<Long, Citizen> entry : transaction.dehydratedCitizens.entrySet()) {
                long ptr = entry.getKey();
                Citizen citizen = entry.getValue();
                if (citizen.___isDirty()) {
                    if (transaction.hasConflictingWrite(ptr))
                        return ValidateStatus.hasConflictingWrites;
                    validationResults.put(citizen, ValidateStatus.hasNonconflictingWrites);
                    writesFound = true;
                } else {
                    validationResults.put(citizen, ValidateStatus.onlyReads);
                }
            }

            return writesFound ? ValidateStatus.hasNonconflictingWrites : ValidateStatus.onlyReads;
        }

        private Set<Citizen> findNewlyborns(Citizen citizen) {
            Set<Citizen> result = new HashSet<Citizen>();
            //todo
            return result;
        }


        private Iterator<Citizen> iterateOverNewlyborns() {
            Set<Citizen> result = new HashSet<Citizen>();
            result.addAll(transaction.newlybornCitizens.values());

            for (Citizen citizen : transaction.newlybornCitizens.values())
                result.addAll(findNewlyborns(citizen));

            for (Citizen citizen : transaction.dehydratedCitizens.values())
                result.addAll(findNewlyborns(citizen));

            //todo: not all reachable objects are returned, only the once that are attached directly.
            return result.iterator();
        }

        private void makePermanent() {
            prepareNewlybornsForPlacementInStm();

            for (Citizen obj : transaction.dehydratedCitizens.values()) {
                if (validationResults.get(obj) == ValidateStatus.hasNonconflictingWrites) {
                    Object[] content = new Object[1];
                    DehydratedCitizen dehydratedCitizen = obj.___hydrate();
                    content[0] = dehydratedCitizen;
                    transaction.write(obj.___getPointer(), content);
                }
            }
        }

        private void prepareNewlybornsForPlacementInStm() {
            for (Iterator<Citizen> it = iterateOverNewlyborns(); it.hasNext();) {
                Citizen newlyborn = (Citizen) it.next();
                long ptr = heap.createHandle();
                newlyborn.___setPointer(ptr);
                newlyborn.___onAttach(transaction);
                transaction.dehydratedCitizens.put(ptr, newlyborn);
            }
        }
    }
}

