package org.codehaus.multiverse.multiversionedstm.utils;

import org.codehaus.multiverse.core.BadVersionException;
import org.codehaus.multiverse.multiversionedstm.MultiversionedHeapSnapshot;

import static java.lang.String.format;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
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
 * This HeapSnapshotChain has a non blocking add {@link #compareAndAdd(org.codehaus.multiverse.multiversionedstm.MultiversionedHeapSnapshot , org.codehaus.multiverse.multiversionedstm.MultiversionedHeapSnapshot)}.
 *
 * @author Peter Veentjer
 * @param <H>
 */
public final class MultiversionedHeapSnapshotChain<H extends MultiversionedHeapSnapshot> {

    private final static AtomicInteger gcThreadIdGenerator = new AtomicInteger();

    private final AtomicReference<Node<H>> headReference = new AtomicReference<Node<H>>();

    private final ReferenceQueue<WeakSnapshotReference> referenceQueue = new ReferenceQueue<WeakSnapshotReference>();

    /**
     * Creates a new HeapSnapshotChain .
     *
     * @param heapSnapshot the initial HeapSnapshot.
     * @throws NullPointerException if heapSnapshot is null.
     */
    public MultiversionedHeapSnapshotChain(H heapSnapshot) {
        if (heapSnapshot == null) throw new NullPointerException();
        headReference.set(new Node(heapSnapshot, null, referenceQueue));

        new CleanupThread().start();
    }

    /**
     * Adds the newSnapshot to this HeapSnapshotChain. This call is non blocking, so the expectedSnapshot
     * is needed as well, to see if there was a failure while adding.
     *
     * @param expectedSnapshot the HeapSnapshot we expected to be in place.
     * @param newSnapshot      the HeapSnapshot to add.
     * @return true if the HeapSnapshot was added successfully, false otherwise.
     * @throws NullPointerException     if expected or added is null.
     * @throws IllegalArgumentException if the version of the added is not larger than the version of the expected.
     */
    public boolean compareAndAdd(H expectedSnapshot, H newSnapshot) {
        if (expectedSnapshot == null || newSnapshot == null) throw new NullPointerException();

        Node<H> oldHead = headReference.get();
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
        Node<H> newHead = new Node(newSnapshot, oldHead, referenceQueue);
        if (!headReference.compareAndSet(oldHead, newHead)) {
            //another threads has done an update, so our set didn't succeeed.
            return false;
        }

        //the new head was set successfully, we can clear the strongSnapshotReference on the oldHead so that
        //the snapshot can be garbage collected if nobody is using it anymore.
        oldHead.strongSnapshotReference = null;
        oldHead.newer = newHead;
        enteredCounter.incrementAndGet();
        return true;
    }

    final AtomicLong enteredCounter = new AtomicLong();

    /**
     * Gets the head HeapSnapshot. This reflects the most up to date 'reality'.
     *
     * @return the head Snapshot. The returned value will never be null. Value could be stale as soon as
     *         it is returned.
     */
    public H getHead() {
        //needs to be done in a loop because a new head could be placed, and the MultiversionedHeapSnapshot is garbage collected.
        while (true) {
            H snapshot = headReference.get().weakSnapshotReference.get();
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
        Node<H> node = headReference.get();
        long oldest = -1;
        do {
            H snapshot = node.weakSnapshotReference.get();
            //since the snapshot could have been removed by the garbage collector, we need to check that is
            //still is there. If it isn't, we skip this node and go to the newer.
            if (snapshot != null) {
                oldest = snapshot.getVersion();

                //if the snapshot has a version that is smaller or equal to the version we are looking for, we are done
                if (snapshot.getVersion() <= version)
                    return snapshot;
            }
            node = node.older;
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
            if (node.weakSnapshotReference.get() != null)
                result++;
            node = node.older;
        } while (node != null);

        return result;
    }

    public int getChainCount() {
        int result = 0;
        Node node = headReference.get();
        do {
            result++;
            node = node.older;
        } while (node != null);

        return result;
    }

    static class WeakSnapshotReference<H extends MultiversionedHeapSnapshot> extends WeakReference<H> {
        final Node<H> node;

        WeakSnapshotReference(Node<H> node, H referent, ReferenceQueue queue) {
            super(referent, queue);
            this.node = node;
        }
    }

    static class Node<H extends MultiversionedHeapSnapshot> {
        //if you just need an instance to the snapshotReference, always use this one.
        final WeakSnapshotReference<H> weakSnapshotReference;
        //strong reference only is used to prevent garbage collection. As soon as the head node,
        //is no head anymore, this reference is set to null. So in most cases this will be null.
        volatile H strongSnapshotReference;

        volatile Node<H> older;
        volatile Node<H> newer;

        Node(H snapshot, Node<H> older, ReferenceQueue<WeakSnapshotReference> referenceQueue) {
            this.strongSnapshotReference = snapshot;
            this.weakSnapshotReference = new WeakSnapshotReference(this, snapshot, referenceQueue);
            this.older = older;
        }
    }

    class CleanupThread extends Thread {

        public CleanupThread() {
            super("RemoveDeadChainEntries-Garbagecollector-Thread: " + gcThreadIdGenerator.incrementAndGet());
            setDaemon(true);
            setPriority(Thread.MAX_PRIORITY);
        }

        public void run() {
            int counter = 0;
            int withNewer = 0;
            int withOlder = 0;

            try {
                while (true) {
                    try {
                        WeakSnapshotReference reference = (WeakSnapshotReference) referenceQueue.remove();
                        Node node = reference.node;

                        //a node with a strong reference never has been an element of the chain
                        if (node.strongSnapshotReference != null)
                            return;

                        if (counter % 200000 == 0) {
                            System.out.println("-------------------------------------------------------");
                            System.out.println(format("count %s", counter));
                            System.out.println(format("withNewer %s", withNewer));
                            System.out.println(format("withOlder %s", withOlder));
                            System.out.println(format("alivecount %s", getAliveCount()));
                            System.out.println(format("chaincount %s", getChainCount()));
                            System.out.println(format("enteredcount %s", enteredCounter.get()));
                        }

                        counter++;

                        // System.out.println("garbage found");

                        Node older = node.older;
                        Node newer = node.newer;

                        //todo: could it happen that the newer has not been set when this node is garbage collected?
                        if (newer != null) {
                            newer.older = older;
                            withNewer++;
                        }

                        //the last node has no older, so a check should be done.
                        if (older != null) {
                            withOlder++;
                            older.newer = newer;
                        }

                        node.older = null;
                        node.newer = null;
                    } catch (InterruptedException e) {
                        //todo: what to do with the interrupt status here.
                        interrupt();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
