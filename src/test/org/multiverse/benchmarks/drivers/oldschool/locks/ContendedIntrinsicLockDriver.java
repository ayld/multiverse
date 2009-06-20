package org.multiverse.benchmarks.drivers.oldschool.locks;

import org.benchy.TestCaseResult;
import org.benchy.executor.AbstractDriver;
import org.benchy.executor.TestCase;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;

import java.util.concurrent.TimeUnit;

/**
 * A {@link org.benchy.executor.Driver} for benchmarking the a contended intrinsic
 * lock.
 * <p/>
 * todo:
 * could the empty synchronized block be removed by the JIT?
 *
 * @author Peter Veentjer
 */
public class ContendedIntrinsicLockDriver extends AbstractDriver {

    private WorkerThread[] threads;
    private int threadCount;
    private long lockAndUnlockCount;

    @Override
    public void preRun(TestCase testCase) {
        threadCount = testCase.getIntProperty("threadCount");
        lockAndUnlockCount = testCase.getLongProperty("lockAndUnlockCount");

        threads = new WorkerThread[threadCount];
        for (int k = 0; k < threadCount; k++) {
            threads[k] = new WorkerThread(k);
        }
    }

    @Override
    public void run() {
        startAll(threads);
        joinAll(threads);
    }

    @Override
    public void postRun(TestCaseResult caseResult) {
        long transactions = lockAndUnlockCount * threadCount;
        long durationNs = caseResult.getLongProperty("duration(ns)");
        double transactionSec = (TimeUnit.SECONDS.toNanos(1) * transactions / durationNs);
        caseResult.put("transactions/second", transactionSec);
    }

    public class WorkerThread extends TestThread {
        private final Object lock = new Object();

        public WorkerThread(int id) {
            super("WorkerThreads-" + id);
        }

        @Override
        public void run() {
            for (long k = 0; k < lockAndUnlockCount; k++) {
                synchronized (lock) {
                }
            }
        }
    }
}
