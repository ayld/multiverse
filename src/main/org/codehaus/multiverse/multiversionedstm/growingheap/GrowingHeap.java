package org.codehaus.multiverse.multiversionedstm.growingheap;

import org.codehaus.multiverse.multiversionedstm.DehydratedStmObject;
import org.codehaus.multiverse.multiversionedstm.Heap;
import org.codehaus.multiverse.multiversionedstm.HeapCommitResult;
import org.codehaus.multiverse.multiversionedstm.HeapSnapshot;
import org.codehaus.multiverse.core.BadVersionException;
import org.codehaus.multiverse.core.NoSuchObjectException;
import org.codehaus.multiverse.util.iterators.ArrayIterator;
import org.codehaus.multiverse.util.iterators.ResetableIterator;
import org.codehaus.multiverse.util.latches.Latch;

import static java.lang.String.format;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Default {@link Heap} implementation that is able to grow.
 * <p/>
 * idea: when a write is done, you know which overwrites there are. With this information you know which listeners
 * to wakeup.
 *
 * @author Peter Veentjer.
 */
public final class GrowingHeap implements Heap {

    private final AtomicLong nextFreeHandler = new AtomicLong();

    private final AtomicReference<GrowingHeapSnapshot> currentSnapshotReference = new AtomicReference<GrowingHeapSnapshot>();
    private final GrowingHeapStatistics statistics = new GrowingHeapStatistics();
    private final ListenerSupport listenerSupport = new DefaultListenerSupport();

    public GrowingHeap() {
        currentSnapshotReference.set(new GrowingHeapSnapshot());
    }

    public GrowingHeapStatistics getStatistics() {
        return statistics;
    }

    public long createHandle() {
        return nextFreeHandler.incrementAndGet();
    }

    public HeapSnapshot getActiveSnapshot() {
        return currentSnapshotReference.get();
    }

    public HeapSnapshot getSnapshot(long version) {
        return currentSnapshotReference.get().getSnapshot(version);
    }

    /**
     * Commits the changes to the heap. This is a convenience method that allows for a varargs.
     *
     * @param startVersion
     * @param changes
     * @return
     * @see #commit(long, org.codehaus.multiverse.util.iterators.ResetableIterator)
     */
    public HeapCommitResult commit(long startVersion, DehydratedStmObject... changes) {
        return commit(startVersion, new ArrayIterator<DehydratedStmObject>(changes));
    }

    public int getSnapshotAliveCount() {
        int result = 0;
        GrowingHeapSnapshot current = currentSnapshotReference.get();
        do {
            result++;
            current = current.parentSnapshot;
        } while (current != null);

        return result;
    }

    public HeapCommitResult commit(long startVersion, ResetableIterator<DehydratedStmObject> changes) {
        if (changes == null) throw new NullPointerException();

        statistics.commitNonBlockingStatistics.incEnterCount();

        if (!changes.hasNext()) {
            //if there are no changes to write to the heap, the transaction was readonly and we are done.
            statistics.commitReadonlyCount.incrementAndGet();
            return HeapCommitResult.createReadOnly(currentSnapshotReference.get());
        }

        GrowingHeapSnapshot newSnapshot;
        boolean anotherTransactionDidCommit;
        CreateNewResult createNewResult;
        do {
            GrowingHeapSnapshot currentSnapshot = currentSnapshotReference.get();
            createNewResult = currentSnapshot.createNew(changes, startVersion);
            if (!createNewResult.success) {
                //if there was a write conflict, we can end by returning a writeconflict-result 
                statistics.commitWriteConflictCount.incrementAndGet();
                return HeapCommitResult.createWriteConflict();
            }

            newSnapshot = createNewResult.snapshot;

            anotherTransactionDidCommit = !currentSnapshotReference.compareAndSet(currentSnapshot, newSnapshot);
            if (anotherTransactionDidCommit) {
                //if another transaction also did a commit while we were creating a new snapshot, we
                //have to try again. We need to reset the changes iterator for that.
                statistics.commitNonBlockingStatistics.incFailureCount();
                changes.reset();
            }
        } while (anotherTransactionDidCommit);

        //the commit was a success, so lets update the state.
        listenerSupport.wakeupListeners(newSnapshot.getVersion(), createNewResult.toHandlesArray());
        statistics.commitSuccessCount.incrementAndGet();
        statistics.committedStoreCount.addAndGet(createNewResult.handles.size());
        return HeapCommitResult.createSuccess(createNewResult.snapshot, createNewResult.handles.size());
    }

