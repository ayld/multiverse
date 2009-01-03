package org.codehaus.multiverse.multiversionedstm;

import java.util.concurrent.atomic.AtomicLong;

public final class MultiversionedStmStatistics {

    public final AtomicLong transactionsStartedCount = new AtomicLong();
    public final AtomicLong transactionsCommitedCount = new AtomicLong();
    public final AtomicLong transactionsConflictedCount = new AtomicLong();
    public final AtomicLong transactionsAbortedCount = new AtomicLong();
    public final AtomicLong transactionsReadonlyCount = new AtomicLong();
    public final AtomicLong transactionRetriedCount = new AtomicLong();

    /**
     * Returns the current number of active transactions (so are started, but have not committed, or rolled back). The value
     * is a rough estimation. The returned value will always be larger or equal to zero.
     *
     * @return the number of active transactions.
     */
    public long getActiveCount() {
        //since no locking is done, it could be that content are read from different points in time in the stm.
        long count = getTransactionsStartedCount() - (getTransactionsCommitedCount() + getTransactionsAbortedCount());
        return count < 0 ? 0 : count;
    }

    /**
     * Returns the number of transactions that have aborted.
     *
     * @return the number of transactions that have started.
     */
    public long getTransactionsStartedCount() {
        return transactionsStartedCount.longValue();
    }

    /**
     * Returns the number of transactions that have committed, but were only readonly.
     *
     * @return the number of committed readonly transactions.
     */
    public long getTransactionsReadonlyCount() {
        return transactionsReadonlyCount.longValue();
    }

    /**
     * Returns the number of transactions that have committed.
     *
     * @return the number of transactions that have committed.
     */
    public long getTransactionsCommitedCount() {
        return transactionsCommitedCount.longValue();
    }

    /**
     * Returns the number of transactions that have aborted.
     *
     * @return the number of transactions that have aborted.
     */
    public long getTransactionsAbortedCount() {
        return transactionsAbortedCount.longValue();
    }

    public void renderAsString(StringBuffer sb) {
        sb.append("stm.transaction.activecount: ").append(getActiveCount()).append("\n");
        sb.append("stm.transaction.startedcount: ").append(getTransactionsStartedCount()).append("\n");

        sb.append("stm.transaction.committedcount: ").append(getTransactionsCommitedCount()).append("\n");
        double committedPercentage = (100 * transactionsCommitedCount.longValue()) / transactionsStartedCount.longValue();
        sb.append("stm.transaction.committed-percentage: ").append(committedPercentage).append("\n");

        sb.append("stm.transaction.abortedcount: ").append(getTransactionsAbortedCount()).append("\n");
        double abortedPercentage = ((100.0 * transactionsAbortedCount.longValue()) / transactionsStartedCount.longValue());
        sb.append("stm.transaction.aborted-percentage: ").append(abortedPercentage).append("\n");
        sb.append("stm.transaction.readonlycount: ").append(getTransactionsReadonlyCount()).append("\n");
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        renderAsString(sb);
        return sb.toString();
    }
}
