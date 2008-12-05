package org.codehaus.multiverse.multiversionedstm;

public class HeapCommitResult {
    public boolean success = false;
    public HeapSnapshot snapshpt;
    public long writeCount = 0;

    public static HeapCommitResult createWriteConflict() {
        HeapCommitResult result = new HeapCommitResult();
        result.success = false;
        return result;
    }

    public static HeapCommitResult createReadOnly(HeapSnapshot snapshot) {
        return createSuccess(snapshot, 0);
    }

    public static HeapCommitResult createSuccess(HeapSnapshot snapshot, long writeCount) {
        HeapCommitResult result = new HeapCommitResult();
        result.success = true;
        result.writeCount = writeCount;
        result.snapshpt = snapshot;
        return result;
    }
}
