package org.multiverse.multiversionedstm.tests;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Handle;
import org.multiverse.api.StmUtils;
import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionTemplate;
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.multiversionedstm.examples.IntegerValue;

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
@SuppressWarnings({"VolatileLongOrDoubleField", "ArithmeticOnVolatileField"})
public class DiningPhilosophersTest {
    private int forkCount = 10;
    private int attemptCount = 1000;

    private final AtomicLong countDown = new AtomicLong();

    private MultiversionedStm stm;
    private Handle<IntegerValue>[] forkHandles;

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
        for (Handle<IntegerValue> handle : forkHandles) {
            IntegerValue fork = t.read(handle);
            assertEquals(1, fork.get());
        }
        t.commit();
    }

    public PhilosoperThread[] createPhilosoperThreads() {
        PhilosoperThread[] threads = new PhilosoperThread[forkCount];
        for (int k = 0; k < forkCount; k++) {
            Handle<IntegerValue> leftForkHandle = forkHandles[k];
            Handle<IntegerValue> rightForkHandle = k == forkCount - 1 ? forkHandles[0] : forkHandles[k + 1];
            threads[k] = new PhilosoperThread(leftForkHandle, rightForkHandle);
        }
        return threads;
    }

    public void createForks() {
        forkHandles = new Handle[forkCount];
        Transaction t = stm.startTransaction();
        for (int k = 0; k < forkHandles.length; k++) {
            forkHandles[k] = t.attach(new IntegerValue(1));
        }

        t.commit();
    }

    static AtomicInteger philosoperThreadIdGenerator = new AtomicInteger();

    class PhilosoperThread extends TestThread {
        private final Handle<IntegerValue> leftForkHandle;
        private final Handle<IntegerValue> rightForkHandle;
        private volatile long successCount = 0;

        PhilosoperThread(Handle<IntegerValue> leftForkHandle, Handle<IntegerValue> rightForkHandle) {
            super("PhilosoperThread-" + philosoperThreadIdGenerator.incrementAndGet());
            this.leftForkHandle = leftForkHandle;
            this.rightForkHandle = rightForkHandle;
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

        private void returnFork(Transaction t, Handle<IntegerValue> forkHandle) {
            IntegerValue fork = t.read(forkHandle);
            fork.set(1);
        }

        private void obtainFork(Transaction t, Handle<IntegerValue> forkHandle) {
            IntegerValue fork = t.read(forkHandle);
            if (fork.get() == 0)
                StmUtils.retry();
            fork.set(0);
        }
    }
}
