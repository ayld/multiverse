package org.codehaus.multiverse.multiversionedstm.growingheap;

import org.codehaus.multiverse.util.NonBlockingStatistics;

import static java.lang.String.format;
import java.util.concurrent.atomic.AtomicLong;

public final class GrowingMultiversionedHeapStatistics {

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

    public final NonBlockingStatistics commitNonBlockingStatistics = new NonBlockingStatistics("heap.commit.nonblocking");

    public final NonBlockingStatistics listenNonBlockingStatistics = new NonBlockingStatistics("heap.listen.nonblocking");

    public final AtomicLong readCount = new AtomicLong();

    public void renderAsString(StringBuffer sb) {
        sb.append(format("heap.commit.readonly %s\n", commitReadonlyCount.longValue()));
        sb.append(format("heap.commit.conflicts %s\n", commitWriteConflictCount.longValue()));
        sb.append(format("heap.commit.succeeded %s\n", commitSuccessCount.longValue()));
        commitNonBlockingStatistics.renderAsString(sb);
        listenNonBlockingStatistics.renderAsString(sb);
        sb.append(format("heap.reads %s\n", readCount));
        sb.append(format("heap.stores %s\n", committedStoreCount.longValue()));
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        renderAsString(sb);
        return sb.toString();
    }
}
