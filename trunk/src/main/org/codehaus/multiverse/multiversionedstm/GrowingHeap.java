package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.transaction.BadVersionException;
import org.codehaus.multiverse.transaction.NoSuchObjectException;
import org.codehaus.multiverse.util.iterators.ResetableIterator;
import org.codehaus.multiverse.util.latches.Latch;
import org.codehaus.multiverse.util.latches.StandardLatch;
import org.codehaus.multiverse.util.latches.LatchGroup;
import org.codehaus.multiverse.util.latches.OpenLatch;

import static java.lang.String.format;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * idea: since some dehydrateccitizen are connected to others.. perhaps they could be grouped somehow?
 *
 * @author Peter Veentjer.
 */
public final class GrowingHeap implements Heap {

    private final AtomicLong nextFreeHandler = new AtomicLong();
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

        HeapSnapshotImpl newSnapshot;
        boolean someoneElseDidAnUpdate;
        do {
            HeapSnapshotImpl currentSnapshot = currentSnapshotReference.get();
            newSnapshot = currentSnapshot.createNew(changes);
            if (newSnapshot == null)//a write conflict was detected, so return -1 to indicate that
                return -1;

            someoneElseDidAnUpdate = !currentSnapshotReference.compareAndSet(currentSnapshot, newSnapshot);
            if (someoneElseDidAnUpdate){
                System.out.println("------------------someOneElseDidAndUpdate");
                changes.reset();
            }

        } while (someoneElseDidAnUpdate);

        wakeupListeners(newSnapshot);

        return newSnapshot.getVersion();
    }

    private void wakeupListeners(HeapSnapshotImpl newSnapshot) {
        //todo: this can be done on another thread, it doesn't have to be done on the current one.
        newSnapshot.wakeupListeners();
    }

    public Latch listen(long[] handles, long transactionVersion) {
        if (handles.length == 0)
            return OpenLatch.INSTANCE;

        Latch listenerLatch = new StandardLatch();
        HeapSnapshotImpl currentSnapshot = currentSnapshotReference.get();
        for (long handle : handles) {
            long version = currentSnapshot.getVersion(handle);
            if (version == -1) {
                //the object doesn't exist anymore.

                //lets open the latch so that is can be cleaned up.
                listenerLatch.open();
                throw new NoSuchObjectException(handle, currentSnapshot.version);
            } else if (version > transactionVersion) {
                //woohoo, we have an overwrite, the latch can be opened, and it can be returned.
                listenerLatch.open();
                return listenerLatch;
            } else {
                //no update has been found, so lets register the latch
                HeapSnapshotImpl snapshot = getSpecificVersion(transactionVersion);
                LatchGroup latchGroup = snapshot.getOrCreateLatchGroup(handle);
                latchGroup.add(listenerLatch);
            }

            //if the latch is opened, we don't have to register the other handles and we can
            //return the opened latch.
            if (listenerLatch.isOpen())
                return listenerLatch;
        }

        HeapSnapshotImpl newSnapshot = currentSnapshotReference.get();
        if (newSnapshot != currentSnapshot) {
            //another transaction made an update. And chances are that it didn't wakeup the listener we just registered,
            //so lets do that ourselfes to make sure that it has been done. If we don't do this, it could be that
            //a latch is not openend, even though an interesting update has taken place. This is undesirable behavior.
            //todo: this can be done on another thread.
            wakeupListeners(newSnapshot);

            System.out.println("--------------- someone did an update while we are listening");
        } else {
            //no other transaction have made updates, so it is now the responsibility of an updating transaction
            //to wake up the listener.
        }

        return listenerLatch;
    }

    public void signalVersionDied(long version) {
        throw new RuntimeException();
    }

    //class is immutable.
    private static class HeapSnapshotImpl implements HeapSnapshot {
        private final HeapSnapshotImpl parentSnapshot;
        private final Bucket[] buckets;
        private final long version;
        private final long[] roots;

        HeapSnapshotImpl() {
            version = 0;
            roots = new long[]{};
            parentSnapshot = null;
            buckets = new Bucket[31];
        }

        HeapSnapshotImpl(HeapSnapshotImpl parentSnapshot, Bucket[] buckets, long version) {
            this.parentSnapshot = parentSnapshot;
            this.buckets = buckets;
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
            Bucket bucket = getBucket(handle);
            return bucket == null ? null : bucket.get(handle);
        }

        public long getVersion(long handle) {
            int bucketIndex = getBucketIndex(handle);
            Bucket bucket = buckets[bucketIndex];
            return bucket == null ? -1 : bucket.getVersion(handle);
        }

        private Bucket getBucket(long handle) {
            int bucketIndex = getBucketIndex(handle);
            return buckets[bucketIndex];
        }

        public HeapSnapshotImpl createNew(Iterator<StmObject> changes) {
            long newVersion = version + 1;

            List<DehydratedStmObject>[] bucketsContent = new List[31];
            for (; changes.hasNext();) {
                StmObject stmObject = changes.next();

                if (hasWriteConflict(stmObject))
                    return null;

                long handle = stmObject.___getHandle();
                int bucketIndex = getBucketIndex(handle);
                List<DehydratedStmObject> bucketContent = bucketsContent[bucketIndex];
                if (bucketContent == null) {
                    bucketContent = new LinkedList();
                    bucketsContent[bucketIndex] = bucketContent;
                }

                DehydratedStmObject dehydrated = stmObject.___dehydrate(newVersion);
                bucketContent.add(dehydrated);
            }

            Bucket[] newBuckets = new Bucket[31];
            for (int bucketIndex = 0; bucketIndex < bucketsContent.length; bucketIndex++) {
                List<DehydratedStmObject> bucketContent = bucketsContent[bucketIndex];

                if (bucketContent == null) {
                    newBuckets[bucketIndex] = buckets[bucketIndex];
                } else {
                    newBuckets[bucketIndex] = new Bucket(
                            newVersion,
                            bucketContent,
                            parentSnapshot);
                }
            }

            return new HeapSnapshotImpl(this, newBuckets, newVersion);
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

        private int getBucketIndex(long handle) {
            return ((int) (handle ^ (handle >>> 32))) % 31;
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

        public void wakeupListeners() {
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

                current = current.parentSnapshot;
            } while (current != null);
        }
    }

    //immutable
    static class Bucket {
        final long version;
        final List<DehydratedStmObject> dehydratedObjects;
        final HeapSnapshotImpl parentHeapSnapshot;

        Bucket(long version, List<DehydratedStmObject> dehydratedObjects, HeapSnapshotImpl parentHeapSnapshot) {
            this.version = version;
            this.dehydratedObjects = dehydratedObjects;
            this.parentHeapSnapshot = parentHeapSnapshot;
        }

        DehydratedStmObject get(long handle) {
            //by first looking at the current values, you ignore the overwritten values..
            for (DehydratedStmObject dehydrated : dehydratedObjects) {
                if (dehydrated.getHandle() == handle)
                    return dehydrated;
            }

            return parentHeapSnapshot == null ? null : parentHeapSnapshot.read(handle);
        }

        long getVersion(long handle) {
            for (DehydratedStmObject dehydrated : dehydratedObjects) {
                if (dehydrated.getHandle() == handle)
                    return version;
            }

            return parentHeapSnapshot == null ? -1 : parentHeapSnapshot.getVersion(handle);
        }
    }
}
