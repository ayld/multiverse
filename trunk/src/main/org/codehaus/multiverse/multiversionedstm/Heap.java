package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.util.latches.Latch;
import org.codehaus.multiverse.util.iterators.ResetableIterator;

/**
 *
 *
 * All methods can be called concurrently.
 *
 * Garbage collection:
 * each cell can be marked as root. If a cell is marked as root, or wen a cell is reachable from a cell marked as root
 * it has to remain. Otherwise it can be garbage collected. Mark and sweep algorithm could be used.
 *
 * Deletion:
 * Deletion of an object explicitly is hard because you don't know which other cells have a reference to that cell. It
 * is easier to remove the reference or to unmark the object as root. The garbage collector can take care..
 *
 * Problem:
 * the heap contains cells, but at the moment it isn't possible to find which references a cell has. The heap can
 * be parametrized with everything. So perhaps the E should be made stricter to DehydratedCitizen. And this interface
 * has to be expanded to find the dependencies. 
 * 
 *
  * @author Peter Veentjer.
 */
public interface Heap {

    /**
     * Gets the Snapshot of the current state of the Heap. As soon as the snapshot is returned,
     * it could be that it is outdated. The returned value will always be not null.
     *
     * @return the current snapshot of the Heap.
     */
    HeapSnapshot getSnapshot();

    /**
     * Gets a snapshot of some state of the Heap. If exact version doesn't exist, an older version
     * is returned.
     *
     * todo: what to do if there doesn't exist a snapshot with a version equal or smaller than
     * the specified version.
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
     * Writes changes to the heap
     *
     * todo:
     * instead of returning a long. a more complex object could be returned containing the states
     * of the write (success, failure etc) but also statistics, or information about the write conflicts.
     *
     * @param changes a resetable iterator over the
     * @return the version under which the changes where committed. -1 if there was a write conflict.
     */
    long write(long startVersion, ResetableIterator<DehydratedStmObject> changes);

    /**
     * Creates a {@link Latch} that is opened when a write is done on of the handle.
     *
     * When a transaction does a retry, an update on one of the handles a transaction has read, could
     * lead to a succeeding execution. So the transaction listens to the heap, and when the heap does
     * a write on one of those handles, the latch is opened and the transaction can retry.
     *
     * todo:
     * instead of returning a latch, let the caller give a latch. This way the caller can decide
     * to use a cheaplatch of no timing is needed, or to use a more expensive latch when timing is needed.
     *
     * <p/>
     * Method is threadsafe.
     *
     * @param transactionVersion the highest version of the reads that were of no value to the transaction.
     * @param handles
     * @return the created Latch.
     */
    void listen(Latch latch, long[] handles, long transactionVersion);

    void signalVersionDied(long version);
}
