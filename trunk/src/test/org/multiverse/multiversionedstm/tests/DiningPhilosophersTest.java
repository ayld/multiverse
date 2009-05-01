package org.multiverse.multiversionedstm.tests;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Originator;
import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionTemplate;
import org.multiverse.multiversionedstm.MultiversionedStm;
import static org.multiverse.multiversionedstm.MultiversionedStmUtils.retry;
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
    private Originator<IntegerValue>[] forkOriginators;

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
        for (Originator originator : forkOriginators) {
            IntegerValue fork = (IntegerValue) t.read(originator);
            assertEquals(1, fork.get());
        }
        t.commit();
    }

    public PhilosoperThread[] createPhilosoperThreads() {
        PhilosoperThread[] threads = new PhilosoperThread[forkCount];
        for (int k = 0; k < forkCount; k++) {
            Originator leftForkHandle = forkOriginators[k];
            Originator rightForkHandle = k == forkCount - 1 ? forkOriginators[0] : forkOriginators[k + 1];
            threads[k] = new PhilosoperThread(leftForkHandle, rightForkHandle);
        }
        return threads;
    }

    public void createForks() {
        forkOriginators = new Originator[forkCount];
        Transaction t = stm.startTransaction();
        for (int k = 0; k < forkOriginators.length; k++) {
            forkOriginators[k] = t.attach(new IntegerValue(1));
        }

        t.commit();
    }

    static AtomicInteger philosoperThreadIdGenerator = new AtomicInteger();

    class PhilosoperThread extends TestThread {
        private final Originator leftForkOriginator;
        private final Originator rightForkOriginator;
        private volatile long successCount = 0;

        PhilosoperThread(Originator leftForkOriginator, Originator rightForkOriginator) {
            super("PhilosoperThread-" + philosoperThreadIdGenerator.incrementAndGet());
            this.leftForkOriginator = leftForkOriginator;
            this.rightForkOriginator = rightForkOriginator;
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
                    returnFork(t, leftForkOriginator);
                    returnFork(t, rightForkOriginator);
                    return null;
                }
            }.execute();
        }

        public void obtainForks() {
            new TransactionTemplate(stm) {
                @Override
                protected Boolean execute(Transaction t) throws Exception {
                    obtainFork(t, leftForkOriginator);
                    obtainFork(t, rightForkOriginator);
                    return null;
                }
            }.execute();
        }

        private void returnFork(Transaction t, Originator forkOriginator) {
            IntegerValue fork = (IntegerValue) t.read(forkOriginator);
            fork.set(1);
        }

        private void obtainFork(Transaction t, Originator forkOriginator) {
            IntegerValue fork = (IntegerValue) t.read(forkOriginator);
            if (fork.get() == 0)
                retry();
            fork.set(0);
        }
    }
}
