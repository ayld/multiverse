package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.Stm;
import org.codehaus.multiverse.multiversionedstm.growingheap.GrowingHeap;
import org.codehaus.multiverse.transaction.*;
import org.codehaus.multiverse.util.iterators.ArrayIterator;
import org.codehaus.multiverse.util.latches.Latch;
import org.codehaus.multiverse.util.latches.CheapLatch;
import static org.codehaus.multiverse.util.PtrUtils.assertNotNull;
import org.codehaus.multiverse.util.iterators.ResetableIterator;

import static java.lang.String.format;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

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

    private final Heap heap;

    public MultiversionedStm() {
        this(new GrowingHeap());
    }

    public MultiversionedStm(GrowingHeap heap) {
        if (heap == null) throw new NullPointerException();
        this.heap = heap;
    }

    public Heap getMemory() {
        return heap;
    }

    public long getActiveVersion() {
        return heap.getSnapshot().getVersion();
    }

    /**
     * Returns the current number of active transactions (so are started, but have not committed, or rolled back). The value
     * is a rough estimation. The returned value will always be larger or equal to zero.
     *
     * @return
     */
    public long getActiveCount() {
        //since no locking is done, it could be that content are read from different points in time in the stm.
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
        Latch latch = new CheapLatch();
        heap.listen(latch, transaction.getReadHandles(), transaction.getVersion());
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
        //sb.append("stm.heap.cellcount: ").append(heap.getCellCount()).append("\n");
        //sb.append("stm.heap.versioncount: ").append(heap.getVersionCount()).append("\n");
        //sb.append("stm.heap.writecount: ").append(heap.getWriteCount()).append("\n");
        //sb.append("stm.heap.readcount: ").append(heap.getReadCount()).append("\n");
        return sb.toString();
    }

    public class MultiversionedTransaction implements Transaction {

        private volatile TransactionStatus status = TransactionStatus.active;
        private final HeapSnapshot snapshot;
        private final Map<Long, StmObject> newlybornObjects = new HashMap<Long, StmObject>();
        private final Map<Long, StmObject> dehydratedObjects = new Hashtable<Long, StmObject>();
        private final Set<Long> deletedCitizens = new HashSet<Long>();

        private long numberOfWrites = 0;

        public MultiversionedTransaction() {
            snapshot = heap.getSnapshot();
            startedCount.incrementAndGet();
        }

        public long getNumberOfWrites() {
            return numberOfWrites;
        }

        public long getVersion() {
            return snapshot.getVersion();
        }

        public TransactionStatus getStatus() {
            return status;
        }

        /**
         * Returns an array containing all handles that have been read by this Transaction. The returned
         * value will never be null.
         *
         * @return an array containing all handles that have been read by this Transaction.
         */
        public long[] getReadHandles() {
            long[] result = new long[dehydratedObjects.size()];
            int index = 0;
            for (Long address : dehydratedObjects.keySet()) {
                result[index] = address;
                index++;
            }
            return result;
        }

        public void unmarkAsRoot(long handle) {
            assertNotNull(handle);

            assertTransactionActive();

            throw new RuntimeException();
        }

        public void unmarkAsRoot(Object root) {
            if (root == null)
                throw new NullPointerException();

            if (!(root instanceof StmObject))
                throw new IllegalArgumentException(format("%s is not an instanceof the StmObject interface", root));

            assertTransactionActive();

            StmObject object = (StmObject) root;
            Transaction transaction = object.___getTransaction();
            if (transaction == null)
                throw BadTransactionException.createNoTransaction(root);
            if (transaction != this)
                throw BadTransactionException.createAttachedToDifferentTransaction(root);

            deletedCitizens.add(object.___getHandle());
        }

        public Object read(long handle) {
            //preconditions
            assertNotNull(handle);
            assertTransactionActive();

            //if the object already is loaded in this Transaction, the same object should
            //be returned every time.
            StmObject found = dehydratedObjects.get(handle);
            if (found != null)
                return found;

            //if the object was newly born, this content should be returned.
            found = newlybornObjects.get(handle);
            if (found != null)
                return found;

            //so the object doesn't exist in the current transaction, lets look in the heap
            DehydratedStmObject dehydrated = snapshot.read(handle);
            if (dehydrated == null)
                throw new NoSuchObjectException(handle, getVersion());

            try {
                StmObject citizen = dehydrated.hydrate(this);
                dehydratedObjects.put(handle, citizen);
                return citizen;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create privatized StmObject instance", e);
            }
        }

        public long attachAsRoot(Object root) {
            if (root == null)
                throw new NullPointerException();
            if (!(root instanceof StmObject))
                throw new IllegalArgumentException();
            assertTransactionActive();

            StmObject rootStmObject = (StmObject) root;
            ensureNoAttachConflicts(new ArrayIterator<StmObject>(rootStmObject));
            attachAll(new ArrayIterator<StmObject>(rootStmObject));
            return rootStmObject.___getHandle();
        }

        private void ensureNoAttachConflicts(Iterator<StmObject>... roots) {
            for (Iterator<StmObject> it = new StmObjectIterator(roots); it.hasNext();) {
                StmObject citizen = it.next();
                ensureNoAftachConflict(citizen);
            }
        }

        private void ensureNoAftachConflict(StmObject citizen) {
            Transaction transaction = citizen.___getTransaction();
            if (transaction != this && transaction != null) {
                String msg = format("object %s already is attached to another transaction %s", citizen, this);
                throw new BadTransactionException(msg);
            }
        }

        private void attachAll(Iterator<StmObject> roots) {
            for (Iterator<StmObject> it = new StmObjectIterator(roots); it.hasNext();) {
                StmObject citizen = it.next();
                if (citizen.___getTransaction() == null) {
                    long ptr = heap.createHandle();
                    citizen.___setHandle(ptr);
                    citizen.___onAttach(this);
                    newlybornObjects.put(ptr, citizen);
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

                    if (!commitChanges()) {
                        abort();
                        throw new AbortedException("Transaction is aborted because of a write conflict");
                    }

                    status = TransactionStatus.committed;
                    break;
                case committed:
                    //ignore, transaction already is committed, can't do any harm to commit again
                    break;
                default:
                    throw new IllegalStateException();
            }
        }

        private void attachAll() {
            ensureNoAttachConflicts();
            attachAll(newlybornObjects.values().iterator());
        }

        private void ensureNoAttachConflicts() {
            Iterator<StmObject> dehydratedIterator = dehydratedObjects.values().iterator();
            Iterator<StmObject> freshIterator = newlybornObjects.values().iterator();
            ensureNoAttachConflicts(dehydratedIterator, freshIterator);
        }

        private boolean commitChanges() {
            long commitVersion = heap.write(new CommitIterator());
            if (commitVersion >= 0) {
                committedCount.incrementAndGet();
                return true;
            } else {
                return false;
            }

            //for (; it.hasNext();) {
            //     StmObject citizen = it.next();
            //     if (citizen.___isDirty()) {
            //         DehydratedStmObject dehydrated = citizen.___dehydrate();
            //         writeToHeap(citizen.___getHandle(), true, dehydrated);
            //     }
            // }
            //if (numberOfWrites > 0)
            //    activeVersion.incrementAndGet();
            //else
            //readonlyCount.incrementAndGet();

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

        class CommitIterator implements ResetableIterator<StmObject> {
            private Iterator<StmObject> iterator;
            private StmObject next;

            public void reset() {
                next = null;
                iterator = null;
            }

            public boolean hasNext() {
                if (next != null)
                    return true;

                if (iterator == null) {
                    iterator = new StmObjectIterator(
                            dehydratedObjects.values().iterator(),
                            newlybornObjects.values().iterator());
                }

                while (iterator.hasNext()) {
                    StmObject object = iterator.next();
                    if (object.___isDirty()) {
                        next = object;
                        return true;
                    }

                }

                return false;
            }

            public StmObject next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                StmObject tmp = next;
                next = null;
                return tmp;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }


}

