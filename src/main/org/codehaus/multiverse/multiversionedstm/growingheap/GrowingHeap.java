package org.codehaus.multiverse.multiversionedstm.growingheap;

import org.codehaus.multiverse.multiversionedstm.DehydratedStmObject;
import org.codehaus.multiverse.multiversionedstm.Heap;
import org.codehaus.multiverse.multiversionedstm.HeapCommitResult;
import org.codehaus.multiverse.multiversionedstm.HeapSnapshot;
import org.codehaus.multiverse.transaction.BadVersionException;
import org.codehaus.multiverse.transaction.NoSuchObjectException;
import org.codehaus.multiverse.util.iterators.ArrayIterator;
import org.codehaus.multiverse.util.iterators.ResetableIterator;
import org.codehaus.multiverse.util.latches.Latch;

import static java.lang.String.format;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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

    private final ConcurrentMap<Long, VersionedLatchGroup> latchGroups = new ConcurrentHashMap<Long, VersionedLatchGroup>();
    private final AtomicReference<GrowingHeapSnapshot> currentSnapshotReference = new AtomicReference<GrowingHeapSnapshot>();
    private final GrowingHeapStatistics statistics = new GrowingHeapStatistics();

    public GrowingHeap() {
        currentSnapshotReference.set(new GrowingHeapSnapshot());
    }

    public GrowingHeapStatistics getStatistics() {
        return statistics;
    }

    public long createHandle() {
        return nextFreeHandler.incrementAndGet();
    }

    public HeapSnapshot getSnapshot() {
        return currentSnapshotReference.get();
    }

    public HeapSnapshot getSnapshot(long version) {
        return currentSnapshotReference.get().getSnapshot(version);
    }

    public HeapCommitResult write(long startVersion, DehydratedStmObject... changes) {
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
        assert changes != null;

        //todo: check if there are changes. If there are no changes.. you don't have to create a new snapshot.
        if (!changes.hasNext())
            return HeapCommitResult.createReadOnly(currentSnapshotReference.get());

        statistics.incCommitTriesCount();

        GrowingHeapSnapshot newSnapshot;
        boolean someoneElseDidAnUpdate;
        CreateNewResult createNewResult;
        do {
            GrowingHeapSnapshot currentSnapshot = currentSnapshotReference.get();
            createNewResult = currentSnapshot.createNew(changes, startVersion);
            if (!createNewResult.success) {
                //a commit conflict was detected, so return -1 to indicate that
                statistics.incCommitWriteConlictCount();
                return HeapCommitResult.createWriteConflict();
            }

            newSnapshot = createNewResult.snapshot;

            someoneElseDidAnUpdate = !currentSnapshotReference.compareAndSet(currentSnapshot, newSnapshot);
            if (someoneElseDidAnUpdate) {
                statistics.incCommitRetryCount();
                changes.reset();
            }

        } while (someoneElseDidAnUpdate);

        newSnapshot.wakeupListeners();

        statistics.incCommitSuccessCount();
        statistics.incCommittedStoreCount(createNewResult.handles.size());
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

        GrowingHeapSnapshot currentSnapshot = currentSnapshotReference.get();

        //this is the snapshot where the latches get added to.
        GrowingHeapSnapshot transactionSnapshot = currentSnapshot.getSpecificSnapshot(transactionVersion);
        for (long handle : handles) {
            long version = currentSnapshot.getVersion(handle);
            if (version == -1) {
                //the object doesn't exist anymore.
                //lets open the latch so that is can be cleaned up.
                latch.open();
                throw new NoSuchObjectException(handle, currentSnapshot.version);
            } else if (version > transactionVersion) {
                //woohoo, we have an overwrite, the latch can be opened, and we can end this method.
                //The event the transaction is waiting for already has occurred.
                latch.open();
                return;
            } else {
                //The event the transaction is waiting for, hasn't occurred yet. So lets register the latch.
                //The latch is always registered on the Snapshot of the transaction version. 
                VersionedLatchGroup latchGroup = transactionSnapshot.getOrCreateLatchGroup(handle);
                latchGroup.addLatch(transactionVersion + 1, latch);
            }

            //if the latch is opened, we don't have to register the other handles and we can return
            if (latch.isOpen())
                return;
        }

        GrowingHeapSnapshot newSnapshot = currentSnapshotReference.get();
        if (newSnapshot != currentSnapshot) {
            //another transaction made an update. And chances are that it didn't wakeup the listener we just registered,
            //so lets do that ourselfes to make sure that it has been done. If we don't do this, it could be that
            //a latch is not openend, even though an interesting update has taken place. This is undesirable behavior.
            //todo: this can be done on another thread.
            newSnapshot.wakeupListeners();
        } else {
            //no other transaction have made updates, so it is now the responsibility of an updating transaction
            //to wake up the listener.
        }
    }

    static class CreateNewResult {
        GrowingHeapSnapshot snapshot;
        Set<Long> handles;
        boolean success;

        public static CreateNewResult createSuccess(Set<Long> handles, GrowingHeapSnapshot snapshot) {
            CreateNewResult result = new CreateNewResult();
            result.handles = handles;
            result.snapshot = snapshot;
            result.success = true;
            return result;
        }

        public static CreateNewResult createWriteConflict() {
            CreateNewResult result = new CreateNewResult();
            result.success = false;
            return result;
        }
    }

    //class is immutable.
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
            return root == null ? null : root.find(handle).getContent();
        }

        public long getVersion(long handle) {
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

            long previousVersion = startSnapshot.getVersion(handle);

            //if the object is new to the heap, there is no commit conflict.
            if (previousVersion == -1)
                return false;

            //the version of object in the current snapshot.
            long newVersion = getVersion(handle);

            //the object was in the heap at some point in time and not visible from the current HeapSnapshot anymore,
            //because it has been removed, so there is a commit conflict
            if (newVersion == -1)
                return true;

            //the object is found in both snapshots. If the version still is the same, there is no commit conflict.
            //if the version isn't the same, it means that it has been updated by a different transaction in the
            //mean while.
            return newVersion != previousVersion;
        }


        private VersionedLatchGroup getOrCreateLatchGroup(long handle) {
            VersionedLatchGroup latchGroup = latchGroups.get(handle);
            if (latchGroup == null) {
                VersionedLatchGroup newLatchGroup = new VersionedLatchGroup(version);
                latchGroup = latchGroups.putIfAbsent(handle, newLatchGroup);
                if (latchGroup == null) {
                    latchGroup = newLatchGroup;
                }
            }
            return latchGroup;
        }

        /**
         * Wakes up all listeners. This is called by a writing thread that has just updated the heap.
         *
         * @param handles
         */
        public void wakeupListenersByWriter(Iterator<Long> handles) {
            for (; handles.hasNext();) {
                //this is the address a commit has happened.
                long handle = handles.next();

                //so you need to wakup listeners at the specified handle. But which version(s)?
                //do you need to check listeners on all version?
                //at the moment this is required: it could be the there is a listener listening on version 6
                //and also a listener for 7...8... etc..
                //so if an update happens, they all need to be notified. And the big problem is that the complexity
                //of this behavior is O(n) with n being the number of versions. The complexity should be reduced to
                //O(c). The question is: how can this be done.. Is there some way to prevent checking the other
                //versions. When a new snapshot is activated, can't it get hold of the listeners of the previous
                //snapshot? If you have the guarantee that all listeners are attached to the current snapshot.. you
                //don't need to look far.
            }

            //GrowingHeapSnapshot current = parentSnapshot;
            //do {
            //    for (Map.Entry<Long, LatchGroup> entry : parentSnapshot.latchGroups.entrySet()) {
            //        long handle = entry.getKey();
            //        long newestVersion = getVersion(handle);
            //        if (newestVersion == -1) {
            //            //todo
            //            throw new RuntimeException();
            //        } else if (newestVersion > parentSnapshot.version) {
            //            LatchGroup latchGroup = entry.getValue();
            //            latchGroup.open();
            //            //parentSnapshot.latchGroups.remove(handle);
            //        }
            //    }
            //
            //    //if (current.version < transactionVersion)
            //    //    return;
            //
            //    current = curret.parentSnapshot;
            //} while (current != null);
        }

        public void wakeupListeners() {
            for (Map.Entry<Long, VersionedLatchGroup> entry : latchGroups.entrySet()) {
                entry.getValue().activateVersion(version);
            }
        }
    }
}