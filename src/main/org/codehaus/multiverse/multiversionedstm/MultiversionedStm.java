package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.core.*;
import org.codehaus.multiverse.multiversionedstm.growingheap.GrowingMultiversionedHeap;
import org.codehaus.multiverse.multiversionedstm.utils.StmObjectIterator;
import static org.codehaus.multiverse.util.HandleUtils.assertNotNull;
import org.codehaus.multiverse.util.iterators.ArrayIterator;
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
        ensureValidPredecessor(predecessor);
        ensureProgressPossible(predecessor);
        Latch latch = new CheapLatch();

        heap.listen(latch, predecessor.getConditionHandles(), predecessor.getVersion());
        latch.await();
        statistics.transactionRetriedCount.incrementAndGet();
        return startTransaction();
    }

    private void ensureValidPredecessor(MultiversionedTransaction predecessor) {
        if (predecessor == null) throw new NullPointerException();

        if (predecessor.stm != this)
            throw new IllegalArgumentException();
    }

    private void ensureProgressPossible(MultiversionedTransaction predecessor) {
        if (predecessor.getConditionHandles().length == 0)
            throw new NoProgressPossibleException();
    }

    public MultiversionedTransaction tryStartRetriedTransaction(MultiversionedStm.MultiversionedTransaction x, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        throw new RuntimeException();
    }

    public class MultiversionedTransaction implements Transaction {

        //is volatile so that other threads are also able to read the status.
        private volatile TransactionStatus status = TransactionStatus.active;

        private final MultiversionedHeapSnapshot snapshot;

        private final Map<Long, StmObject> attachedObjects = new HashMap<Long, StmObject>();

        private final Map<Long, StmObject> hydratedObjects = new Hashtable<Long, StmObject>();

        private final Set<Long> conditionHandles = new HashSet<Long>();

        private long writeCount = 0;
        private final MultiversionedStm stm;

        public MultiversionedTransaction() {
            snapshot = heap.getActiveSnapshot();
            stm = MultiversionedStm.this;
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
         * Returns the number of objects that are hydrated within this transaction. Since immutable StmObjects
         * don't need hydration (the instance is contained in the DehydratedStmObject} they will part of the
         * returned count.
         *
         * @return the number of hydrated objects within this transaction.
         */
        public long getHydratedObjectCount() {
            return hydratedObjects.size();
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
        public long[] getConditionHandles() {
            long[] result = new long[hydratedObjects.size()];
            int index = 0;
            for (long handle : hydratedObjects.keySet()) {
                result[index] = handle;
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
            //detachedHandles.add(object.___getHandle());
            //todo
            throw new RuntimeException();
        }

        private void assertNoTransactionProblemsForUnmarkAsRoot(Object root, StmObject object) {
            if (object.___isImmutable())
                return;

            Transaction transaction = object.___getTransaction();
            if (transaction == null)
                throw BadTransactionException.createNoTransaction(root);
            if (transaction != this)
                throw BadTransactionException.createAttachedToDifferentTransaction(root);
        }

        public StmObject read(long handle) {
            assertTransactionActive();

            if (handle == 0)
                return null;

            //if the object already is loaded in this Transaction, the same object should
            //be returned every time. This is the same behavior Hibernate provides for example.
            StmObject previouslyHydratedObject = hydratedObjects.get(handle);
            if (previouslyHydratedObject != null)
                return previouslyHydratedObject;

            //if the object is fresh, this object should be returned.
            StmObject freshObject = attachedObjects.get(handle);
            if (freshObject != null)
                return freshObject;

            //so the object doesn't exist in the current transaction, lets look in the heap if there is
            //a dehydrated version.
            DehydratedStmObject dehydratedObject = snapshot.read(handle);
            //if dehydrated doesn't exist in the heap, the handle that is used is not valid.
            if (dehydratedObject == null)
                throw new NoSuchObjectException(handle, getVersion());

            //dehydrated was found in the heap, lets hydrate so we get a stmObject instance
            StmObject hydratedObject;
            try {
                hydratedObject = dehydratedObject.hydrate(this);
            } catch (Exception e) {
                String msg = format("Failed to dehydrate %s instance with handle %s", dehydratedObject, handle);
                throw new RuntimeException(msg, e);
            }

            if (!hydratedObject.___isImmutable()) {
                //the stmObject instance needs to be registered, so that the next request for the same
                //handle returns the same stmObject. We are not registrating immutableStmObjects to prevent overhead.
                //So a read of a immutable object, always go to the heap.
                hydratedObjects.put(handle, hydratedObject);
            }

            //lets return the instance.
            return hydratedObject;
        }

        public long attachAsRoot(Object root) {
            assertStmObjectInstance(root);
            assertTransactionActive();

            StmObject stmObject = (StmObject) root;

            if (stmObject.___isImmutable()) {
                //if the object already is.. todo: what about version?
                if (snapshot.read(stmObject.___getHandle()) == null) {
                    attachDeep(new ArrayIterator<StmObject>(stmObject));
                }
            } else {
                assertNoAttachConflicts(new ArrayIterator(stmObject));
                attachDeep(new ArrayIterator<StmObject>(stmObject));
            }

            return stmObject.___getHandle();
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
                StmObject stmObject = it.next();
                assertNoShallowAttachConflict(stmObject);
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
        private void assertNoShallowAttachConflict(StmObject object) {
            if (object.___isImmutable())
                return;

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
        private void attachDeep(Iterator<StmObject>... rootIterators) {
            for (Iterator<StmObject> it = new StmObjectIterator(rootIterators); it.hasNext();) {
                StmObject stmObject = it.next();
                attachShallow(stmObject);
            }
        }

        private void attachShallow(StmObject stmObject) {
            if (stmObject.___isImmutable()) {
                long handle = stmObject.___getHandle();

                //if the stmObject has not been persisted before, it should be added to the newlyborns so
                //that it will be persisted.
                if (snapshot.read(handle) == null) {
                    attachedObjects.put(handle, stmObject);
                }
            } else {
                Transaction transaction = stmObject.___getTransaction();
                if (transaction == null) {
                    long handle = heap.createHandle();
                    stmObject.___onAttach(this);
                    //todo: could this cause a concurrentmodificationexception?
                    attachedObjects.put(handle, stmObject);
                } else if (transaction != this) {
                    throw createBadTransactionException(stmObject);
                }
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
            MultiversionedHeap.CommitResult result = heap.commit(
                    snapshot.getVersion(),
                    new CommitIterator());

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
                            hydratedObjects.values().iterator(),
                            attachedObjects.values().iterator());
                }

                while (iterator.hasNext()) {
                    StmObject object = iterator.next();
                    if (object.___isImmutable() || object.___isDirty()) {
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