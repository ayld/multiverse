package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.api.*;
import org.codehaus.multiverse.api.exceptions.LockOwnedByOtherTransactionException;
import org.codehaus.multiverse.api.exceptions.NoSuchLockException;
import org.codehaus.multiverse.api.exceptions.NoSuchObjectException;
import org.codehaus.multiverse.api.exceptions.WriteConflictError;
import org.codehaus.multiverse.multiversionedheap.Deflated;
import org.codehaus.multiverse.multiversionedheap.HeapSnapshot;
import org.codehaus.multiverse.multiversionedheap.MultiversionedHeap;
import org.codehaus.multiverse.multiversionedheap.MultiversionedHeap.LockNoWaitResult;
import org.codehaus.multiverse.multiversionedheap.standard.DefaultMultiversionedHeap;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm.MultiversionedTransactionImpl;
import org.codehaus.multiverse.multiversionedstm.utils.StmObjectIterator;
import org.codehaus.multiverse.utils.Pair;
import org.codehaus.multiverse.utils.iterators.ResetableIterator;
import org.codehaus.multiverse.utils.latches.CheapLatch;
import org.codehaus.multiverse.utils.latches.Latch;

import static java.lang.String.format;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * {@link Stm} implementation that uses multiversion concurrency control as concurrency control mechanism.
 * The content is stored in a {@link org.codehaus.multiverse.multiversionedheap.MultiversionedHeap}.
 *
 * @author Peter Veentjer.
 */
public final class MultiversionedStm implements Stm<MultiversionedTransactionImpl> {

    private final MultiversionedHeap<Deflated, StmObject> heap;
    private final MultiversionedStmStatistics statistics = new MultiversionedStmStatistics();
    private final AtomicLong transactionIdGenerator = new AtomicLong();

