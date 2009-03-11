package org.codehaus.multiverse.multiversionedheap.standard;

import org.codehaus.multiverse.api.LockMode;
import org.codehaus.multiverse.api.TransactionId;
import org.codehaus.multiverse.api.exceptions.NoProgressPossibleException;
import org.codehaus.multiverse.api.exceptions.NoSuchObjectException;
import org.codehaus.multiverse.multiversionedheap.Deflatable;
import org.codehaus.multiverse.multiversionedheap.Deflated;
import org.codehaus.multiverse.multiversionedheap.MultiversionedHeap;
import org.codehaus.multiverse.multiversionedheap.MultiversionedHeapSnapshot;
import org.codehaus.multiverse.multiversionedheap.listenersupport.DefaultListenerSupport;
import org.codehaus.multiverse.multiversionedheap.listenersupport.ListenerSupport;
import org.codehaus.multiverse.util.iterators.ArrayIterator;
import org.codehaus.multiverse.util.iterators.PLongArrayIterator;
import org.codehaus.multiverse.util.iterators.PLongIterator;
import org.codehaus.multiverse.util.iterators.ResetableIterator;
import org.codehaus.multiverse.util.latches.Latch;

import static java.lang.String.format;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Default {@link org.codehaus.multiverse.multiversionedheap.MultiversionedHeap} implementation.
 *
 * @author Peter Veentjer.
 */
