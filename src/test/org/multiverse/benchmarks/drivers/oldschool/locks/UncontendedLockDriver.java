package org.multiverse.benchmarks.drivers.oldschool.locks;

import org.benchy.executor.AbstractDriver;
import org.benchy.executor.TestCase;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * todo: watch out with lock elesion and escape analysis.
 *
 * @author Peter Veentjer
 */
public class UncontendedLockDriver extends AbstractDriver {

    private WorkerThread[] threads;
    private int threadCount;
    private long lockAndUnlockCount;
    private boolean fair;

    @Override
    public void preRun(TestCase testCase) {
        threadCount = testCase.getIntProperty("threadCount");
        lockAndUnlockCount = testCase.getLongProperty("lockAndUnlockCount");
        fair = testCase.getBooleanProperty("fair");

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
        private final Lock lock = new ReentrantLock(fair);

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
