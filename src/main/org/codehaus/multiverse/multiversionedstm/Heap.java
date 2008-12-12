package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.util.iterators.ResetableIterator;
import org.codehaus.multiverse.util.latches.Latch;

/**
 * All methods can be called concurrently.
 * <p/>
 * Garbage collection:
 * each cell can be marked as root. If a cell is marked as root, or wen a cell is reachable from a cell marked as root
 * it has to remain. Otherwise it can be garbage collected. Mark and sweep algorithm could be used.
 * <p/>
 *
 * Deletion:
 * Deletion is not 'possible', just like it isn;t possible with Java. An object gets deleted when it can't be references
 * directly or indirectly from one of the roots. This heap has the same semantics as normal Java Heap. Objects can
 * be made root and as long as objects can be reached from the root, they are not garbage collected.
 *
 * @author Peter Veentjer.
 */
public interface Heap {

    /**
     * Gets the Snapshot of the active state of the Heap. As soon as the snapshot is returned,
     * it could be that it is outdated. The returned value will never be null.
     *
     * Method is threadsafe.
     *
     * @return the current snapshot of the Heap.
     */
    HeapSnapshot getActiveSnapshot();

    /**
     * Gets a snapshot of some state of the Heap. If exact version doesn't exist, an older version
     * is returned.
     * <p/>
     * todo: what to do if there doesn't exist a snapshot with a version equal or smaller than
     * the specified version.
     *
     * Method is threadsafe.
     *
     * @param version the version of the snapshot to search for.
     * @return the found HeapSnapshot.
     */
    HeapSnapshot getSnapshot(long version);

    /**
     * Creates a new handle that can be used to find an object (it could be compared to an alloc function).
     * <p/>
     * Method is threadsafe.
     * <p/>
     * It could be that handles are created, but eventually are not written to main memory. An example is a
     * transaction that is aborted.
     *
     * @return the created handle.
     */
    long createHandle();

    /**
     * Writes changes to the heap. The write has to be 'atomic', so it should not be allowed that a snapshot
     * sees the updates being made.
     *
     * Method is threadsafe.
     *
     * @param startVersion the version of heap when the transaction begin. This information is needed for
     *                     detecting writeconflicts.
     * @param changes      a resetable iterator over the
     * @return the HeapCommitResult. This object contains information regarding the
     */
    HeapCommitResult commit(long startVersion, ResetableIterator<DehydratedStmObject> changes);

    /**
     * Creates a {@link Latch} that is opened when an update is done on one of the handles.
     * <p/>
     * When a transaction does a retry, an update on one of the handles a transaction has read, could
     * lead to a succeeding execution. So the transaction listens to the heap, and when the heap does
     * a write on one of those handles, the latch is opened and the transaction can retry.
     * 
     * Method is threadsafe.
     *
     * todo: the transactionVersion can't be higher than the actual version of the heap.
     *
     * @param latch the Latch to register. This latch is openened when the desired update has occurred.
     * @param handles an array of handles to listen to.
     * @param transactionVersion the highest version of the reads that were of no value to the transaction.
     * @throws NullPointerException if latch or handles is null.
     */
    void listen(Latch latch, long[] handles, long transactionVersion);

    /**
     * Returns the number of alive Snapshots. When a snapshot is not needed anymore, it can be garbage
     * collected, so it is removed from the alived set of Snapshots. The number will always be equal or
     * larger than 1. The returned value could be stale as soon as it is received.
     *
     * Method is threadsafe.
     *
     * @return the number of alive Snapshots.
     */
    int getSnapshotAliveCount();
}

