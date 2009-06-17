package org.multiverse.benchmarks.drivers.oldschool.locks;

import org.multiverse.TestThread;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;
import org.multiverse.benchmarkframework.executor.AbstractDriver;
import org.multiverse.benchmarkframework.executor.TestCase;

import java.util.concurrent.locks.ReentrantLock;

/**
 * A {@link org.multiverse.benchmarkframework.executor.Driver} for benchmarking a contended
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
