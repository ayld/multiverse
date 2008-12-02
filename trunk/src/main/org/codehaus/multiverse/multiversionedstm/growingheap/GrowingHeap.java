package org.codehaus.multiverse.multiversionedstm.growingheap;

import org.codehaus.multiverse.multiversionedstm.DehydratedStmObject;
import org.codehaus.multiverse.multiversionedstm.Heap;
import org.codehaus.multiverse.multiversionedstm.HeapSnapshot;
import org.codehaus.multiverse.transaction.BadVersionException;
import org.codehaus.multiverse.transaction.NoSuchObjectException;
import org.codehaus.multiverse.util.iterators.ArrayIterator;
import org.codehaus.multiverse.util.iterators.ResetableIterator;
import org.codehaus.multiverse.util.latches.Latch;
import org.codehaus.multiverse.util.latches.LatchGroup;

import static java.lang.String.format;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * idea: when a write is done, you know which overwrites there are. With this information you know which listeners
 * to wakeup.
 *
 * @author Peter Veentjer.
 */
public final class GrowingHeap implements Heap {

    private final AtomicLong nextFreeHandler = new AtomicLong();
    private final AtomicLong writeTries = new AtomicLong();
    private final AtomicLong writeRetries = new AtomicLong();
    private final AtomicLong writeConflicts = new AtomicLong();
    private final AtomicLong writeSuccesses = new AtomicLong();
    private final AtomicReference<HeapSnapshotImpl> currentSnapshotReference = new AtomicReference<HeapSnapshotImpl>();

    public GrowingHeap() {
        currentSnapshotReference.set(new HeapSnapshotImpl());
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

    public long write(long startVersion, DehydratedStmObject... changes) {
        return write(startVersion, new ArrayIterator(changes));
    }

    public long write(long startVersion, ResetableIterator<DehydratedStmObject> changes) {
        assert changes != null;

        //todo: check if there are changes. If there are no changes.. you don't have to create a new snapshot.

        writeTries.incrementAndGet();

        HeapSnapshotImpl newSnapshot;
        boolean someoneElseDidAnUpdate;
        do {
            HeapSnapshotImpl currentSnapshot = currentSnapshotReference.get();
            newSnapshot = currentSnapshot.createNew(changes, startVersion);
            if (newSnapshot == null) {
                //a write conflict was detected, so return -1 to indicate that
                writeConflicts.incrementAndGet();
                return -1;
            }

            someoneElseDidAnUpdate = !currentSnapshotReference.compareAndSet(currentSnapshot, newSnapshot);
            if (someoneElseDidAnUpdate) {
                writeRetries.incrementAndGet();
                changes.reset();
            }

        } while (someoneElseDidAnUpdate);

        wakeupListeners(newSnapshot, 0);

        writeSuccesses.incrementAndGet();
        return newSnapshot.getVersion();
    }

    private void wakeupListeners(HeapSnapshotImpl newSnapshot, long transactionVersion) {
        //todo: this can be done on another thread, it doesn't have to be done on the current one.
        newSnapshot.wakeupListeners(transactionVersion);
    }

    public void listen(Latch latch, long[] handles, long transactionVersion) {
        if (latch == null) throw new NullPointerException();

        //if there are no addresses to listen to, open the latch and return.
        //todo: is this desirable behavior? Do you event want to allow this situation?
        if (handles.length == 0) {
            latch.open();
            return;
        }

        HeapSnapshotImpl currentSnapshot = currentSnapshotReference.get();

        //this is the snapshot where the latches get added to.
        HeapSnapshotImpl transactionSnapshot = currentSnapshot.getSpecificSnapshot(transactionVersion);
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
                LatchGroup latchGroup = transactionSnapshot.getOrCreateLatchGroup(handle);
                latchGroup.add(latch);
            }

            //if the latch is opened, we don't have to register the other handles and we can return
            if (latch.isOpen())
                return;
        }

