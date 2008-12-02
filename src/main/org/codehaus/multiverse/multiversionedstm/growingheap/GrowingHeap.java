package org.codehaus.multiverse.multiversionedstm.growingheap;

import org.codehaus.multiverse.multiversionedstm.DehydratedStmObject;
import org.codehaus.multiverse.multiversionedstm.Heap;
import org.codehaus.multiverse.multiversionedstm.HeapSnapshot;
import org.codehaus.multiverse.multiversionedstm.StmObject;
import org.codehaus.multiverse.transaction.BadVersionException;
import org.codehaus.multiverse.transaction.NoSuchObjectException;
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

    /**
     * Returns the Snapshot with the specific version.
     * <p/>
     * todo: what to do if the snapshots with version doesn't exist anymore.
     *
     * @param version the specific version of the HeapSnapshot to look for.
     * @return the found HeapSnapshot
     * @throws
     */
    private HeapSnapshotImpl getSpecificVersion(long version) {
        HeapSnapshotImpl snapshot = (HeapSnapshotImpl) getSnapshot(version);
        if (snapshot.version != version)
            throw new IllegalArgumentException(format("Snapshot with version %s is not found", version));
        return snapshot;
    }

    public long write(ResetableIterator<StmObject> changes) {
        assert changes != null;

        //todo: check if there are changes. If there are no changes.. you have to create a new snapshot.

        writeTries.incrementAndGet();

        HeapSnapshotImpl newSnapshot;
        boolean someoneElseDidAnUpdate;
        do {
            HeapSnapshotImpl currentSnapshot = currentSnapshotReference.get();
            newSnapshot = currentSnapshot.createNew(changes);
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

        return newSnapshot.getVersion();
    }

    private void wakeupListeners(HeapSnapshotImpl newSnapshot, long transactionVersion) {
        //todo: this can be done on another thread, it doesn't have to be done on the current one.
        newSnapshot.wakeupListeners(transactionVersion);
    }


    public void listen(Latch latch, long[] handles, long transactionVersion) {
        if (latch == null) throw new NullPointerException();

        if (handles.length == 0) {
            latch.open();
            return;
        }

        HeapSnapshotImpl currentSnapshot = currentSnapshotReference.get();
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
                HeapSnapshotImpl snapshot = getSpecificVersion(transactionVersion);
                LatchGroup latchGroup = snapshot.getOrCreateLatchGroup(handle);
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
        sb.append(format("write retry percentage %s \n", writeRetryPercentage));
        return sb.toString();
    }

    //class is immutable.
    private static class HeapSnapshotImpl implements HeapSnapshot {
        private final HeapSnapshotImpl parentSnapshot;
        //private final Bucket[] buckets;
        private final HeapTreeNode node;
        private final long version;
        private final long[] roots;

        HeapSnapshotImpl() {
            version = 0;
            roots = new long[]{};
            parentSnapshot = null;
            node = null;
        }

        HeapSnapshotImpl(HeapSnapshotImpl parentSnapshot, HeapTreeNode node, long version) {
            this.parentSnapshot = parentSnapshot;
            this.node = node;
            this.version = version;
            this.roots = new long[]{};
        }

        public long getVersion() {
            return version;
        }

        public long[] getRoots() {
            return roots;
        }

        public HeapSnapshotImpl getSnapshot(long version) {
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


        public DehydratedStmObject read(long handle) {
            return node == null ? null : node.find(handle).getContent();
        }

        public long getVersion(long handle) {
            return node == null ? null : node.find(handle).getVersion();
        }

        public HeapSnapshotImpl createNew(Iterator<StmObject> changes) {
            long newVersion = version + 1;

            HeapTreeNode newNode = node;

            for (; changes.hasNext();) {
                StmObject stmObject = changes.next();

                if (hasWriteConflict(stmObject))
                    return null;

                if (newNode == null)
                    newNode = new HeapTreeNode(stmObject.___dehydrate(newVersion), null, null);
                else
                    newNode = newNode.createNew(stmObject.___dehydrate(newVersion));
            }

            return new HeapSnapshotImpl(this, newNode, newVersion);
        }

        private boolean hasWriteConflict(StmObject stmObject) {
            DehydratedStmObject oldDehydratedStmObject = stmObject.___getInitialDehydratedStmObject();
            if (oldDehydratedStmObject != null) {
                long activeVersion = getVersion(stmObject.___getHandle());
                if (activeVersion > oldDehydratedStmObject.getVersion()) {
                    return true;
                }
            }

            return false;
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
