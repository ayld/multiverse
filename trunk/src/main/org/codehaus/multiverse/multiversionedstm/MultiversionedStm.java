package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.Stm;
import org.codehaus.multiverse.multiversionedstm.growingheap.GrowingHeap;
import org.codehaus.multiverse.transaction.*;
import static org.codehaus.multiverse.util.PtrUtils.assertNotNull;
import org.codehaus.multiverse.util.iterators.ArrayIterator;
import org.codehaus.multiverse.util.iterators.ResetableIterator;
import org.codehaus.multiverse.util.latches.CheapLatch;
import org.codehaus.multiverse.util.latches.Latch;

import static java.lang.String.format;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * {@link Stm} implementation that uses multiversion concurrency control as concurrency control mechanism.
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

    public MultiversionedTransaction startRetriedTransaction(Transaction base) throws InterruptedException {
        if (base == null) throw new NullPointerException();
        if (!(base instanceof MultiversionedTransaction)) throw new IllegalArgumentException();
        MultiversionedTransaction transaction = (MultiversionedTransaction) base;
        Latch latch = new CheapLatch();
        heap.listen(latch, transaction.getReadHandles(), transaction.getVersion());
        latch.await();
        return startTransaction();
    }

    public MultiversionedTransaction tryStartRetriedTransaction(Transaction base, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
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
            assertStmObjectInstance(root);
            assertTransactionActive();

            StmObject object = (StmObject) root;
            assertNoTransactionProblemsForUnmarkAsRoot(root, object);
            deletedCitizens.add(object.___getHandle());
        }

        private void assertNoTransactionProblemsForUnmarkAsRoot(Object root, StmObject object) {
            Transaction transaction = object.___getTransaction();
            if (transaction == null)
                throw BadTransactionException.createNoTransaction(root);
            if (transaction != this)
                throw BadTransactionException.createAttachedToDifferentTransaction(root);
        }

        public Object read(long handle) {
            //preconditions
            assertNotNull(handle);
            assertTransactionActive();

            //if the object already is loaded in this Transaction, the same object should
            //be returned every time. This is the same behavior Hibernate provides for example.
            StmObject found = dehydratedObjects.get(handle);
            if (found != null)
                return found;

            //if the object was newly born, this content should be returned.
            found = newlybornObjects.get(handle);
            if (found != null)
                return found;

            //so the object doesn't exist in the current transaction, lets look up the dehydratedStmObject
            //in the heap.
            DehydratedStmObject dehydrated = snapshot.read(handle);
            //if dehydrated doesn't exist in the heap, the handle that is used, is not valid.
            if (dehydrated == null)
                throw new NoSuchObjectException(handle, getVersion());

            //dehydrated was found in the heap, lets dehydrate so we get a stmObject instance
            StmObject instance;
            try {
                instance = dehydrated.hydrate(this);
            } catch (Exception e) {
                String msg = format("Failed to dehydrate %s instance with handle %s", dehydrated, handle);
                throw new RuntimeException(msg, e);
            }

            //the stmObject instance needs to be registered, so that the next request for the same
            //handle returns the same stmObject.
            dehydratedObjects.put(handle, instance);
            //lets return the instance.
            return instance;
        }

        public long attachAsRoot(Object root) {
            assertStmObjectInstance(root);
            assertTransactionActive();

            StmObject rootInstance = (StmObject) root;
            assertNoAttachConflicts(new ArrayIterator<StmObject>(rootInstance));
            attachAll(new ArrayIterator<StmObject>(rootInstance));
            return rootInstance.___getHandle();
        }

        /**
         * Makes sure that the object is an {@link StmObject} instance.
         *
         * @param object the object to check
         * @throws NullPointerException     if object is null
         * @throws IllegalArgumentException if the object is not an instance of {@link StmObject}.
         */
        private void assertStmObjectInstance(Object object) {
            if (object == null)
                throw new NullPointerException();
            if (!(object instanceof StmObject))
                throw new IllegalArgumentException();
        }

        private void assertNoAttachConflicts(Iterator<StmObject>... roots) {
            for (Iterator<StmObject> it = new StmObjectIterator(roots); it.hasNext();) {
                StmObject citizen = it.next();
                assertNoAftachConflict(citizen);
            }
        }

        /**
         * Makes sure that the stmObject can be attached to this transaction. It can be attached if the transaction
         * inside the StmObject is null, or if the transaction is the same as the current transaction (multiple attach
         * of the same object to the same transaction should be ignored).
         * <p/>
         * Dependencies of the object are not checked, on the object itself.
         *
         * @param object the StmObject to check.
         * @throws BadTransactionException if there is a transaction conflict.
         */
        private void assertNoAftachConflict(StmObject object) {
            Transaction transaction = object.___getTransaction();
            if (transaction != this && transaction != null) {
                throw createBadTransactionException(object);
            }
        }

        private BadTransactionException createBadTransactionException(StmObject object) {
            String msg = format("object %s already is attached to another transaction %s", object, this);
            return new BadTransactionException(msg);
        }

        /**
         * Makes sure that all objects attached to this transaction (directly or indirectly) can be attached to
         * this transaction. This method is usefull if you want to commit, and make sure that there are no problems.
         */
        private void assertNoAttachConflictsForAllReachables() {
            assertNoAttachConflicts(
                    dehydratedObjects.values().iterator(),
                    newlybornObjects.values().iterator());
        }


        /**
         * Attaches all objects that can be reached from the array of root iterators (directly or indirectly)
         * to this Transaction.
         *
         * @param rootIterators an array of iterators containing root objects.
         */
        private void attachAll(Iterator<StmObject>... rootIterators) {
            for (Iterator<StmObject> it = new StmObjectIterator(rootIterators); it.hasNext();) {
                StmObject stmObject = it.next();
                Transaction transaction = stmObject.___getTransaction();
                if (transaction == null) {
                    long ptr = heap.createHandle();
                    stmObject.___setHandle(ptr);
                    stmObject.___onAttach(this);
                    newlybornObjects.put(ptr, stmObject);
                } else if (transaction != this) {
                    throw createBadTransactionException(stmObject);
                }
            }
        }

        /**
         * Attaches all objects that can be reached from this transaction (directly and indirect) to
         * this transaction.
         */
        private void attachAllReachables() {
            attachAll(
                    newlybornObjects.values().iterator(),
                    dehydratedObjects.values().iterator());
        }

        /**
         * Makes sure that the current transaction still is active.
         */
        private void assertTransactionActive() {
            if (status != TransactionStatus.active)
                throw new IllegalStateException();
        }

        public void commit() {
            switch (status) {
                case active:
                    assertNoAttachConflictsForAllReachables();
                    attachAllReachables();

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

        private boolean commitChanges() {
            long commitVersion = heap.write(snapshot.getVersion(), new CommitIterator());
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

        class CommitIterator implements ResetableIterator<DehydratedStmObject> {
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

            public DehydratedStmObject next() {
                if (!hasNext())
                    throw new NoSuchElementException();

                StmObject tmp = next;
                next = null;
                return tmp.___dehydrate();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }
}

