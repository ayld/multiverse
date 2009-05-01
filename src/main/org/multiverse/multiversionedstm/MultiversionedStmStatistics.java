package org.multiverse.multiversionedstm;

import static java.lang.String.format;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An object responsible for storing statistics of the {@link MultiversionedStm}.
 *
 * @author Peter Veentjer.
 */
public final class MultiversionedStmStatistics {

    private final AtomicLong materializedCount = new AtomicLong();
    private final AtomicLong writeCount = new AtomicLong();
    private final AtomicLong transactionStartedCount = new AtomicLong();
    private final AtomicLong transactionAbortedCount = new AtomicLong();
    private final AtomicLong transactionCommittedCount = new AtomicLong();
    private final AtomicLong transactionRetriedCount = new AtomicLong();
    private final AtomicLong transactionReadonlyCount = new AtomicLong();
    private final AtomicLong transactionWriteConflictCount = new AtomicLong();
    private final AtomicLong transactionSnapshotTooOldCount = new AtomicLong();
    private final AtomicLong lockAcquiredCount = new AtomicLong();
    private final AtomicLong transactionLockAcquireFailureCount = new AtomicLong();
    private final AtomicLong transactionPendingRetryCount = new AtomicLong();

    public void incWriteCount() {
        writeCount.incrementAndGet();
    }

    public void incWriteCount(int amount) {
        if (amount < 0)
            throw new IllegalArgumentException();
        writeCount.addAndGet(amount);
    }

    public long getWriteCount() {
        return writeCount.get();
    }

    public long getTransactionPendingRetryCount() {
        return transactionPendingRetryCount.get();
    }

    public void incTransactionPendingRetryCount() {
        transactionPendingRetryCount.incrementAndGet();
    }

    public void decTransactionPendingRetryCount() {
        transactionPendingRetryCount.decrementAndGet();
    }

    public long getTransactionLockAcquireFailureCount() {
        return transactionLockAcquireFailureCount.get();
    }

    public void incTransactionLockAcquireFailureCount() {
        transactionLockAcquireFailureCount.incrementAndGet();
    }

    public void incLockAcquiredCount(int count) {
        if (count < 0)
            throw new IllegalArgumentException();
        lockAcquiredCount.addAndGet(count);
    }

    public long getLockAcquiredCount() {
        return lockAcquiredCount.get();
    }

    public long getTransactionWriteConflictCount() {
        return transactionWriteConflictCount.get();
    }

    public void incTransactionWriteConflictCount() {
        transactionWriteConflictCount.incrementAndGet();
    }

    public long getTransactionSnapshotTooOldCount() {
        return transactionSnapshotTooOldCount.get();
    }

    public void incTransactionSnapshotTooOldCount() {
        transactionSnapshotTooOldCount.incrementAndGet();
    }

    public long getTransactionCommittedCount() {
        return transactionCommittedCount.get();
    }

    public void incTransactionCommittedCount() {
        transactionCommittedCount.incrementAndGet();
    }

    public long getTransactionReadonlyCount() {
        return transactionReadonlyCount.get();
    }

    public void incTransactionReadonlyCount() {
        transactionReadonlyCount.incrementAndGet();
    }

    public long getTransactionRetriedCount() {
        return transactionRetriedCount.get();
    }

    public void incTransactionRetriedCount() {
        transactionRetriedCount.incrementAndGet();
    }

    public void incTransactionStartedCount() {
        transactionStartedCount.incrementAndGet();
    }

    public long getTransactionStartedCount() {
        return transactionStartedCount.get();
    }

    public void incTransactionAbortedCount() {
        transactionAbortedCount.incrementAndGet();
    }

    public long getTransactionAbortedCount() {
        return transactionAbortedCount.get();
    }

    public void incMaterializedCount() {
        materializedCount.incrementAndGet();
    }

    public long getMaterializedCount() {
        return materializedCount.get();
    }

    private float toPercentage(AtomicLong a, AtomicLong b) {
        return (a.get() * 100.0f) / b.get();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(format("multiversionedstm.transaction.started.count %s\n", transactionStartedCount.get()));
        sb.append(format("multiversionedstm.transaction.committed.count %s\n", transactionCommittedCount.get()));
        sb.append(format("multiversionedstm.transaction.committed.percentage %s\n", toPercentage(transactionCommittedCount, transactionStartedCount)));
        sb.append(format("multiversionedstm.transaction.aborted.count %s\n", transactionAbortedCount.get()));
        sb.append(format("multiversionedstm.transaction.aborted.percentage %s\n", toPercentage(transactionAbortedCount, transactionStartedCount)));
        sb.append(format("multiversionedstm.transaction.readonly.count %s\n", transactionReadonlyCount.get()));
        sb.append(format("multiversionedstm.transaction.readonly.percentage %s\n", toPercentage(transactionReadonlyCount, transactionCommittedCount)));
        sb.append(format("multiversionedstm.transaction.retried.count %s\n", transactionRetriedCount.get()));
        sb.append(format("multiversionedstm.transaction.retried.percentage %s\n", toPercentage(transactionRetriedCount, transactionStartedCount)));
        sb.append(format("multiversionedstm.transaction.pendingretry.count %s\n", transactionPendingRetryCount.get()));
        sb.append(format("multiversionedstm.transaction.writeconflict.count %s\n", transactionWriteConflictCount.get()));
        sb.append(format("multiversionedstm.transaction.writeconflict.percentage %s\n", toPercentage(transactionWriteConflictCount, transactionStartedCount)));
        sb.append(format("multiversionedstm.transaction.snapshottooold.count %s\n", transactionSnapshotTooOldCount.get()));
        sb.append(format("multiversionedstm.transaction.snapshottooold.percentage %s\n", toPercentage(transactionSnapshotTooOldCount, transactionStartedCount)));
        sb.append(format("multiversionedstm.transaction.failedtoacquirelocks.count %s\n", transactionLockAcquireFailureCount.get()));
        sb.append(format("multiversionedstm.transaction.failedtoacquirelocks.percentage %s\n", toPercentage(transactionLockAcquireFailureCount, transactionStartedCount)));
        sb.append(format("multiversionedstm.write.count %s\n", writeCount.get()));
        sb.append(format("multiversionedstm.materialized.count %s\n", materializedCount.get()));
        sb.append(format("multiversionedstm.lockacquired.count %s\n", lockAcquiredCount.get()));
        return sb.toString();
    }

}
