package org.multiverse.integrationtests;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import org.multiverse.api.Stm;
import static org.multiverse.api.StmUtils.retry;
import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.datastructures.refs.IntRef;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

import java.util.concurrent.atomic.AtomicLong;

/**
 * The cause of the dining philosophers problem is that the take of the left and right fork are not atomic. So it
 * could happen that all philosopers have their left fork, but won't get the right for because the philosopher sitting
 * right to them has that fork.
 * <p/>
 * Within the MultiversionedStm both forks are aquired atomically (so a philosopher gets them both, or won't
 * get them at all).
 *
 * @author Peter Veentjer.
 */
public class DiningPhilosophersLongTest {
    private int forkCount = 10;
    private int attemptCount = 1000;

    private final AtomicLong countDown = new AtomicLong();

    private IntRef[] forks;
    private Stm stm;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        setThreadLocalTransaction(null);
    }

    @After
    public void tearDown() {
        //   stm.getProfiler().print();
    }

    @Test
    public void test() {
        countDown.set(attemptCount);
        createForks();

        PhilosopherThread[] philosopherThreads = createPhilosoperThreads();
        startAll(philosopherThreads);
        joinAll(philosopherThreads);

        assertAllForksHaveReturned();
    }

    public void assertAllForksHaveReturned() {
        for (IntRef fork : forks) {
            assertEquals(0, fork.get());
        }
    }

    public PhilosopherThread[] createPhilosoperThreads() {
        PhilosopherThread[] threads = new PhilosopherThread[forkCount];
        for (int k = 0; k < forkCount; k++) {
            IntRef leftFork = forks[k];
            IntRef rightFork = k == forkCount - 1 ? forks[0] : forks[k + 1];
            threads[k] = new PhilosopherThread(k, leftFork, rightFork);
        }
        return threads;
    }

    public void createForks() {
        forks = new IntRef[forkCount];
        for (int k = 0; k < forks.length; k++) {
            forks[k] = new IntRef(0);
        }
    }

    class PhilosopherThread extends TestThread {
        private final IntRef leftFork;
        private final IntRef rightFork;
        private volatile long successCount = 0;

        PhilosopherThread(int id, IntRef leftFork, IntRef rightFork) {
            super("PhilosopherThread-" + id);
            this.leftFork = leftFork;
            this.rightFork = rightFork;
        }

        @Override
        public void doRun() {
            while (countDown.decrementAndGet() >= 0) {
                eat();
                successCount++;
            }
        }

        public void eat() {
            takeForks();
            stuffHole();
            releaseForks();
        }

        private void stuffHole() {
            //simulate the eating
            sleepRandomMs(50);
        }

        @AtomicMethod
        public void releaseForks() {
            leftFork.dec();
            rightFork.dec();
        }

        @AtomicMethod
        public void takeForks() {
            if (leftFork.get() == 1) {
                retry();
            } else {
                leftFork.inc();
            }

            if (rightFork.get() == 1) {
                retry();
            } else {
                rightFork.inc();
            }
        }
    }
}
