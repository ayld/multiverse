package org.multiverse.benchmarks.drivers.oldschool.locks;

import org.benchy.TestCaseResult;
import org.benchy.executor.AbstractDriver;
import org.benchy.executor.TestCase;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A {@link org.benchy.executor.Driver} for benchmarking a contended
 * {@link ReentrantLock}.
 * <p/>
 * todo:
 * could the lock/unlock be removed by the jit since it does not provide any value?
 *
 * @author Peter Veentjer
 */
public class ContendedLockDriver extends AbstractDriver {

    private WorkerThread[] threads;
    private int threadCount;
    private long lockAndUnlockCount;
    private ReentrantLock lock;
    private boolean fair;

    @Override
    public void preRun(TestCase testCase) {
        threadCount = testCase.getIntProperty("threadCount");
        lockAndUnlockCount = testCase.getLongProperty("lockAndUnlockCount");
        fair = testCase.getBooleanProperty("fair");
        lock = new ReentrantLock(fair);

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
        public WorkerThread(int id) {
            super("WorkerThreads-" + id);
        }

        @Override
        public void run() {
            for (long k = 0; k < lockAndUnlockCount; k++) {
                lock.lock();
                lock.unlock();
            }
        }
    }
}
