package org.multiverse.multiversionedstm.tests;

import static org.junit.Assert.assertFalse;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Handle;
import static org.multiverse.api.StmUtils.retry;
import static org.multiverse.api.TransactionThreadLocal.getTransaction;
import org.multiverse.api.annotations.Atomic;
import org.multiverse.api.annotations.TmEntity;

import java.util.concurrent.atomic.AtomicInteger;
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
@SuppressWarnings({"VolatileLongOrDoubleField", "ArithmeticOnVolatileField"})
public class DiningPhilosophersLongTest {
    private int forkCount = 10;
    private int attemptCount = 1000;

    private final AtomicLong countDown = new AtomicLong();

    private Handle<Fork>[] forkHandles;

    @TmEntity
    private static class Fork {
        private boolean isUsed;

        Fork() {
            isUsed = false;
        }

        public boolean isUsed() {
            return isUsed;
        }

        public void release() {
            isUsed = false;
        }

        public void take() {
            if (isUsed)
                retry();
            isUsed = true;
        }
    }

    @Test
    public void test() {
        countDown.set(attemptCount);
        createForks();

        PhilosopherThread[] philosoperThreads = createPhilosoperThreads();
        startAll(philosoperThreads);
        joinAll(philosoperThreads);

        assertAllForksHaveReturned();

        for (PhilosopherThread thread : philosoperThreads)
            System.out.println("count: " + thread.successCount);
    }

    @Atomic
    public void assertAllForksHaveReturned() {
        for (Handle<Fork> handle : forkHandles) {
            Fork fork = getTransaction().read(handle);
            assertFalse(fork.isUsed());
        }
    }

    public PhilosopherThread[] createPhilosoperThreads() {
        PhilosopherThread[] threads = new PhilosopherThread[forkCount];
        for (int k = 0; k < forkCount; k++) {
            Handle<Fork> leftForkHandle = forkHandles[k];
            Handle<Fork> rightForkHandle = k == forkCount - 1 ? forkHandles[0] : forkHandles[k + 1];
            threads[k] = new PhilosopherThread(leftForkHandle, rightForkHandle);
        }
        return threads;
    }

    @Atomic
    public void createForks() {
        forkHandles = new Handle[forkCount];
        for (int k = 0; k < forkHandles.length; k++) {
            forkHandles[k] = getTransaction().attach(new Fork());
        }
    }

    static AtomicInteger philosoperThreadIdGenerator = new AtomicInteger();

    class PhilosopherThread extends TestThread {
        private final Handle<Fork> leftForkHandle;
        private final Handle<Fork> rightForkHandle;
        private volatile long successCount = 0;

        PhilosopherThread(Handle<Fork> leftForkHandle, Handle<Fork> rightForkHandle) {
            super("PhilosopherThread-" + philosoperThreadIdGenerator.incrementAndGet());
            this.leftForkHandle = leftForkHandle;
            this.rightForkHandle = rightForkHandle;
        }

        @Override
        public void run() {
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

        @Atomic
        public void releaseForks() {
            Fork leftFork = getTransaction().read(leftForkHandle);
            leftFork.release();

            Fork rightFork = getTransaction().read(rightForkHandle);
            rightFork.release();
        }

        @Atomic
        public void takeForks() {
            Fork leftFork = getTransaction().read(leftForkHandle);
            leftFork.take();

            Fork rightFork = getTransaction().read(rightForkHandle);
            rightFork.take();
        }
    }
}