public final class DefaultMultiversionedHeap<I extends Deflated, D extends Deflatable>
        implements MultiversionedHeap<I, D> {

    private final AtomicReference<MultiversionedHeapSnapshotImpl> snapshotRef =
            new AtomicReference<MultiversionedHeapSnapshotImpl>(new MultiversionedHeapSnapshotImpl());

    private final DefaultMultiversionedHeapStatistics statistics = new DefaultMultiversionedHeapStatistics();

    private final ListenerSupport listenerSupport = new DefaultListenerSupport();

    public DefaultMultiversionedHeap() {
    }

    public DefaultMultiversionedHeapStatistics getStatistics() {
        return statistics;
    }

    public MultiversionedHeapSnapshot<I> getActiveSnapshot() {
        return snapshotRef.get();
    }

    @Override
    public LockNoWaitResult lockNoWait(TransactionId transactionId, long handle, LockMode lockMode) {
        assert transactionId != null && lockMode != null;

        throw new RuntimeException();
    }

    @Override
    public void abort(TransactionId transactionId) {
        assert transactionId != null;

        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Commits the changes to the heap. This is a convenience method that allows for a varargs.
     *
     * @param startSnapshot
     * @param changes
     * @return
     */
    public CommitResult commit(MultiversionedHeapSnapshot<I> startSnapshot, D... changes) {
        return commit(startSnapshot, new ArrayIterator<D>(changes));
    }

    public CommitResult commit(MultiversionedHeapSnapshot<I> startSnapshot, ResetableIterator<D> changes) {
        assert startSnapshot != null && changes != null;

        //todo: all locks that are required by this transaction, should be removed.

        beforeCommit();

        if (!changes.hasNext()) {
            //if there are no changes to write to the heap, the transaction was readonly and we are done.
            statistics.commitReadonlyCount.incrementAndGet();
            return CommitResult.createReadOnly(snapshotRef.get());
        }

        boolean anotherTransactionDidCommit;
        CreateNewSnapshotResult createNewSnapshotResult;
        do {
            MultiversionedHeapSnapshotImpl activeSnapshot = snapshotRef.get();
            createNewSnapshotResult = activeSnapshot.createNew(changes, startSnapshot);

            //if there was a writeconflict, we are done
            if (!createNewSnapshotResult.success) {
                statistics.commitWriteConflictCount.incrementAndGet();
                return CommitResult.createWriteConflict();
            }

            //lets try to activate the created snapshot.
            anotherTransactionDidCommit = !snapshotRef.compareAndSet(
                    activeSnapshot,
                    createNewSnapshotResult.snapshot);

            if (anotherTransactionDidCommit) {
                //if another transaction also did a commit while we were creating a new snapshot, we
                //have to try again. We need to reset the changes iterator for that.
                statistics.commitNonBlockingStatistics.incFailureCount();
                changes.reset();
            }
        } while (anotherTransactionDidCommit);

        return afterCommit(createNewSnapshotResult);
    }

    private void beforeCommit() {
        statistics.commitTotalCount.incrementAndGet();
        statistics.commitNonBlockingStatistics.incEnterCount();
    }

    private CommitResult afterCommit(CreateNewSnapshotResult createNewSnapshotResult) {
        //yes.. the snapshot was activated, lets update statistics and lets wakeup listeners.
        listenerSupport.wakeupListeners(
                createNewSnapshotResult.snapshot.getVersion(),
                createNewSnapshotResult.getHandles());
        statistics.commitSuccessCount.incrementAndGet();
        statistics.committedStoreCount.addAndGet(createNewSnapshotResult.writeCount);
        return CommitResult.createSuccess(createNewSnapshotResult.snapshot, createNewSnapshotResult.writeCount);
    }

    public void listen(MultiversionedHeapSnapshot startSnapshot, Latch latch, long[] handles) {
        if (latch == null) throw new NullPointerException();

        if (handles.length == 0)
            throw new NoProgressPossibleException();

        statistics.listenNonBlockingStatistics.incEnterCount();

        //the latch only needs to be registered once.
        boolean latchIsAdded = false;
        boolean success;
        do {
            MultiversionedHeapSnapshotImpl snapshot = snapshotRef.get();
            for (long handle : handles) {
                if (hasUpdate(snapshot, handle, latch, startSnapshot.getVersion()))
                    return;

                //if the latch is opened
                if (latch.isOpen())
                    return;
            }

            if (!latchIsAdded) {
                listenerSupport.addListener(
                        startSnapshot.getVersion(),
                        new PLongArrayIterator(handles), latch);
                latchIsAdded = true;
            }

            success = snapshot == snapshotRef.get();
            if (!success)
                statistics.listenNonBlockingStatistics.incFailureCount();
        } while (!success);
    }

    private boolean hasUpdate(MultiversionedHeapSnapshotImpl currentSnapshot, long handle, Latch latch, long transactionVersion) {
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
    private class MultiversionedHeapSnapshotImpl implements MultiversionedHeapSnapshot<I> {
        private final HeapNode root;
        private final long version;

        MultiversionedHeapSnapshotImpl() {
            version = 0;
            root = null;
        }

        MultiversionedHeapSnapshotImpl(HeapNode root, long version) {
            this.root = root;
            this.version = version;
        }

        @Override
        public long getVersion() {
            return version;
        }

        @Override
        public PLongIterator getRoots() {
            throw new RuntimeException();
        }

        @Override
        public LockMode readLockMode(long handle) {
            throw new RuntimeException();
        }

        @Override
        public I read(long handle) {
            statistics.readCount.incrementAndGet();

            if (handle == 0 || root == null)
                return null;

            HeapNode<BlockImpl> node = root.find(handle);
            return node == null ? null : (I) node.getBlock().getInflatable();
        }

        @Override
        public long readVersion(long handle) {
            if (root == null || handle == 0)
                return -1;

            HeapNode<BlockImpl> node = root.find(handle);
            return node == null ? -1 : node.getBlock().getInflatable().___getVersion();
        }

        /**
         * Creates a new HeapSnapshotImpl based on the current HeapSnapshot and all the changes. Since
         * each HeapSnapshot  is immutable, a new HeapSnapshotImpl is created instead of modifying the
         * existing one.
         *
         * @param changes       an iterator over all the DehydratedStmObject that need to be written
         * @param startSnapshot the heapsnapshot when the transaction began. This
         *                      information is required for write conflict detection.
         * @return the created HeapSnapshot or null of there was a write conflict.
         */
        public CreateNewSnapshotResult createNew(Iterator<D> changes, MultiversionedHeapSnapshot startSnapshot) {
            long commitVersion = version + 1;

            HeapNode newRoot = root;
            //the snapshot the transaction sees when it begin. All changes it made on objects, are on objects
            //loaded from this version.

            long expectedVersion = startSnapshot.getVersion();

            int writeCount = 0;
            BlockImpl head = null;
            for (; changes.hasNext();) {
                D change = changes.next();
                I inflatable = (I) change.___deflate(commitVersion);
                //todo:
                head = new BlockImpl(inflatable, head, null, LockMode.none);

                if (newRoot == null) {
                    newRoot = new DefaultHeapNode(head, null, null);
                } else {
                    newRoot = newRoot.write(head, expectedVersion);
                    if (newRoot == null)//write conflict.
                        return new CreateNewSnapshotResult();//ugly constructor..
                }

                writeCount++;
            }

            MultiversionedHeapSnapshotImpl newSnapshot = new MultiversionedHeapSnapshotImpl(newRoot, commitVersion);
            return new CreateNewSnapshotResult(writeCount, newSnapshot, head);
        }

        public String toString() {
            return format("Snapshot(version=%s)", version);
        }
    }

    private class CreateNewSnapshotResult {
        MultiversionedHeapSnapshotImpl snapshot;
        boolean success;
        int writeCount;
        private BlockImpl head;

        CreateNewSnapshotResult(int writeCount, MultiversionedHeapSnapshotImpl snapshot, BlockImpl head) {
            this.snapshot = snapshot;
            this.success = true;
            this.writeCount = writeCount;
            this.head = head;
        }

        CreateNewSnapshotResult() {
            this.success = false;
        }

        PLongIterator getHandles() {
            return new PLongIterator() {
                private BlockImpl nextToReturn = head;

                public long next() {
                    if (!hasNext())
                        throw new NoSuchElementException();
                    BlockImpl result = nextToReturn;
                    nextToReturn = nextToReturn.next;
                    return result.getHandle();
                }

                public boolean hasNext() {
                    return nextToReturn != null;
                }
            };
        }
    }

    static class BlockImpl implements Block {
        private final BlockImpl next;
        private final Deflated deflated;
        private final LockMode lockMode;
        private final TransactionId lockOwner;

        public BlockImpl(Deflated deflated, BlockImpl next, TransactionId lockOwner, LockMode lockMode) {
            assert deflated != null;
            this.deflated = deflated;
            this.next = next;
            this.lockMode = lockMode;
            this.lockOwner = lockOwner;
        }

        @Override
        public LockMode getLockMode() {
            return lockMode;
        }

        @Override
        public TransactionId getOwner() {
            return lockOwner;
        }

        public long getHandle() {
            return deflated.___getHandle();
        }

        public Deflated getInflatable() {
            return deflated;
        }

        public BlockImpl getNext() {
            return next;
        }

        public String toString() {
            return format("Block(%s)", deflated);
        }
    }
}