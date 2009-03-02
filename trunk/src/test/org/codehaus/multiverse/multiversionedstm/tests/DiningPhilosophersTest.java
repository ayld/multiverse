package org.codehaus.multiverse.multiversionedstm.tests;

import org.codehaus.multiverse.TestThread;
import static org.codehaus.multiverse.TestUtils.*;
import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.core.TransactionTemplate;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import static org.codehaus.multiverse.multiversionedstm.TransactionMethods.retry;
import org.codehaus.multiverse.multiversionedstm.examples.IntegerValue;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The cause of the dining philosopers problem is that the take of the left and right fork are not atomic. So it
 * could happen that all philosopers have their left fork, but won't get the right for because the philosopher sitting
 * right to them has that fork.
 * <p/>
 * Within the MultiversionedStm both forks are aquired atomically (so a philosopher gets them both, or won't
 * get them at all). The fork is simulated by a IntegerValue (0 indicates no fork, 1 indicates fork available).
 * The forks are retrieved in 1 transaction, and are returned in another.
 *
 * @author Peter Veentjer.
 */
public class DiningPhilosophersTest {
    private int forkCount = 10;
    private int attemptCount = 1000;

    private final AtomicLong countDown = new AtomicLong();

    private MultiversionedStm stm;
    private long[] forkHandles;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void test() {
        countDown.set(attemptCount);
        createForks();

        PhilosoperThread[] philosoperThreads = createPhilosoperThreads();
        startAll(philosoperThreads);
        joinAll(philosoperThreads);

        assertAllForksHaveReturned();

        for (PhilosoperThread thread : philosoperThreads)
            System.out.println("count: " + thread.successCount);
    }

    public void assertAllForksHaveReturned() {
        Transaction t = stm.startTransaction();
        for (long forkHandle : forkHandles) {
            IntegerValue fork = (IntegerValue) t.read(forkHandle);
            assertEquals(1, fork.get());
        }
        t.commit();
    }

    public PhilosoperThread[] createPhilosoperThreads() {
        PhilosoperThread[] threads = new PhilosoperThread[forkCount];
        for (int k = 0; k < forkCount; k++) {
            long leftForkHandle = forkHandles[k];
            long rightForkHandle = k == forkCount - 1 ? forkHandles[0] : forkHandles[k + 1];
            threads[k] = new PhilosoperThread(leftForkHandle, rightForkHandle);
        }
        return threads;
    }

    public void createForks() {
        forkHandles = new long[forkCount];
        Transaction t = stm.startTransaction();
        for (int k = 0; k < forkHandles.length; k++)
            forkHandles[k] = t.attachAsRoot(new IntegerValue(1));

        t.commit();
    }

    static AtomicInteger philosoperThreadIdGenerator = new AtomicInteger();

    class PhilosoperThread extends TestThread {
        private final long leftForkHandle;
        private final long rightForkHandle;
        private volatile long successCount = 0;

        PhilosoperThread(long leftForkHandle, long rightForkHandle) {
            super("PhilosoperThread-" + philosoperThreadIdGenerator.incrementAndGet());
            this.leftForkHandle = leftForkHandle;
            this.rightForkHandle = rightForkHandle;

            System.out.println("left: " + leftForkHandle + " right: " + rightForkHandle);
        }

        @Override
        public void run() {
            while (countDown.decrementAndGet() >= 0)
                eat();
        }

        public void eat() {
            obtainForks();

            //simulate the eating
            sleepRandomMs(50);

            returnForks();
            successCount++;
        }

        private void returnForks() {
            new TransactionTemplate(stm) {
                @Override
                protected Boolean execute(Transaction t) throws Exception {
                    returnFork(t, leftForkHandle);
                    returnFork(t, rightForkHandle);
                    return null;
                }
            }.execute();
        }

        public void obtainForks() {
            new TransactionTemplate(stm) {
                @Override
                protected Boolean execute(Transaction t) throws Exception {
                    obtainFork(t, leftForkHandle);
                    obtainFork(t, rightForkHandle);
                    return null;
                }
            }.execute();
        }

        private void returnFork(Transaction t, long forkHandle) {
            IntegerValue fork = (IntegerValue) t.read(forkHandle);
            fork.setValue(1);
        }

        private void obtainFork(Transaction t, long forkHandle) {
            IntegerValue fork = (IntegerValue) t.read(forkHandle);
            if (fork.get() == 0)
                retry();
            fork.setValue(0);
        }
    }
}
