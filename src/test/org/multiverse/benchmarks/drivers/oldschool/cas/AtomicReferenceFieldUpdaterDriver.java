package org.multiverse.benchmarks.drivers.oldschool.cas;

import org.benchy.TestCaseResult;
import org.benchy.executor.AbstractDriver;
import org.benchy.executor.TestCase;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @author Peter Veentjer
 */
public class AtomicReferenceFieldUpdaterDriver extends AbstractDriver {

    private IncrementAndGetThread[] threads;
    private AtomicReferenceFieldUpdater cas;
    private int threadCount;
    private long count;

    @Override
    public void preRun(TestCase testCase) {
        cas = AtomicReferenceFieldUpdater.newUpdater(null, null, null);
        threadCount = testCase.getIntProperty("threadCount");
        count = testCase.getLongProperty("count");

        threads = new IncrementAndGetThread[threadCount];
        for (int k = 0; k < threadCount; k++) {
            threads[k] = new IncrementAndGetThread(k);
        }
    }

    @Override
    public void postRun(TestCaseResult caseResult) {
        long transactions = count * threadCount;
        long durationNs = caseResult.getLongProperty("duration(ns)");
        double transactionSec = (TimeUnit.SECONDS.toNanos(1) * transactions / durationNs);
        caseResult.put("transactions/second", transactionSec);
    }

    @Override
    public void run() {
        startAll(threads);
        joinAll(threads);
    }

    private class IncrementAndGetThread extends TestThread {

        public IncrementAndGetThread(int id) {
            super("IncrementAndGetThread-" + id);
        }

        @Test
        public void run() {
            for (long k = 0; k < count; k++) {
                // cas.compareAndSet();
            }
        }
    }
}
