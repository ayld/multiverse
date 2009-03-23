package org.codehaus.multiverse.multiversionedheap.standard;

import org.codehaus.multiverse.utils.NonBlockingStatistics;

import static java.lang.String.format;
import java.util.concurrent.atomic.AtomicLong;

public final class DefaultMultiversionedHeapStatistics {

    public final AtomicLong commitTotalCount = new AtomicLong();
    //the total number of commits that have been attempted on this heap.
    //the total number of retries that were needed to do a commit because of the non blocking approach this
    //growing heap uses.
    //the total number of commits that have failed because of commit conflicts with other transactions
    public final AtomicLong commitWriteConflictCount = new AtomicLong();
    //the number of successfully committed commits.
    public final AtomicLong commitSuccessCount = new AtomicLong();
    //the individual (each stm object seperate) writes to the heap that have successfully been committed.
    public final AtomicLong committedStoreCount = new AtomicLong();

    public final AtomicLong commitReadonlyCount = new AtomicLong();

    public final AtomicLong pessimisticLocksAcquired = new AtomicLong();

    public final NonBlockingStatistics commitNonBlockingStatistics = new NonBlockingStatistics("heap.commit.nonblocking");

    public final NonBlockingStatistics listenNonBlockingStatistics = new NonBlockingStatistics("heap.listen.nonblocking");

    public final AtomicLong readCount = new AtomicLong();

    public void renderAsString(StringBuffer sb) {
        sb.append(format("heap.commit.total %s\n", commitTotalCount.longValue()));
        sb.append(format("heap.commit.readonly %s\n", commitReadonlyCount.longValue()));
        double readonlyPercentage = 100.0 * commitReadonlyCount.get() / commitTotalCount.longValue();
        sb.append(format("heap.commit.readonly-percentage %s\n", readonlyPercentage));

        sb.append(format("heap.commit.writeconflicts %s\n", commitWriteConflictCount.longValue()));
        double conflictPercentage = (100.0 * commitWriteConflictCount.longValue() / commitTotalCount.longValue());

        sb.append(format("heap.commit.writeconflict-percentage %s\n", conflictPercentage));
        sb.append(format("heap.commit.succeeded %s\n", commitSuccessCount.longValue()));

        commitNonBlockingStatistics.renderAsString(sb);
        listenNonBlockingStatistics.renderAsString(sb);
        sb.append(format("heap.reads %s\n", readCount));
        sb.append(format("heap.stores %s\n", committedStoreCount.longValue()));
        sb.append(format("heap.pessimisticlocks.acquired %s\n", pessimisticLocksAcquired.longValue()));
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        renderAsString(sb);
        return sb.toString();
    }
}
