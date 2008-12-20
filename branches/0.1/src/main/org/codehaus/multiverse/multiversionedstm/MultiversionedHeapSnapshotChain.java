package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.core.BadVersionException;

import static java.lang.String.format;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A an object that is responsible for maintaining the chain of HeapSnapshots used. Every time a commit is done,
 * a new HeapSnapshot is created and stored in this HeapSnapshotChain.
 * <p/>
 * Garbage collection:
 * As soon as a HeapSnapshot isn't used anymore, it will be garbage collected (except the first). Each HeapSnapshot
 * is wrapped in a WeakReference, so that the garbage collector can clean up unused HeapSnapshots.
 * <p/>
 * Non Blocking:
 * This HeapSnapshotChain has a non blocking add {@link #compareAndAdd(MultiversionedHeapSnapshot , MultiversionedHeapSnapshot)}.
 *
 * @author Peter Veentjer
 * @param <H>
 */
public final class MultiversionedHeapSnapshotChain<H extends MultiversionedHeapSnapshot> {

    private final AtomicReference<Node> headReference = new AtomicReference<Node>();

    /**
     * Creates a new HeapSnapshotChain .
     *
     * @param heapSnapshot the initial HeapSnapshot.
     * @throws NullPointerException if heapSnapshot is null.
     */
    public MultiversionedHeapSnapshotChain(H heapSnapshot) {
        if (heapSnapshot == null) throw new NullPointerException();
        headReference.set(new Node(heapSnapshot, null));

        new CleanupThread().start();
    }

    /**
     * Adds the newSnapshot to this HeapSnapshotChain. This call is non blocking, so the expectedSnapshot
     * is needed as well, to see if there was a failure while adding.
     *
     * @param expectedSnapshot the HeapSnapshot we expected to be in place.
     * @param newSnapshot      the HeapSnapshot to add.
     * @return true if the HeapSnapshot  was added successfully, false otherwise.
     * @throws NullPointerException     if expected or added is null.
     * @throws IllegalArgumentException if the version of the added is not larger than the version of the expected.
     */
    public boolean compareAndAdd(H expectedSnapshot, H newSnapshot) {
        if (expectedSnapshot == null || newSnapshot == null) throw new NullPointerException();

        Node oldHead = headReference.get();
        H oldSnapshot = oldHead.strongSnapshotReference;
        if (oldSnapshot != expectedSnapshot) {
            //another thread already updated the head since the strong reference already is cleared or updated.
            return false;
        }

        //a sanity check to make sure that snapshots are not inserted in a bad order.
        if (oldSnapshot.getVersion() >= newSnapshot.getVersion()) {
            String msg = format("Can't add snapshot with old version, oldsnaphot.version %s and newSnapshot.version %s",
                    oldSnapshot.getVersion(),
                    newSnapshot.getVersion());
            throw new IllegalArgumentException(msg);
        }

        //lets try to set the newHead. This is done atomically.
        Node newHead = new Node(newSnapshot, oldHead);
        if (!headReference.compareAndSet(oldHead, newHead)) {
            //another threads has done an update, so our set didn't succeeed.
            return false;
        }

        //the new head was set successfully, we can clear the strongSnapshotReference on the oldHead so that
        //the snapshot can be garbage collected if nobody is using it anymore.
        oldHead.strongSnapshotReference = null;
        return true;
    }

    /**
     * Gets the head HeapSnapshot. This reflects the most up to date 'reality'.
     *
     * @return the head Snapshot. The returned value will never be null. Value could be stale as soon as
     *         it is returned.
     */
    public H getHead() {
        //needs to be done in a loop because a new head could be placed, and the MultiversionedHeapSnapshot is garbage collected.
        while (true) {
            H snapshot = headReference.get().softSnapshotReference.get();
            if (snapshot != null)
                return snapshot;
        }
    }

    /**
     * Gets the Snapshot with equal or smaller to the specified version.
     *
     * @param version the version of the Snapshot to look for.
     * @return the found Snapshot.
     * @throws org.codehaus.multiverse.core.BadVersionException
     *          if no Snapshot exists with a version equal or smaller to the specified version.
     */
    public H get(long version) {
        Node node = headReference.get();
        long oldest = -1;
        do {
            H snapshot = node.softSnapshotReference.get();
            //since the snapshot could have been removed by the garbage collector, we need to check that is
            //still is there. If it isn't, we skip this node and go to the next.
            if (snapshot != null) {
                oldest = snapshot.getVersion();

                //if the snapshot has a version that is smaller or equal to the version we are looking for, we are done
                if (snapshot.getVersion() <= version)
                    return snapshot;
            }
            node = node.prev;
        } while (node != null);

        String msg = format("Snapshot with a version equal or smaller than  %s is not found, oldest version found is %s",
                version, oldest);
        throw new BadVersionException(msg);
    }

    /**
     * Get the Snapshot with the specific version.
     *
     * @param version the specific version of the HeapSnapshot to look for.
     * @return the found HeapSnapshot. The value will always be not null
     * @throws IllegalArgumentException if the Snapshot with the specific version is not found.
     */
    public H getSpecific(long version) {
        H snapshot = get(version);
        if (snapshot.getVersion() != version)
            throw new IllegalArgumentException(format("Snapshot with version %s is not found", version));
        return snapshot;
    }

    /**
     * Returns the number of alive Snapshots. Since unused HeapSnapshots are garbage collected, the count
     * should remain a lot smaller than the total number of transactions.
     *
     * @return the number of alive Snapshots.
     */
    public int getAliveCount() {
        int result = 0;
        Node node = headReference.get();
        do {
            if (node.softSnapshotReference.get() != null)
                result++;
            node = node.prev;
        } while (node != null);

        return result;
    }

    class Node {
        final SoftReference<H> softSnapshotReference;
        //strong reference only is used to prevent garbage collection. As soon as the head node,
        //is no head anymore, this reference is set to null.
        volatile H strongSnapshotReference;
        final Node prev;

        Node(H snapshot, Node prev) {
            this.strongSnapshotReference = snapshot;
            this.softSnapshotReference = new SoftReference(snapshot, queue);
            this.prev = prev;
        }
    }

    private ReferenceQueue queue = new ReferenceQueue();

    private final static AtomicInteger gcThreadCounter = new AtomicInteger();

    class CleanupThread extends Thread {

        public CleanupThread() {
            super("RemoveDeadChainEntries-Garbagecollector-Thread: " + gcThreadCounter.incrementAndGet());
            setDaemon(true);
        }

        public void run() {
            while (true) {
                try {
                    queue.remove();
                } catch (InterruptedException e) {
                    interrupt();
                }
            }
        }
    }
}
