package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.core.*;
import org.codehaus.multiverse.multiversionedstm.growingheap.GrowingMultiversionedHeap;
import org.codehaus.multiverse.multiversionedstm.utils.StmObjectIterator;
import static org.codehaus.multiverse.util.HandleUtils.assertNotNull;
import org.codehaus.multiverse.util.iterators.ArrayIterator;
import org.codehaus.multiverse.util.iterators.CollectionIterator;
import org.codehaus.multiverse.util.iterators.ResetableIterator;
import org.codehaus.multiverse.util.latches.CheapLatch;
import org.codehaus.multiverse.util.latches.Latch;

import static java.lang.String.format;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * {@link Stm} implementation that uses multiversion concurrency control as concurrency control mechanism.
 * The content is stored in a {@link MultiversionedHeap}.
 *
 * @author Peter Veentjer.
 */
public final class MultiversionedStm implements Stm<MultiversionedStm.MultiversionedTransaction> {

    private final MultiversionedHeap heap;
    private final MultiversionedStmStatistics statistics = new MultiversionedStmStatistics();

    /**
     * Creates a new MultiversionedStm with a GrowingMultiversionedHeap as heap.
     */
    public MultiversionedStm() {
        this(new GrowingMultiversionedHeap());
    }

    /**
     * Creates a MultiversionedStm with the given heap.
     *
     * @param heap the MultiversionedHeap used
     * @throws NullPointerException if heap is null.
     */
    public MultiversionedStm(MultiversionedHeap heap) {
        if (heap == null) throw new NullPointerException();
        this.heap = heap;
    }

    /**
     * Returns the MultiversionedStmStatistics this MultiversionedStm uses to store statistics.
     *
     * @return the MultiversionedStmStatistics  this MultiversionedStm  uses to store statistics.
     */
    public MultiversionedStmStatistics getStatistics() {
        return statistics;
    }

    /**
     * Returns the Heap this MultiversionedStm uses.
     *
     * @return the Heap this MultiversionedStm uses.
     */
    public MultiversionedHeap getHeap() {
        return heap;
    }

    /**
     * Returns the current version of the heap. Value could be stale as it is received.
     *
     * @return the current version of the heap.
     */
    public long getCurrentVersion() {
        return heap.getActiveSnapshot().getVersion();
    }

    public MultiversionedTransaction startTransaction() {
        statistics.transactionsStartedCount.incrementAndGet();
        return new MultiversionedTransaction();
    }

    public MultiversionedTransaction startRetriedTransaction(MultiversionedStm.MultiversionedTransaction predecessor) throws InterruptedException {
        if (predecessor == null) throw new NullPointerException();

        Latch latch = new CheapLatch();
        heap.listen(latch, predecessor.getReadHandles(), predecessor.getVersion());
        latch.await();
        statistics.transactionRetriedCount.incrementAndGet();
        return startTransaction();
    }

    public MultiversionedTransaction tryStartRetriedTransaction(MultiversionedStm.MultiversionedTransaction x, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        throw new RuntimeException();
    }

    public class MultiversionedTransaction implements Transaction {

        private volatile TransactionStatus status = TransactionStatus.active;
        private final MultiversionedHeapSnapshot snapshot;
        private final Map<Long, StmObject> newlybornObjects = new HashMap<Long, StmObject>();
        private final Map<Long, StmObject> dehydratedObjects = new Hashtable<Long, StmObject>();
        private final Set<Long> detachedHandles = new HashSet<Long>();

        private long writeCount = 0;

        public MultiversionedTransaction() {
            snapshot = heap.getActiveSnapshot();
        }

        /**
         * Returns the number of writes done by this transaction. Only when the transaction has committed, this value
         * is set. Before it returns 0.
         *
         * @return the number of writes done by this transaction.
         */
        public long getWriteCount() {
            return writeCount;
        }

        /**
         * Returns the number of reads that have been done on the heap.
         *
         * @return the number of reads that have been done on the heap.
         */
        public long getReadFromHeapCount() {
            return dehydratedObjects.size();
        }

        /**
         * Returns the version of the transaction. This value is equal to the version of the heap when
         * the transaction began.
         *
         * @return the version of the transacton.
         */
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
            detachedHandles.add(object.___getHandle());
        }

        private void assertNoTransactionProblemsForUnmarkAsRoot(Object root, StmObject object) {
            Transaction transaction = object.___getTransaction();
            if (transaction == null)
                throw BadTransactionException.createNoTransaction(root);
            if (transaction != this)
                throw BadTransactionException.createAttachedToDifferentTransaction(root);
        }

        public Object read(long handle) {
            assertTransactionActive();

            if (handle == 0)
                return null;

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
         * @throws org.codehaus.multiverse.core.BadTransactionException
         *          if there is a transaction conflict.
         */
        private void assertNoAftachConflict(StmObject object) {
            Transaction transaction = object.___getTransaction();
            if (transaction != this && transaction != null)
                throw createBadTransactionException(object);
        }

        private BadTransactionException createBadTransactionException(StmObject object) {
            String msg = format("object %s already is attached to another transaction %s", object, this);
            return new BadTransactionException(msg);
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
                attach(stmObject);
            }
        }

        private void attach(StmObject stmObject) {
            Transaction transaction = stmObject.___getTransaction();
            if (transaction == null) {
                long handle = heap.createHandle();
                stmObject.___onAttach(this);
                //todo: could this cause a concurrentmodificationexception?
                newlybornObjects.put(handle, stmObject);
            } else if (transaction != this) {
                throw createBadTransactionException(stmObject);
            }
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
                    try {
                        commitChanges();
                        status = TransactionStatus.committed;
                    } catch (RuntimeException ex) {
                        abort();
                        throw ex;
                    }

                    break;
                case committed:
                    //ignore, transaction already is committed, can't do any harm to commit again
                    break;
                case aborted:
                    throw new IllegalStateException("Can't commit an already aborted transaction");
                default:
                    throw new IllegalStateException();
            }
        }

        private void commitChanges() {
            List<DehydratedStmObject> commitList = new LinkedList<DehydratedStmObject>();
            for (Iterator<DehydratedStmObject> it = new DirtyIterator(); it.hasNext();) {
                commitList.add(it.next());
            }

            MultiversionedHeap.CommitResult result = heap.commit(
                    snapshot.getVersion(),
                    new CollectionIterator<DehydratedStmObject>(commitList));

            //MultiversionedHeap.CommitResult result = heap.commit(
            //                    snapshot.getVersion(),
            //                    new DirtyIterator());


            if (result.isSuccess()) {
                statistics.transactionsCommitedCount.incrementAndGet();

                if (result.getWriteCount() == 0)
                    statistics.transactionsReadonlyCount.incrementAndGet();
                else
                    writeCount = result.getWriteCount();
            } else {
                statistics.transactionsConflictedCount.incrementAndGet();
                throw new WriteConflictException("Transaction is aborted because of a write conflict");
            }
        }

        public void abort() {
            switch (status) {
                case active:
                    status = TransactionStatus.aborted;
                    statistics.transactionsAbortedCount.incrementAndGet();
                    break;
                case committed:
                    throw new IllegalStateException("Can't abort an already committed transaction");
                case aborted:
                    //ignore, transaction already is aborted.
                    break;
                default:
                    throw new IllegalStateException();
            }
        }

        class DirtyIterator implements ResetableIterator<DehydratedStmObject> {
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
                        attach(object);
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

