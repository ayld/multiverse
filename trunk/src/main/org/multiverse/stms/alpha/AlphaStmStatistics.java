package org.multiverse.stms.alpha;

import static java.lang.String.format;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An object responsible for storing the statistics for a {@link AlphaStm}.
 *
 * @author Peter Veentjer.
 */
public final class AlphaStmStatistics {
    private final AtomicLong loadCount = new AtomicLong();
    private final AtomicLong attachNewCount = new AtomicLong();
    private final AtomicLong writeCount = new AtomicLong();
    private final AtomicLong updateTransactionStartedCount = new AtomicLong();
    private final AtomicLong updateTransactionAbortedCount = new AtomicLong();
    private final AtomicLong updateTransactionEmptyCommittedCount = new AtomicLong();
    private final AtomicLong updateTransactionCommittedCount = new AtomicLong();
    private final AtomicLong updateTransactionRetriedCount = new AtomicLong();
    private final AtomicLong updateTransactionReadonlyCount = new AtomicLong();
    private final AtomicLong updateTransactionWriteConflictCount = new AtomicLong();
    private final AtomicLong updateTransactionSnapshotTooOldCount = new AtomicLong();
    private final AtomicLong updateTransactionLockAcquireFailureCount = new AtomicLong();
    private final AtomicLong updateTransactionPendingRetryCount = new AtomicLong();
    private final AtomicLong lockAcquiredCount = new AtomicLong();
    private final AtomicLong readonlyTransactionStartedCount = new AtomicLong();
    private final AtomicLong readonlyTransactionCommittedCount = new AtomicLong();
    private final AtomicLong readonlyTransactionAbortedCount = new AtomicLong();

    public void incReadonlyTransactionCommittedCount() {
        readonlyTransactionCommittedCount.incrementAndGet();
    }

    public long getReadonlyTransactionCommittedCount() {
        return readonlyTransactionCommittedCount.get();
    }

    public void incReadonlyTransactionAbortedCount() {
        readonlyTransactionAbortedCount.incrementAndGet();
    }

    public long getReadonlyTransactionAbortedCount() {
        return readonlyTransactionAbortedCount.get();
    }

    public void incReadonlyTransactionStartedCount() {
        readonlyTransactionStartedCount.incrementAndGet();
    }

    public long getReadonlyTransactionStartedCount() {
        return readonlyTransactionStartedCount.get();
    }

    public void incWriteCount() {
        writeCount.incrementAndGet();
    }