        HeapSnapshotImpl newSnapshot = currentSnapshotReference.get();
        if (newSnapshot != currentSnapshot) {
            //another transaction made an update. And chances are that it didn't wakeup the listener we just registered,
            //so lets do that ourselfes to make sure that it has been done. If we don't do this, it could be that
            //a latch is not openend, even though an interesting update has taken place. This is undesirable behavior.
            //todo: this can be done on another thread.
            wakeupListeners(newSnapshot, transactionVersion);
        } else {
            //no other transaction have made updates, so it is now the responsibility of an updating transaction
            //to wake up the listener.
        }
    }

    public void signalVersionDied(long version) {
        throw new RuntimeException();
    }

    public String getStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append(format("write tries %s\n", writeTries.longValue()));
        sb.append(format("write conflicts %s\n", writeConflicts.longValue()));
        sb.append(format("write retries %s\n", writeRetries.longValue()));
        double writeRetryPercentage = (100.0d * writeRetries.longValue()) / writeTries.longValue();
        sb.append(format("write retry percentage %s\n", writeRetryPercentage));
        sb.append(format("write succeeded %s\n", writeSuccesses.longValue()));
        return sb.toString();
    }

    //class is immutable.
    private static class HeapSnapshotImpl implements HeapSnapshot {
        private final HeapSnapshotImpl parentSnapshot;
        //private final Bucket[] buckets;
        private final HeapTreeNode root;
        private final long version;
        private final long[] roots;

        HeapSnapshotImpl() {
            version = 0;
            roots = new long[]{};
            parentSnapshot = null;
            root = null;
        }

        HeapSnapshotImpl(HeapSnapshotImpl parentSnapshot, HeapTreeNode root, long version) {
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
         * @param version
         * @return
         */
        HeapSnapshotImpl getSnapshot(long version) {
            HeapSnapshotImpl current = this;
            long oldest = -1;
            do {
                if (current.version <= version)
                    return current;

                if (current.parentSnapshot == null) {
                    oldest = current.getVersion();
                }

                current = current.parentSnapshot;
            } while (current != null);

            throw new BadVersionException(format("Version %s is not found, oldest version found is %s", version, oldest));
        }

        /**
         * Returns the Snapshot with the specific version.
         * <p/>
         * todo: what to do if the snapshots with version doesn't exist anymore.
         *
         * @param version the specific version of the HeapSnapshot to look for.
         * @return the found HeapSnapshot. The value will always be not null
         * @throws IllegalArgumentException if the Snapshot with the specific version is not found.
         */
        private HeapSnapshotImpl getSpecificSnapshot(long version) {
            HeapSnapshotImpl snapshot = getSnapshot(version);
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
         * @param changes an iterator over all the DehydratedStmObject that need to be written
         * @param startVersion the version of the heap when the transaction, that wants to commits, began. This
         *        information is required for write conflict detection.
         * @return the created HeapSnapshot or null of there was a write conflict.
         */
        public HeapSnapshotImpl createNew(Iterator<DehydratedStmObject> changes, long startVersion) {
            long commitVersion = version + 1;

            HeapTreeNode newRoot = root;
            //the snapshot the transaction sees when it begin. All changes it made on objects, are on objects
            //loaded from this version.
            HeapSnapshotImpl startSnapshot = getSpecificSnapshot(startVersion);

            for (; changes.hasNext();) {
                DehydratedStmObject stmObject = changes.next();

                if (hasWriteConflict(stmObject.getHandle(), startSnapshot))
                    return null;

                if (newRoot == null)
                    newRoot = new HeapTreeNode(stmObject, commitVersion, null, null);
                else
                    newRoot = newRoot.createNew(stmObject, commitVersion);
            }

            return new HeapSnapshotImpl(this, newRoot, commitVersion);
        }

        /**
         * Checks if the current HeapSnapshot has a write conflict at the specified handle with the startSnapshot.
         *
         * @param handle the handle of the Object to check.
         * @param startSnapshot the Snapshot of the Heap when the transaction that wants to write
         * @return true if there was a write conflict, false otherwise.
         */
        private boolean hasWriteConflict(long handle, HeapSnapshotImpl startSnapshot) {
            //if the current snapshot is the same as the startSnapshot, other transaction didn't update the heap.
            //so we know that there is no write conflict.
            if (startSnapshot == this)
                return false;

            long previousVersion = startSnapshot.getVersion(handle);

            //if the object is new to the heap, there is no write conflict.
            if (previousVersion == -1)
                return false;

            //the version of object in the current snapshot.
            long newVersion = getVersion(handle);

            //the object was in the heap at some point in time and not visible from the current HeapSnapshot anymore,
            //because it has been removed, so there is a write conflict
            if (newVersion == -1)
                return true;            

            //the object is found in both snapshots. If the version still is the same, there is no write conflict.
            //if the version isn't the same, it means that it has been updated by a different transaction in the mean
            //while.
            return newVersion > previousVersion;
        }

        //todo: structure could be initialized lazy.
        private final ConcurrentMap<Long, LatchGroup> latchGroups = new ConcurrentHashMap<Long, LatchGroup>();

        private LatchGroup getOrCreateLatchGroup(long handle) {
            LatchGroup latchGroup = latchGroups.get(handle);
            if (latchGroup == null) {
                LatchGroup newLatchGroup = new LatchGroup();
                latchGroup = latchGroups.putIfAbsent(handle, newLatchGroup);
                if (latchGroup == null) {
                    latchGroup = newLatchGroup;
                }
            }
            return latchGroup;
        }

        public void wakeupListeners(long transactionVersion) {
            HeapSnapshotImpl current = parentSnapshot;
            do {
                for (Map.Entry<Long, LatchGroup> entry : parentSnapshot.latchGroups.entrySet()) {
                    long handle = entry.getKey();
                    long newestVersion = getVersion(handle);
                    if (newestVersion == -1) {
                        //todo
                        throw new RuntimeException();
                    } else if (newestVersion > parentSnapshot.version) {
                        LatchGroup latchGroup = entry.getValue();
                        latchGroup.open();
                        //parentSnapshot.latchGroups.remove(handle);
                    }
                }

                //if (current.version < transactionVersion)
                //    return;

                current = current.parentSnapshot;
            } while (current != null);
        }
    }
}