package org.codehaus.multiverse.multiversionedheap.standard;

import org.codehaus.multiverse.api.LockMode;
import org.codehaus.multiverse.api.TransactionId;
import org.codehaus.multiverse.api.exceptions.NoProgressPossibleException;
import org.codehaus.multiverse.api.exceptions.NoSuchObjectException;
import org.codehaus.multiverse.multiversionedheap.Deflatable;
import org.codehaus.multiverse.multiversionedheap.Deflated;
import org.codehaus.multiverse.multiversionedheap.HeapSnapshot;
import org.codehaus.multiverse.multiversionedheap.MultiversionedHeap;
import org.codehaus.multiverse.util.Pair;
import org.codehaus.multiverse.util.iterators.ArrayIterator;
import org.codehaus.multiverse.util.iterators.ResetableIterator;
import org.codehaus.multiverse.util.latches.Latch;

import static java.lang.String.format;
import java.util.Iterator;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Default {@link org.codehaus.multiverse.multiversionedheap.MultiversionedHeap} implementation.
 *
 * @author Peter Veentjer.
 */
public final class DefaultMultiversionedHeap<I extends Deflated, D extends Deflatable>
        implements MultiversionedHeap<I, D> {

    private final AtomicReference<HeapSnapshotImpl> snapshotRef =
            new AtomicReference<HeapSnapshotImpl>(new HeapSnapshotImpl());

    private final DefaultMultiversionedHeapStatistics statistics = new DefaultMultiversionedHeapStatistics();

    public DefaultMultiversionedHeap() {
    }

    public DefaultMultiversionedHeapStatistics getStatistics() {
        return statistics;
    }

    public HeapSnapshot<I> getActiveSnapshot() {
        return snapshotRef.get();
    }

    @Override
    public LockNoWaitResult lockNoWait(TransactionId owner, LockMode lockMode, long handle) {
        assert owner != null && lockMode != null;

        HeapSnapshotImpl newSnapshot;
        boolean success;
        do {
            HeapSnapshotImpl oldSnapshot = snapshotRef.get();
            newSnapshot = oldSnapshot.createNewForSettingPessimisticLock(
                    owner, lockMode, handle);

            //if null is returned, a locking failure happened. So we should
            //not replace the newSnapshot.
            if (newSnapshot == null)
                return LockNoWaitResult.createFailure();

            //if there was no locking change, we are done. No need
            //to repace the snapshot.
            if (newSnapshot == oldSnapshot)
                return LockNoWaitResult.createSuccess(oldSnapshot);

            success = snapshotRef.compareAndSet(oldSnapshot, newSnapshot);
        } while (!success);

        newSnapshot.activate();
        return LockNoWaitResult.createSuccess(newSnapshot);
    }

    @Override
    public void abort(TransactionId transactionId) {
        assert transactionId != null;

        throw new RuntimeException("Not implemented yet");
    }

    /**
     * Commits the changes to the heap. This is a convenience method that allows for a varargs.
     *
     * @param startSnapshot
     * @param changes
     * @return
     */
    public CommitResult commit(HeapSnapshot<I> startSnapshot, D... changes) {
        return commit(startSnapshot, new ArrayIterator<D>(changes));
    }

    @Override
    public CommitResult commit(HeapSnapshot<I> startSnapshot, ResetableIterator<D> changes) {
        assert startSnapshot != null && changes != null;

        //todo: all locks that are required by this transaction, should be released.

        beforeCommit();

        if (!changes.hasNext()) {
            //if there are no changes to createNewForWrite to the heap, the transaction was readonly and we are done.
            statistics.commitReadonlyCount.incrementAndGet();
            return CommitResult.createReadOnly(snapshotRef.get());
        }

        boolean anotherTransactionDidCommit;
        CreateNewSnapshotResult createNewSnapshotResult;
        do {
            HeapSnapshotImpl activeSnapshot = snapshotRef.get();

            createNewSnapshotResult = activeSnapshot.createNewForCommit(changes, startSnapshot);

            //if there was a writeconflict, we are done
            if (!createNewSnapshotResult.success) {
                statistics.commitWriteConflictCount.incrementAndGet();
                return CommitResult.createWriteConflict();
            }

            //lets try to activate the created snapshot.
            anotherTransactionDidCommit = !snapshotRef.compareAndSet(
                    activeSnapshot,
                    createNewSnapshotResult.createdSnapshot);

            if (anotherTransactionDidCommit) {
                //if another transaction also did a commit while we were creating a new snapshot, we
                //have to try again. We need to reset the changes iterator for that.
                statistics.commitNonBlockingStatistics.incFailureCount();
                changes.reset();
            }
        } while (anotherTransactionDidCommit);

        createNewSnapshotResult.createdSnapshot.activate();

        return afterCommit(createNewSnapshotResult);
    }

    private void beforeCommit() {
        statistics.commitTotalCount.incrementAndGet();
        statistics.commitNonBlockingStatistics.incEnterCount();
    }

    private CommitResult afterCommit(CreateNewSnapshotResult createNewSnapshotResult) {
        //yes.. the snapshot was activated, lets update statistics and lets wakeup listeners.
        statistics.commitSuccessCount.incrementAndGet();
        statistics.committedStoreCount.addAndGet(createNewSnapshotResult.writeCount);
        return CommitResult.createSuccess(createNewSnapshotResult.createdSnapshot, createNewSnapshotResult.writeCount);
    }

    @Override
    public void listen(HeapSnapshot startSnapshot, Latch listener, long[] handles) {
        if (listener == null) throw new NullPointerException();

        if (handles.length == 0)
            throw new NoProgressPossibleException();

        statistics.listenNonBlockingStatistics.incEnterCount();

        HeapSnapshotImpl newActiveSnapshot;

        //the listener only needs to be registered once.
        boolean success;
        do {
            HeapSnapshotImpl activeSnapshot = snapshotRef.get();

            //check if the desired update already has taken place.
            for (long handle : handles) {
                if (hasUpdate(activeSnapshot, handle, listener, startSnapshot.getVersion()))
                    return;

                //if the listener is opened
                if (listener.isOpen())
                    return;
            }

            //the update has not taken place, so we need to register the listener. This is done by
            //create a newActiveSnapshot containing the added listener.
            newActiveSnapshot = activeSnapshot.createNewByAddingListener(listener, handles);
            if (newActiveSnapshot == null)
                throw new RuntimeException();//todo

            //if there is no change, we are done.
            if (newActiveSnapshot == activeSnapshot)
                return;

            success = snapshotRef.compareAndSet(activeSnapshot, newActiveSnapshot);
            if (!success)
                statistics.listenNonBlockingStatistics.incFailureCount();
        } while (!success);

        newActiveSnapshot.activate();
    }

    private boolean hasUpdate(HeapSnapshotImpl currentSnapshot, long handle, Latch latch, long transactionVersion) {
        long version = currentSnapshot.readVersion(handle);
        if (version == -1) {
            //the object doesn't exist anymore.
            //lets open the latch so that is can be cleaned up.
            latch.open();
            throw new NoSuchObjectException(handle, currentSnapshot.version);
        } else if (version > transactionVersion) {
            //woohoo, we have an overwrite, the latch can be opened, and we can end this method.
            //The event the transaction is waiting for already has occurred.
            latch.open();
            return true;
        } else {
            //The event the transaction is waiting for, hasn't occurred yet. So lets register the latch.
            //The latch is always registered on the Snapshot of the transaction version.
            return false;
        }
    }

    /**
     * GrowingHeap specific HeapSnapshot implementation.
     * <p/>
     * Class is completely immutable.
     */
    private class HeapSnapshotImpl implements HeapSnapshot<I> {
        private final HeapNode root;
        private final long version;
        private Stack<ListenerNode> listeners;

        HeapSnapshotImpl() {
            version = 0;
            root = null;
        }

        HeapSnapshotImpl(HeapNode root, long version, Stack<ListenerNode> listeners) {
            this.root = root;
            this.version = version;
            this.listeners = listeners;
        }

        @Override
        public long getVersion() {
            return version;
        }

        @Override
        public I read(long handle) {
            statistics.readCount.incrementAndGet();

            if (handle == 0 || root == null)
                return null;

            HeapNode node = root.find(handle);
            return node == null ? null : (I) node.getBlock().getDeflated();
        }

        @Override
        public long readVersion(long handle) {
            if (root == null || handle == 0)
                return -1;

            HeapNode node = root.find(handle);
            return node == null ? -1 : node.getBlock().getDeflated().___getVersion();
        }


        @Override
        public Pair<TransactionId, LockMode> readLockInfo(long handle) {
            HeapNode node;
            if (root == null || (node = root.find(handle)) == null)
                throw new NoSuchObjectException(handle);

            Block block = node.getBlock();
            return new Pair<TransactionId, LockMode>(block.getLockOwner(), block.getLockMode());
        }

        /**
         * @param lockOwner
         * @param lockMode
         * @param handle    the handle of the object to lock.
         * @return the new HeapSnapshotImpl  or null to indicate failure
         * @throws NoSuchObjectException if the object with the specified handle doesn't exist.
         */
        private HeapSnapshotImpl createNewForSettingPessimisticLock(TransactionId lockOwner, LockMode lockMode, long handle) {
            if (root == null)
                throw new NoSuchObjectException(handle);

            HeapNode newRoot = root.createNewForUpdatingLockMode(lockOwner, lockMode, handle);
            if (newRoot == null)
                return null;

            //if there is no change, we can return the original
            if (newRoot == root)
                return this;

            return new HeapSnapshotImpl(newRoot, version + 1, null);
        }

        /**
         * Creates a new HeapSnapshot by adding a listener.
         *
         * @param listener
         * @param handles
         * @return
         */
        public HeapSnapshotImpl createNewByAddingListener(Latch listener, long[] handles) {
            if (root == null)
                return null;//return null to indicate failure..

            HeapNode newRoot = root;
            for (long handle : handles) {
                newRoot = newRoot.createNewForAddingListener(handle, listener);

                //if the newRoot is null, a failure has occurred, so null can be returned.
                if (newRoot == null)
                    return null;
            }

            return new HeapSnapshotImpl(newRoot, version + 1, null);
        }

        /**
         * Creates a new HeapSnapshotImpl based on the current HeapSnapshot and all the changes. Since
         * each HeapSnapshot  is immutable, a new HeapSnapshotImpl is created instead of modifying the
         * existing one.
         *
         * @param changes       an iterator over all the DehydratedStmObject that need to be written
         * @param startSnapshot the heapsnapshot when te transaction began. This
         *                      information is required for write conflict detection.
         * @return the created HeapSnapshot or null of there was a write conflict.
         */
        private CreateNewSnapshotResult createNewForCommit(Iterator<D> changes, HeapSnapshot startSnapshot) {
            long newVersion = version + 1;

            HeapNode newRoot = root;
            //the snapshot the transaction sees when it begin. All changes it made on objects, are on objects
            //loaded from this version.

            long startOfTransactionVersion = startSnapshot.getVersion();

            //when the created snapshot, all listeners need to be notified that were listening for
            //an update one of of the written fields.
            Stack<ListenerNode> listenersNeedingNotification = new Stack<ListenerNode>();

            int writeCount = 0;
            for (; changes.hasNext();) {
                D change = changes.next();
                Deflated deflated = change.___deflate(newVersion);

                if (newRoot == null) {
                    newRoot = new HeapNode(new Block(deflated));
                } else {
                    newRoot = newRoot.createNewForWrite(
                            deflated,
                            startOfTransactionVersion,
                            listenersNeedingNotification);

                    if (newRoot == null)//createNewForWrite conflict.
                        return new CreateNewSnapshotResult();//ugly constructor..
                }

                writeCount++;
            }

            HeapSnapshotImpl newSnapshot = new HeapSnapshotImpl(newRoot, newVersion, listenersNeedingNotification);
            return new CreateNewSnapshotResult(writeCount, newSnapshot);
        }

        @Override
        public String toString() {
            return format("Snapshot(version=%s)", version);
        }

        public void activate() {
            if (listeners != null) {
                for (ListenerNode node : listeners) {
                    do {
                        node.getListener().open();
                        node = node.getPrevious();
                    } while (node != null);
                }
                listeners = null;
            }
        }
    }

    private class CreateNewSnapshotResult {
        HeapSnapshotImpl createdSnapshot;
        boolean success;
        int writeCount;

        CreateNewSnapshotResult(int writeCount, HeapSnapshotImpl createdSnapshot) {
            this.createdSnapshot = createdSnapshot;
            this.success = true;
            this.writeCount = writeCount;
        }

        CreateNewSnapshotResult() {
            this.success = false;
        }
    }
}