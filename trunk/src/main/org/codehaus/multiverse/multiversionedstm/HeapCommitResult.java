package org.codehaus.multiverse.multiversionedstm;

/**
 * The result of a {@link MultiversionedHeap#commit(long, org.codehaus.multiverse.util.iterators.ResetableIterator)}.
 * The fields are public since it is just a simple data container.
 *
 * @author Peter Veentjer.
 */
public final class HeapCommitResult {
    public boolean success = false;
    public MultiversionedHeapSnapshot snapshot;
    public long writeCount = 0;

    public static HeapCommitResult createWriteConflict() {
        HeapCommitResult result = new HeapCommitResult();
        result.success = false;
        return result;
    }

    public static HeapCommitResult createReadOnly(MultiversionedHeapSnapshot snapshot) {
        return createSuccess(snapshot, 0);
    }

    public static HeapCommitResult createSuccess(MultiversionedHeapSnapshot snapshot, long writeCount) {
        HeapCommitResult result = new HeapCommitResult();
        result.success = true;
        result.writeCount = writeCount;
        result.snapshot = snapshot;
        return result;
    }
}