    public void incWriteCount(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException();
        }
        writeCount.addAndGet(amount);
    }

    public long getWriteCount() {
        return writeCount.get();
    }

    public long getAttachNewCount() {
        return attachNewCount.get();
    }

    public void incAttachNewCount() {
        attachNewCount.incrementAndGet();
    }

    public long getUpdateTransactionPendingRetryCount() {
        return updateTransactionPendingRetryCount.get();
    }

    public void incTransactionPendingRetryCount() {
        updateTransactionPendingRetryCount.incrementAndGet();
    }

    public void decTransactionPendingRetryCount() {
        updateTransactionPendingRetryCount.decrementAndGet();
    }

    public long getUpdateTransactionLockAcquireFailureCount() {
        return updateTransactionLockAcquireFailureCount.get();
    }

    public void incTransactionFailedToAcquireLocksCount() {
        updateTransactionLockAcquireFailureCount.incrementAndGet();
    }

    public void incLockAcquiredCount(int count) {
        if (count < 0) {
            throw new IllegalArgumentException();
        }
        lockAcquiredCount.addAndGet(count);
    }

    public long getLockAcquiredCount() {
        return lockAcquiredCount.get();
    }

    public long getUpdateTransactionWriteConflictCount() {
        return updateTransactionWriteConflictCount.get();
    }

    public void incTransactionWriteConflictCount() {
        updateTransactionWriteConflictCount.incrementAndGet();
    }

    public long getUpdateTransactionSnapshotTooOldCount() {
        return updateTransactionSnapshotTooOldCount.get();
    }

    public void incTransactionSnapshotTooOldCount() {
        updateTransactionSnapshotTooOldCount.incrementAndGet();
    }

    public long getUpdateTransactionCommittedCount() {
        return updateTransactionCommittedCount.get();
    }

    public void incTransactionCommittedCount() {
        updateTransactionCommittedCount.incrementAndGet();
    }

    public long getUpdateTransactionReadonlyCount() {
        return updateTransactionReadonlyCount.get();
    }

    public void incTransactionReadonlyCount() {
        updateTransactionReadonlyCount.incrementAndGet();
    }

    public long getUpdateTransactionRetriedCount() {
        return updateTransactionRetriedCount.get();
    }

    public void incTransactionRetriedCount() {
        updateTransactionRetriedCount.incrementAndGet();
    }

    public void incTransactionStartedCount() {
        updateTransactionStartedCount.incrementAndGet();
    }

    public long getUpdateTransactionStartedCount() {
        return updateTransactionStartedCount.get();
    }

    public void incTransactionAbortedCount() {
        updateTransactionAbortedCount.incrementAndGet();
    }

    public long getTransactionEmptyCommitCount() {
        return updateTransactionEmptyCommittedCount.get();
    }

    public void incTransactionEmptyCommitCount() {
        updateTransactionEmptyCommittedCount.incrementAndGet();
    }

    public long getUpdateTransactionAbortedCount() {
        return updateTransactionAbortedCount.get();
    }

    public void incLoadCount() {
        loadCount.incrementAndGet();
    }

    public long getLoadCount() {
        return loadCount.get();
    }

    private float toPercentage(AtomicLong a, AtomicLong b) {
        return toPercentage(a.get(), b.get());
    }

    private float toPercentage(long a, long b) {
        return (a * 100.0f) / b;
    }

    public void print() {
        System.out.println(toString());
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        long committedCount = updateTransactionCommittedCount.get() + readonlyTransactionCommittedCount.get();

        sb.append(format("stm.readonlytransaction.percentage %s\n", toPercentage(readonlyTransactionCommittedCount.get(), committedCount)));
        sb.append(format("stm.readonlytransaction.started.count %s\n", readonlyTransactionStartedCount.get()));
        sb.append(format("stm.readonlytransaction.committed.count %s\n", readonlyTransactionCommittedCount.get()));
        sb.append(format("stm.readonlytransaction.committed.percentage %s\n", toPercentage(readonlyTransactionCommittedCount, readonlyTransactionStartedCount)));
        sb.append(format("stm.readonlytransaction.aborted.percentage %s\n", toPercentage(readonlyTransactionAbortedCount, readonlyTransactionStartedCount)));

        sb.append(format("stm.readonlytransaction.committed.percentage %s \n", toPercentage(readonlyTransactionCommittedCount.get(), committedCount)));
        sb.append(format("stm.readonlypercentage %s\n", toPercentage(readonlyTransactionStartedCount.get(), readonlyTransactionStartedCount.get() + updateTransactionStartedCount.get())));

        sb.append(format("stm.updatetransaction.started.count %s\n", updateTransactionStartedCount.get()));
        sb.append(format("stm.updatetransaction.committed.count %s\n", updateTransactionCommittedCount.get()));
        sb.append(format("stm.updatetransaction.committed.percentage %s\n", toPercentage(updateTransactionCommittedCount, updateTransactionStartedCount)));
        sb.append(format("stm.updatetransaction.committed.empty.count %s\n", updateTransactionEmptyCommittedCount.get()));
        sb.append(format("stm.updatetransaction.committed.empty.percentage %s\n", toPercentage(updateTransactionEmptyCommittedCount, updateTransactionCommittedCount)));
        sb.append(format("stm.updatetransaction.aborted.count %s\n", updateTransactionAbortedCount.get()));
        sb.append(format("stm.updatetransaction.aborted.percentage %s\n", toPercentage(updateTransactionAbortedCount, updateTransactionStartedCount)));
        sb.append(format("stm.updatetransaction.readonly.count %s\n", updateTransactionReadonlyCount.get()));
        sb.append(format("stm.updatetransaction.readonly.percentage %s\n", toPercentage(updateTransactionReadonlyCount, updateTransactionCommittedCount)));
        sb.append(format("stm.updatetransaction.retried.count %s\n", updateTransactionRetriedCount.get()));
        long totalCount = updateTransactionReadonlyCount.get() + updateTransactionStartedCount.get();
        sb.append(format("stm.updatetransaction.retried.percentage %s\n", toPercentage(updateTransactionRetriedCount.get(), totalCount)));
        sb.append(format("stm.updatetransaction.pendingretry.count %s\n", updateTransactionPendingRetryCount.get()));
        sb.append(format("stm.updatetransaction.writeconflict.count %s\n", updateTransactionWriteConflictCount.get()));
        sb.append(format("stm.updatetransaction.writeconflict.percentage %s\n", toPercentage(updateTransactionWriteConflictCount, updateTransactionStartedCount)));
        sb.append(format("stm.updatetransaction.snapshottooold.count %s\n", updateTransactionSnapshotTooOldCount.get()));
        sb.append(format("stm.updatetransaction.snapshottooold.percentage %s\n", toPercentage(updateTransactionSnapshotTooOldCount, updateTransactionStartedCount)));
        sb.append(format("stm.updatetransaction.failedtoacquirelocks.count %s\n", updateTransactionLockAcquireFailureCount.get()));
        sb.append(format("stm.updatetransaction.failedtoacquirelocks.percentage %s\n", toPercentage(updateTransactionLockAcquireFailureCount, updateTransactionStartedCount)));
        sb.append(format("stm.write.count %s\n", writeCount.get()));
        sb.append(format("stm.write.percentage %s\n", toPercentage(writeCount.get(), loadCount.get() + attachNewCount.get())));
        sb.append(format("stm.attachNew.count %s\n", attachNewCount.get()));
        sb.append(format("stm.load.count %s\n", loadCount.get()));
        sb.append(format("stm.lockacquired.count %s\n", lockAcquiredCount.get()));
        return sb.toString();
    }

}