    public void listen(Latch latch, long[] handles, long transactionVersion) {
        if (latch == null) throw new NullPointerException();

        //if there are no addresses to listen to, open the latch and return.
        //todo: is this desirable behavior? Do you event want to allow this situation?
        if (handles.length == 0) {
            latch.open();
            return;
        }

        statistics.listenNonBlockingStatistics.incEnterCount();

        //the latch only needs to be registered once.
        boolean listenersAdded = false;
        boolean success;
        do {
            GrowingHeapSnapshot snapshot = currentSnapshotReference.get();
            for (long handle : handles) {
                if (hasUpdate(snapshot, handle, latch, transactionVersion))
                    return;

                //if the latch is opened
                if (latch.isOpen())
                    return;
            }

            if (!listenersAdded) {
                listenersAdded = true;
                listenerSupport.addListener(transactionVersion + 1, handles, latch);
            }

            success = snapshot == currentSnapshotReference.get();
            if (!success)
                statistics.listenNonBlockingStatistics.incFailureCount();
        } while (!success);
    }

    private boolean hasUpdate(GrowingHeapSnapshot currentSnapshot, long handle, Latch latch, long transactionVersion) {
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

    private static class CreateNewResult {
        GrowingHeapSnapshot snapshot;
        Set<Long> handles;
        boolean success;

        static CreateNewResult createSuccess(Set<Long> handles, GrowingHeapSnapshot snapshot) {
            CreateNewResult result = new CreateNewResult();
            result.handles = handles;
            result.snapshot = snapshot;
            result.success = true;
            return result;
        }

        static CreateNewResult createWriteConflict() {
            CreateNewResult result = new CreateNewResult();
            result.success = false;
            return result;
        }

        long[] toHandlesArray() {
            int handlesLength = handles.size();
            long[] result = new long[handlesLength];
            Iterator<Long> it = handles.iterator();
            for (int k = 0; k < handlesLength; k++) {
                result[k] = it.next();
            }

            return result;
        }
    }

    /**
     * GrowingHeap specific HeapSnapshot implementation.
     * <p/>
     * Class is completely immutable.
     */
    private class GrowingHeapSnapshot implements HeapSnapshot {
        private final GrowingHeapSnapshot parentSnapshot;
        private final HeapTreeNode root;
        private final long version;
        private final long[] roots;

        GrowingHeapSnapshot() {
            version = 0;
            roots = new long[]{};
            parentSnapshot = null;
            root = null;
        }

        GrowingHeapSnapshot(GrowingHeapSnapshot parentSnapshot, HeapTreeNode root, long version) {
            this.parentSnapshot = parentSnapshot;
            this.root = root;
            this.version = version;
            this.roots = new long[]{};
        }

        public long getVersion() {
            return version;
        }

        public long[] getRoots() {
            return roots;
        }

        /**
         * Gets the Snapshot with equal or smaller to the specified version.
         *
         * @param version the version of the Snapshot to look for.
         * @return the found Snapshot.
         * @throws BadVersionException if no Snapshot exists with a version equal or smaller to the specified version.
         */
        GrowingHeapSnapshot getSnapshot(long version) {
            GrowingHeapSnapshot current = this;
            long oldest = -1;
            do {
                if (current.version <= version)
                    return current;

                if (current.parentSnapshot == null)
                    oldest = current.getVersion();

                current = current.parentSnapshot;
            } while (current != null);

            String msg = format("Snapshot with a version equal or smaller than  %s is not found, oldest version found is %s",
                    version, oldest);
            throw new BadVersionException(msg);
        }

        /**
         * Get the Snapshot with the specific version.
         * <p/>
         * todo: what to do if the snapshots with version doesn't exist anymore.
         *
         * @param version the specific version of the HeapSnapshot to look for.
         * @return the found HeapSnapshot. The value will always be not null
         * @throws IllegalArgumentException if the Snapshot with the specific version is not found.
         */
        private GrowingHeapSnapshot getSpecificSnapshot(long version) {
            GrowingHeapSnapshot snapshot = getSnapshot(version);
            if (snapshot.version != version)
                throw new IllegalArgumentException(format("Snapshot with version %s is not found", version));
            return snapshot;
        }

        public DehydratedStmObject read(long handle) {
            statistics.readCount.incrementAndGet();

            if (handle == 0)
                return null;

            return root == null ? null : root.find(handle).getContent();
        }

        public long readVersion(long handle) {
            if (handle == 0)
                return -1;

            return root == null ? -1 : root.find(handle).getVersion();
        }

        /**
         * Creates a new HeapSnapshotImpl based on the current HeapSnapshot and all the changes. Since
         * each HeapSnapshot  is immutable, a new HeapSnapshotImpl is created instead of modifying the
         * existing one.
         *
         * @param changes      an iterator over all the DehydratedStmObject that need to be written
         * @param startVersion the version of the heap when the transaction, that wants to commits, began. This
         *                     information is required for write conflict detection.
         * @return the created HeapSnapshot or null of there was a write conflict.
         */
        public CreateNewResult createNew(Iterator<DehydratedStmObject> changes, long startVersion) {
            long commitVersion = version + 1;

            HeapTreeNode newRoot = root;
            //the snapshot the transaction sees when it begin. All changes it made on objects, are on objects
            //loaded from this version.
            GrowingHeapSnapshot startSnapshot = getSpecificSnapshot(startVersion);
            Set<Long> handles = new HashSet<Long>();

            for (; changes.hasNext();) {
                DehydratedStmObject stmObject = changes.next();

                long handle = stmObject.getHandle();

                if (hasWriteConflict(handle, startSnapshot))
                    return CreateNewResult.createWriteConflict();

                handles.add(handle);

                if (newRoot == null)
                    newRoot = new HeapTreeNode(stmObject, commitVersion, null, null);
                else
                    newRoot = newRoot.createNew(stmObject, commitVersion);
            }

            GrowingHeapSnapshot newSnapshot = new GrowingHeapSnapshot(this, newRoot, commitVersion);
            return CreateNewResult.createSuccess(handles, newSnapshot);
        }

        /**
         * Checks if the current HeapSnapshot has a write conflict at the specified handle with the startSnapshot.
         *
         * @param handle        the handle of the Object to check.
         * @param startSnapshot the Snapshot of the Heap when the transaction that wants to write
         * @return true if there was a write conflict, false otherwise.
         */
        private boolean hasWriteConflict(long handle, GrowingHeapSnapshot startSnapshot) {
            //if the current snapshot is the same as the startSnapshot, other transactions didn't update the heap.
            //so we know that there is no commit conflict. Further checking is not needed.
            if (startSnapshot == this)
                return false;

            long previousVersion = startSnapshot.readVersion(handle);

            //if the object is new to the heap, there is no commit conflict.
            if (previousVersion == -1)
                return false;

            //the version of object in the current snapshot.
            long newVersion = readVersion(handle);

            //the object was in the heap at some point in time and not visible from the current HeapSnapshot anymore,
            //because it has been removed, so there is a commit conflict
            if (newVersion == -1)
                return true;

            //the object is found in both snapshots. If the version still is the same, there is no commit conflict.
            //if the version isn't the same, it means that it has been updated by a different transaction in the
            //mean while.
            return newVersion != previousVersion;
        }
    }
}