package org.codehaus.multiverse.multiversionedheap.snapshotchain;

import org.codehaus.multiverse.core.BadVersionException;
import org.codehaus.multiverse.multiversionedheap.MultiversionedHeapSnapshot;

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
 * This HeapSnapshotChain has a non blocking add {@link #compareAndAdd(org.codehaus.multiverse.multiversionedheap.MultiversionedHeapSnapshot , org.codehaus.multiverse.multiversionedheap.MultiversionedHeapSnapshot)}.
 *
 * @author Peter Veentjer
 * @param <H>
 */
public final class MultiversionedHeapSnapshotChain<H extends MultiversionedHeapSnapshot> {

    private final static AtomicInteger gcThreadIdGenerator = new AtomicInteger();

    private final AtomicReference<Node<H>> headReference = new AtomicReference<Node<H>>();

    private final ReferenceQueue<WeakSnapshotReference> referenceQueue = new ReferenceQueue<WeakSnapshotReference>();

    private final AtomicLong cleanupCounter = new AtomicLong();

    /**
     * Creates a new HeapSnapshotChain .
     *
     * @param heapSnapshot the initial HeapSnapshot.
     * @throws NullPointerException if heapSnapshot is null.
     */
    public MultiversionedHeapSnapshotChain(H heapSnapshot) {
        if (heapSnapshot == null) throw new NullPointerException();
        headReference.set(new Node(heapSnapshot, null, referenceQueue));
        //new CleanupThread().start();
    }

    /**
     * Adds the newSnapshot to this HeapSnapshotChain under the condition that the expectedSnapshot
     * still is in place.
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

        if (cleanupCounter.incrementAndGet() % 10000 == 0)
            removeUselessNodesFromChain();

        //the new head was set successfully, we need to signal the oldHead that it isn't the Head anymore.
        oldHead.downgradeToBody(newHead);
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
        //needs to be done in a loop because a new head could be placed, and the MultiversionedHeapSnapshot
        //already is garbage collected. 
        while (true) {
            H snapshot = headReference.get().strongSnapshotReference;
            if (snapshot != null)
                return snapshot;
        }
    }

    /**
     * Gets the Snapshot with equal or smaller to the specified version.
     * <p/>
     * The complexity if this method is O(n). So perhaps this could be improved in the future.
     * But atm it doesn't appear in the profiling charts, so not a big issue yet.
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
            //still is there. If it isn't, we skip this node and go to the newHead.
            if (snapshot != null) {
                oldest = snapshot.getVersion();

                //if the snapshot has a version that is smaller or equal to the version we are looking for, we are done
                if (snapshot.getVersion() <= version)
                    return snapshot;
            }
            node = node.oldHead;
        } while (node != null);

        String msg = format("Snapshot with a version equal or smaller than  %s is not found, oldest version found is %s",
                version, oldest);
        throw new BadVersionException(msg);
    }

    /**
     * Get the Snapshot with the specific version.
     * <p/>
     * It uses the #getVersion to the find the correct Snapshot.
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
            node = node.oldHead;
        } while (node != null);

        return result;
    }

    public int getChainCount() {
        int result = 0;
        Node node = headReference.get();
        do {
            result++;
            node = node.oldHead;
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

        //points to the previous head of the chain.
        volatile Node<H> oldHead;

        //the newHead field only is used for removing the node from the Chain.
        volatile Node<H> newHead;

        Node(H snapshot, Node<H> oldHead, ReferenceQueue<WeakSnapshotReference> referenceQueue) {
            this.strongSnapshotReference = snapshot;
            this.weakSnapshotReference = new WeakSnapshotReference(this, snapshot, referenceQueue);
            this.oldHead = oldHead;
        }

        /**
         * Downgrades this Node from head of the chain, to the body of the chain.
         * <p/>
         * Once a new head is set, the old head should be marked that it isn't head anymore.
         * What happens is that the strong reference to the snapshot is removed. so unless somebody else
         * is refering to the Snapshot, it can be garbage collected.
         * <p/>
         * Once this is done, it should never by redone. No checking is done on this bad behaviour.
         *
         * @param newHead the Node that is the new head of the chain.
         */
        void downgradeToBody(Node<H> newHead) {
            this.newHead = newHead;
            this.strongSnapshotReference = null;
        }

        /**
         * Checks if this node is part of the chain. This is done by checking if the strongSnapshotReference
         * is null.
         *
         * @return true if this Node is part of the Snapshot chain, false otherwise.
         */
        boolean partOfChain() {
            return strongSnapshotReference == null;
        }

        /**
         * Once the WeakSnapshotReference is garbage collected. the cleanup thread is going to call this method.
         * This method removes this Node from the chain of nodes.
         */
        void removeFromChain() {
            //nodes that are created by were not successfully set as head (so are not part of the chain) are garbage
            //collected as well. This check makes sure that the 'cleanup' of such nodes doesn't cause any harm.
            if (!partOfChain())
                return;

            //remove the current node of the chain 
            newHead.oldHead = oldHead;

            //if the older exist, the older.newHead should be set (the last node has no older).
            if (oldHead != null)
                oldHead.newHead = newHead;
        }
    }

    private void removeUselessNodesFromChain() {
        while (true) {
            WeakSnapshotReference reference = (WeakSnapshotReference) referenceQueue.poll();
            if (reference == null)
                return;

            reference.node.removeFromChain();
        }
    }

    class CleanupThread extends Thread {

        public CleanupThread() {
            super("RemoveDeadChainEntries-Garbagecollector-Thread: " + gcThreadIdGenerator.incrementAndGet());
            setDaemon(true);
            setPriority(Thread.MIN_PRIORITY);
        }

        public void run() {
            int removedCounter = 0;

            try {
                while (true) {
                    WeakSnapshotReference reference = (WeakSnapshotReference) referenceQueue.remove();
                    reference.node.removeFromChain();
                    removedCounter++;

                    if (removedCounter % 10000000 == 0) {
                        System.out.println("-------------------------------------------------------");
                        System.out.println(format("removedCounter %s", removedCounter));
                        System.out.println(format("enteredcount   %s", enteredCounter.get()));
                        System.out.println(format("alivecount     %s", getAliveCount()));
                        System.out.println(format("chaincount     %s", getChainCount()));

                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
