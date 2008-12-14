package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.core.BadVersionException;

import static java.lang.String.format;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class HeapSnapshotChain<H extends HeapSnapshot> {

    private final AtomicReference<Node> headReference = new AtomicReference<Node>();

    public HeapSnapshotChain(H heapSnapshot) {
        if (headReference == null) throw new NullPointerException();
        headReference.set(new Node(heapSnapshot, null));

        new PrinterThread().start();
    }

    public boolean compareAndAdd(H expected, H added) {
        if (expected == null || added == null) throw new NullPointerException();
        if (added.getVersion() <= expected.getVersion())
            throw new IllegalArgumentException();

        Node oldHead = headReference.get();
        if (oldHead.strongReferenceToSnapshot != expected)
            return false;

        Node newHead = new Node(added, oldHead);
        if (!headReference.compareAndSet(oldHead, newHead))
            return false;

        oldHead.strongReferenceToSnapshot = null;
        return true;
    }

    public H getHead() {
        return headReference.get().weakReferenceToSnapshot.get();
    }

    /**
     * Gets the Snapshot with equal or smaller to the specified version.
     *
     * @param version the version of the Snapshot to look for.
     * @return the found Snapshot.
     * @throws org.codehaus.multiverse.core.BadVersionException
     *          if no Snapshot exists with a version equal or smaller to the specified version.
     */
    public H getSnapshot(long version) {
        Node node = headReference.get();
        long oldest = -1;
        do {
            H snapshot = node.weakReferenceToSnapshot.get();
            if (snapshot != null) {
                oldest = snapshot.getVersion();

                if (snapshot.getVersion() <= version)
                    return snapshot;
            }
            node = node.next;
        } while (node != null);

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
    public H getSpecificSnapshot(long version) {
        H snapshot = getSnapshot(version);
        if (snapshot.getVersion() != version)
            throw new IllegalArgumentException(format("Snapshot with version %s is not found", version));
        return snapshot;
    }

    public int getSnapshotAliveCount() {
        int result = 0;
        Node node = headReference.get();
        do {
            if (node.weakReferenceToSnapshot.get() != null)
                result++;
            node = node.next;
        } while (node != null);

        return result;
    }

    class Node {
        final WeakReference<H> weakReferenceToSnapshot;
        volatile H strongReferenceToSnapshot;
        final Node next;

        Node(H snapshot, Node next) {
            this.weakReferenceToSnapshot = new WeakReference(snapshot, queue);
            this.next = next;
            this.strongReferenceToSnapshot = snapshot;
        }
    }

    private ReferenceQueue queue = new ReferenceQueue();

    private final static AtomicInteger gcThreadCounter = new AtomicInteger();

    class PrinterThread extends Thread {

        public PrinterThread() {
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
