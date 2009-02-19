package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.util.iterators.ResetableIterator;
import org.codehaus.multiverse.util.latches.Latch;

/**
 * Contains the actual content of a {@link MultiversionedStm}.
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
public interface MultiversionedHeap {

    /**
     * Gets the Snapshot of the active state of the Heap. As soon as the snapshot is returned,
     * it could be that it is outdated. The returned value will never be null.
     * <p/>
     * Method is threadsafe.
     *
     * @return the current snapshot of the Heap.
     */
    MultiversionedHeapSnapshot getActiveSnapshot();

    /**
     * Gets a snapshot of some state of the Heap. If exact version doesn't exist, an older version
     * is returned.
     * <p/>
     * todo: what to do if there doesn't exist a snapshot with a version equal or smaller than
     * the specified version.
     * <p/>
     * Method is threadsafe.
     *
     * @param version the version of the snapshot to search for.
     * @return the found HeapSnapshot.
     */
    MultiversionedHeapSnapshot getSnapshot(long version);

    /**
     * Writes changes to the heap. The write has to be 'atomic', so it should not be allowed that a snapshot
     * sees the updates being made.
     * <p/>
     * Method is threadsafe.
     *
     * @param startVersion the version of heap when the transaction begin. This information is needed for
     *                     detecting writeconflicts.
     * @param changes      a resetable iterator over the
     * @return the HeapCommitResult. This object contains information regarding the
     */
    CommitResult commit(long startVersion, ResetableIterator<DehydratedStmObject> changes);

    /**
     * Creates a {@link Latch} that is opened when an update is done on one of the handles.
     * <p/>
     * When a transaction does a retry, an update on one of the handles a transaction has read, could
     * lead to a succeeding execution. So the transaction listens to the heap, and when the heap does
     * a write on one of those handles, the latch is opened and the transaction can retry.
     * <p/>
     * Method is threadsafe.
     * <p/>
     * todo: the transactionVersion can't be higher than the actual version of the heap.
     *
     * @param latch              the Latch to register. This latch is openened when the desired update has occurred.
     * @param handles            an array of handles to listen to.
     * @param transactionVersion the highest version of the reads that were of no value to the transaction.
     * @throws NullPointerException if latch or handles is null.
     */
    void listen(Latch latch, long[] handles, long transactionVersion);

    /**
     * Returns the number of alive Snapshots. When a snapshot is not needed anymore, it can be garbage
     * collected, so it is removed from the alived set of Snapshots. The number will always be equal or
     * larger than 1. The returned value could be stale as soon as it is received.
     * <p/>
     * Method is threadsafe.
     *
     * @return the number of alive Snapshots.
     */
    int getSnapshotAliveCount();

    public final class CommitResult {

        public static CommitResult createWriteConflict() {
            return new CommitResult(false, null, 0);
        }

        public static CommitResult createReadOnly(MultiversionedHeapSnapshot snapshot) {
            if (snapshot == null) throw new NullPointerException();
            return new CommitResult(true, snapshot, 0);
        }

        public static CommitResult createSuccess(MultiversionedHeapSnapshot snapshot, long writeCount) {
            if (snapshot == null) throw new NullPointerException();
            if (writeCount < 0) throw new IllegalArgumentException();
            return new CommitResult(true, snapshot, writeCount);
        }

        private final boolean success;
        private final MultiversionedHeapSnapshot snapshot;
        private final long writeCount;

        private CommitResult(boolean success, MultiversionedHeapSnapshot snapshot, long writeCount) {
            this.success = success;
            this.snapshot = snapshot;
            this.writeCount = writeCount;
        }

        /**
         * True indicates it was a success, false indicates a
         *
         * @return
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * Returns the MultiversionedHeapSnapshot that is the result of commit. If the commit was not a success.
         * the returned value is null.
         *
         * @return
         */
        public MultiversionedHeapSnapshot getSnapshot() {
            return snapshot;
        }

        /**
         * Return the number of writes that have been done. The value will always be equal or larger than zero.
         * If this CommitResult was a success, and the writecount was 0, it was a readonly transaction.
         *
         * @return
         */
        public long getWriteCount() {
            return writeCount;
        }
    }
}

