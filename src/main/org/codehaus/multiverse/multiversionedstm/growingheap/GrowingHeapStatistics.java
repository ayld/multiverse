package org.codehaus.multiverse.multiversionedstm.growingheap;

import static java.lang.String.format;
import java.util.concurrent.atomic.AtomicLong;

public class GrowingHeapStatistics {

    //the total number of commits that have been attempted on this heap.
    private final AtomicLong commitTriesCount = new AtomicLong();
    //the total number of retries that were needed to do a commit because of the non blocking approach this
    //growing heap uses.
    private final AtomicLong commitRetryCount = new AtomicLong();
    //the total number of commits that have failed because of write conflicts with other transactions
    private final AtomicLong commitWriteConflictCount = new AtomicLong();
    //the number of successfully committed commits.
    private final AtomicLong commitSuccessCount = new AtomicLong();
    //the individual (each stm object seperate) writes to the heap that have successfully been committed.
    private final AtomicLong committedStoreCount = new AtomicLong();

    public void incCommitTriesCount() {
        commitTriesCount.incrementAndGet();
    }

    public long getCommitTriesCount(){
        return commitTriesCount.longValue();
    }

    public void incCommitRetryCount() {
        commitRetryCount.incrementAndGet();
    }

    public long getCommitRetryCount(){
        return commitRetryCount.longValue();
    }

    public void incCommitWriteConlictCount() {
        commitWriteConflictCount.incrementAndGet();
    }

    public long getCommitWriteConflictCount(){
        return commitWriteConflictCount.longValue();
    }

    public void incCommittedStoreCount(long count) {
        committedStoreCount.addAndGet(count);
    }

    public long getCommittedStoreCount(){
        return committedStoreCount.longValue();
    }

    public void incCommitSuccessCount(){
        commitSuccessCount.incrementAndGet();
    }

    public long getCommitSuccessCount(){
        return commitSuccessCount.longValue();
    }

    public void renderAsString(StringBuffer sb) {
        sb.append(format("heap.commit.tries %s\n", commitTriesCount.longValue()));
        sb.append(format("heap.commit.conflicts %s\n", commitWriteConflictCount.longValue()));
        sb.append(format("heap.commit.retries %s\n", commitRetryCount.longValue()));
        double writeRetryPercentage = (100.0d * commitRetryCount.longValue()) / commitTriesCount.longValue();
        sb.append(format("heap.commit.retry.percentage %s\n", writeRetryPercentage));
        sb.append(format("heap.commit.succeeded %s\n", commitSuccessCount.longValue()));
        sb.append(format("heap.store.count %s\n", committedStoreCount.longValue()));
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        renderAsString(sb);
        return sb.toString();
    }
}