    /**
     * Creates a new MultiversionedStm with a GrowingMultiversionedHeap as heap.
     */
    public MultiversionedStm() {
        this(new DefaultMultiversionedHeap());
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

    @Override
    public MultiversionedTransactionImpl startTransaction() {
        statistics.transactionsStartedCount.incrementAndGet();
        return new MultiversionedTransactionImpl();
    }

    @Override
    public MultiversionedTransactionImpl startRetriedTransaction(MultiversionedTransactionImpl predecessor) throws InterruptedException {
        ensureValidPredecessor(predecessor);
        Latch latch = new CheapLatch();

        heap.listen(predecessor.snapshot, latch, predecessor.getConditionHandles());
        latch.await();
        statistics.transactionRetriedCount.incrementAndGet();
        return startTransaction();
    }

    private void ensureValidPredecessor(MultiversionedTransactionImpl predecessor) {
        if (predecessor == null) throw new NullPointerException();

        if (predecessor.stm != this)
            throw new IllegalArgumentException();
    }

    public MultiversionedTransactionImpl tryStartRetriedTransaction(MultiversionedTransactionImpl x, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        throw new RuntimeException();
    }

    public class MultiversionedTransactionImpl implements MultiversionedTransaction {

        //is volatile so that other threads are also able to read the status.
        private volatile TransactionStatus status = TransactionStatus.active;

        private final HeapSnapshot snapshot;

        private final Map<Long, StmObject> attachedObjects = new TreeMap<Long, StmObject>();

        private final Map<Long, UnloadedHolderImpl> holderMap = new TreeMap<Long, UnloadedHolderImpl>();

        private int writeCount = 0;
        private final MultiversionedStm stm;
        private final TransactionId transactionId;

        public MultiversionedTransactionImpl() {
            this.snapshot = heap.getActiveSnapshot();
            this.stm = MultiversionedStm.this;
            this.transactionId = new TransactionId("Transaction-" + transactionIdGenerator.getAndIncrement());
        }

        @Override
        public TransactionId getId() {
            return transactionId;
        }

        /**
         * Returns the number of writes done by this transaction. Only when the transaction has committed,
         * this value is set. If the state is any other than committed, an undefined value is returned.
         *
         * @return the number of writes done by this transaction.
         */
        public int getWriteCount() {
            return writeCount;
        }

        /**
         * Returns the number of objects that are hydrated within this transaction. Since immutable StmObjects
         * don't need hydration (the instance is contained in the DehydratedStmObject} they will part of the
         * returned count.
         * <p/>
         * The complexity is lineair to the number of holders (all holders need to be looked at).
         *
         * @return the number of hydrated objects within this transaction.
         */
        public int getHydratedObjectCount() {
            int result = 0;
            for (UnloadedHolderImpl holder : holderMap.values()) {
                if (holder.ref != null)
                    result++;
            }

            return result;
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

        @Override
        public TransactionStatus getStatus() {
            return status;
        }

        /**
         * Returns an array containing all handles that have been read by this Transaction. The returned
         * value will never be null.
         *
         * @return an array containing all handles that have been read by this Transaction.
         */
        private long[] getConditionHandles() {
            long[] result = new long[holderMap.size()];
            int index = 0;
            for (long handle : holderMap.keySet()) {
                result[index] = handle;
                index++;
            }
            return result;
        }

        @Override
        public StmObject read(long handle) {
            assertTransactionIsActive();

            if (handle == 0)
                return null;

            UnloadedHolderImpl holder = readHolder(handle);
            return holder.getAndLoadIfNeeded();
        }

        @Override
        public Object readAndLockOrFail(long handle, LockMode lockMode) {
            throw new UnsupportedOperationException("not implemented yet");
        }

        @Override
        public Object readAndLockOrBlock(long handle, LockMode lockMode) {
            throw new UnsupportedOperationException("not implemented yet");
        }

        @Override
        public <S extends StmObject> UnloadedHolderImpl readHolder(long handle) {
            assertTransactionIsActive();

            if (handle == 0)
                return null;

            UnloadedHolderImpl holder = holderMap.get(handle);
            if (holder == null) {
                //so the object doesn't exist in the current transaction, lets look in the heap if there is
                //a dehydrated version.
                DehydratedStmObject dehydratedObject = (DehydratedStmObject) snapshot.read(handle);

                //if dehydratedObject doesn't exist in the heap, the object that is used is not valid.
                if (dehydratedObject == null)
                    throw new NoSuchObjectException(handle, getVersion());

                holder = new UnloadedHolderImpl(dehydratedObject);
                holderMap.put(handle, holder);
            }
            return holder;
        }

        @Override
        public long attachAsRoot(Object root) {
            assertStmObjectInstance(root);
            assertTransactionIsActive();

            StmObject stmRoot = (StmObject) root;
            long handle = stmRoot.___getHandle();

            //if the object already is attached as root, this call can be ignored.            
            if (attachedObjects.containsKey(handle))
                return handle;

            //todo: if the holder already contains an different reference for the same stm entity check

            attachedObjects.put(handle, stmRoot);

            return handle;
        }

        @Override
        public PessimisticLock getPessimisticLock(Object object) {
            if (object == null) throw new NullPointerException();

            assertTransactionIsActive();
            assertStmObjectInstance(object);

            StmObject stmObject = (StmObject) object;
            long handle = stmObject.___getHandle();
            if (heap.getActiveSnapshot().read(handle) == null)
                throw new NoSuchLockException(format("No lock found in stm for object with handle %s ", handle));

            return new PessimisticLockImpl(stmObject);
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

        /**
         * Makes sure that the current transaction still is active.
         */
        private void assertTransactionIsActive() {
            switch (status) {
                case active:
                    break;
                case aborted:
                    throw new IllegalStateException("The transaction should be active, but already is aborted.");
                case committed:
                    throw new IllegalStateException("The transaction should be active, but already is committed");
                default:
                    throw new RuntimeException();
            }
        }

        @Override
        public void commit() {
            switch (status) {
                case active:
                    try {
                        commitChanges();
                        status = TransactionStatus.committed;
                    } catch (WriteConflictError ex) {
                        abort();
                        throw ex;
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

        private CommitNode createCommitChain() {
            TreeMap<Long, StmObject> s = new TreeMap<Long, StmObject>();
            s.putAll(attachedObjects);

            for (UnloadedHolderImpl holder : holderMap.values()) {
                if (holder.ref != null) {
                    s.put(holder.dehydratedObject.___getHandle(), holder.ref);
                }
            }

            CommitNode head = null;
            for (StmObjectIterator it = new StmObjectIterator(s.values().iterator()); it.hasNext();) {
                StmObject obj = it.next();
                if (obj.___isDirtyIgnoringStmMembers()) {
                    head = new CommitNode(obj, head);
                }
            }

            return head;
        }

        private void commitChanges() {
            CommitNode head = createCommitChain();

            MultiversionedHeap.CommitResult result = heap.commit(snapshot, new CommitIterator(head));

            if (result.isSuccess()) {
                statistics.transactionsCommitedCount.incrementAndGet();

                if (result.getWriteCount() == 0)
                    statistics.transactionsReadonlyCount.incrementAndGet();
                else
                    writeCount = result.getWriteCount();
            } else {
                statistics.transactionsConflictedCount.incrementAndGet();
                throw WriteConflictError.INSTANCE;
            }
        }

        @Override
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

        private class PessimisticLockImpl implements PessimisticLock {
            private final StmObject object;

            private PessimisticLockImpl(StmObject object) {
                assert object != null;
                this.object = object;
            }

            @Override
            public Pair<TransactionId, LockMode> getLockInfo() {
                assertTransactionIsActive();

                return heap.getActiveSnapshot().readLockInfo(object.___getHandle());
            }

            @Override
            public boolean isFree() {
                assertTransactionIsActive();

                throw new UnsupportedOperationException("not implemented yet");
            }

            @Override
            public void acquireSharedNoWait() {
                assertTransactionIsActive();

                LockNoWaitResult result = heap.lockNoWait(transactionId, LockMode.shared, object.___getHandle());
                if (!result.isSuccess())
                    throw new LockOwnedByOtherTransactionException();
            }

            @Override
            public void acquireExclusiveNoWait() {
                assertTransactionIsActive();

                LockNoWaitResult result = heap.lockNoWait(transactionId, LockMode.exclusive, object.___getHandle());
                if (!result.isSuccess())
                    throw new LockOwnedByOtherTransactionException();
            }

            @Override
            public void release() {
                assertTransactionIsActive();

                LockNoWaitResult result = heap.lockNoWait(transactionId, LockMode.free, object.___getHandle());
                if (!result.isSuccess()) {
                    //todo
                    throw new RuntimeException("error needs to be handled");
                }
            }

            @Override
            public String toString() {
                return format("PessimisticLock(handle=%s)", object.___getHandle());
            }
        }

        private class UnloadedHolderImpl implements UnloadedHolder {
            private final DehydratedStmObject dehydratedObject;
            private StmObject ref;

            private UnloadedHolderImpl(DehydratedStmObject dehydratedObject) {
                this.dehydratedObject = dehydratedObject;
            }

            @Override
            public long getHandle() {
                return dehydratedObject.___getHandle();
            }

            @Override
            public StmObject getAndLoadIfNeeded() {
                if (ref == null) {
                    //dehydrated was found in the heap, lets ___inflate so we get a stmObject instance
                    try {
                        ref = (StmObject) dehydratedObject.___inflate(MultiversionedTransactionImpl.this);
                    } catch (Exception e) {
                        //todo: improve message, version also can be included
                        String msg = format("Failed to dehydrate %s instance with object %s", dehydratedObject, dehydratedObject.___getHandle());
                        throw new RuntimeException(msg, e);
                    }
                }
                return ref;
            }
        }

    }

    private static class CommitNode {
        final StmObject ref;
        final CommitNode parent;

        private CommitNode(StmObject ref, CommitNode parent) {
            this.ref = ref;
            this.parent = parent;
        }
    }

    /**
     * This iterator traverses over all stmobjects that need to be committed to the heap.
     */
    private class CommitIterator implements ResetableIterator<StmObject> {
        final CommitNode head;
        CommitNode current;

        CommitIterator(CommitNode head) {
            this.head = head;
            this.current = head;
        }

        @Override
        public void reset() {
            current = head;
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public StmObject next() {
            if (!hasNext())
                throw new NoSuchElementException();

            StmObject result = current.ref;
            current = current.parent;
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}