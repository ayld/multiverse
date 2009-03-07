package org.codehaus.multiverse.multiversionedheap;

import org.codehaus.multiverse.util.iterators.ResetableIterator;
import org.codehaus.multiverse.util.latches.Latch;

/**
 * A multiversioned Heap that can be used to contain the content stores in an stm.
 * <p/>
 * All methods can be called concurrently.
 * <p/>
 * Garbage collection:
 * each cell can be marked as root. If a cell is marked as root, or wen a cell is reachable from a cell marked as root
 * it has to remain. Otherwise it can be garbage collected. Mark and sweep algorithm could be used.
 * <p/>
 * <p/>
 * Deletion:
 * Deletion is not 'possible', just like it isn;t possible with Java. An object gets deleted when it can't be references
 * directly or indirectly from one of the roots. This heap has the same semantics as normal Java Heap. Objects can
 * be made root and as long as objects can be reached from the root, they are not garbage collected.
 *
 * @author Peter Veentjer.
 */
public interface MultiversionedHeap<I extends Deflated, D extends Deflatable> {

    /**
     * Gets the Snapshot of the active state of the Heap. As soon as the snapshot is returned,
     * it could be that it is outdated. The returned value will never be null.
     * <p/>
     * Method is threadsafe.
     *
     * @return the current snapshot of the Heap.
     */
    MultiversionedHeapSnapshot<I> getActiveSnapshot();

    /**
     * Gets a snapshot of some state of the Heap. If the exact version doesn't exist, an older version
     * is returned.
     * <p/>
     * Method is threadsafe.
     *
     * @param version the version of the snapshot to search for.
     * @return the found HeapSnapshot.
     * @throws org.codehaus.multiverse.core.BadVersionException
     *          if no snapshot is found with a  version equal or lower than version.
     */
    MultiversionedHeapSnapshot<I> getSnapshot(long version);

    /**
     * Commits changes to the heap. The write atomic to be 'atomic', so it should not be allowed that a partial
     * commit is observed.
     * <p/>
     * Method is threadsafe.
     * <p/>
     * The commit doesn't check for duplicate Deflatables in the ResetableIterator. This is responsibility
     * of the layer on top of the MultiversionedHeap  (probably the STM). When duplicate items are committed,
     * behavior is undefined.
     *
     * @param startSnapshot the snapshot of heap when the transaction begin. This information is needed
     *                      for detecting writeconflicts.
     * @param changes       a resetable iterator over the all deflatables that need to be written.
     * @return the CommitResult. This object contains information regarding the commit.
     */
    CommitResult commit(MultiversionedHeapSnapshot<I> startSnapshot, ResetableIterator<D> changes);

    /**
     * Creates a {@link Latch} that is opened when an update is done on one of the handles.
     * <p/>
     * When a transaction does a retry, an update on one of the handles a transaction has read, could
     * lead to a succeeding execution. So the transaction listens to the heap, and when the heap does
     * a write on one of those handles, the latch is opened and the transaction can retry.
     * <p/>
     * Method is threadsafe.
     * <p/>
     * <p/>
     * todo: PLongIterator or PLongIteratorFactory should be used.
     *
     * @param startSnapshot
     * @param latch         the Latch to register. This latch is openened when the desired update has occurred.
     * @param handles       an array of handles to listen to.
     * @throws NullPointerException if latch or handles is null.
     * @throws org.codehaus.multiverse.core.NoProgressPossibleException
     *                              if handles is an empty array
     */
    void listen(MultiversionedHeapSnapshot<I> startSnapshot, Latch latch, long[] handles);

    public final class CommitResult {

        public static CommitResult createWriteConflict() {
            return new CommitResult(false, null, 0);
        }

        public static CommitResult createReadOnly(MultiversionedHeapSnapshot snapshot) {
            assert snapshot != null;
            return new CommitResult(true, snapshot, 0);
        }

        public static CommitResult createSuccess(MultiversionedHeapSnapshot snapshot, int writeCount) {
            assert snapshot != null && writeCount > 0;
            return new CommitResult(true, snapshot, writeCount);
        }

        private final boolean success;
        private final MultiversionedHeapSnapshot snapshot;
        private final int writeCount;

        private CommitResult(boolean success, MultiversionedHeapSnapshot snapshot, int writeCount) {
            this.success = success;
            this.snapshot = snapshot;
            this.writeCount = writeCount;
        }

        /**
         * True indicates it was a success, false indicates a write conflict.
         *
         * @return true indicates if the commit was a success, false otherwise.
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * Returns the MultiversionedHeapSnapshot that is the result of commit. If the commit was not a success.
         * the returned value is null.
         *
         * @return the resulting MultiversionedHeapSnapshot.
         */
        public MultiversionedHeapSnapshot getSnapshot() {
            return snapshot;
        }

        /**
         * Return the number of writes that have been done. The value will always be equal or larger than zero.
         * If this CommitResult was a success, and the writecount was 0, it was a readonly transaction.
         *
         * @return the number of writes that have been done.
         */
        public int getWriteCount() {
            return writeCount;
        }
    }
}

