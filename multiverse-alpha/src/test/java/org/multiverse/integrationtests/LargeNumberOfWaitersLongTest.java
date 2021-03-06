package org.multiverse.integrationtests;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;
import static org.multiverse.api.StmUtils.retry;
import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.datastructures.refs.IntRef;
import static org.multiverse.api.ThreadLocalTransaction.setThreadLocalTransaction;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * A test that check if the system is able to deal with large amount of waiters.
 * <p/>
 * The test: there are 2 integervalue's in the stm: one containing the number of wakeup signals, and
 * one containing the number of notify signals. The notify thread will place a number of wakeup signals in the
 * wakeup latch and sets the notifylatch to zero. Once the transaction completes, the waiterthreads will
 * all wake up and decrease the waiterlatch until it reaches zero. Once it reaches zero, the notify latch
 * will be set to 1 so that a notify thread will storeAndReleaseLock new waiters.
 *
 * @author Peter Veentjer.
 */
public class LargeNumberOfWaitersLongTest {

    private IntRef waiterLatch;
    private IntRef notifyLatch;

    private int totalWakeupCount = 1000000;
    private int wakeupCount = 1000;
    private int waiterThreadCount = 20;

    private AtomicInteger wakeupCountDown = new AtomicInteger();
    private AtomicInteger notifyCountDown = new AtomicInteger();

    @Before
    public void setUp() {
        setThreadLocalTransaction(null);
    }

    @After
    public void tearDown() {
        // stm.getProfiler().print();
    }

    @Test
    public void test() {
        wakeupCountDown.set(totalWakeupCount);
        notifyCountDown.set(totalWakeupCount);
        waiterLatch = new IntRef(0);
        notifyLatch = new IntRef(1);

        //System.out.println(stm.getStatistics());

        NotifyThread notifyThread = new NotifyThread(0);
        WaiterThread[] waiterThreads = createWaiterThreads();

        startAll(waiterThreads);
        startAll(notifyThread);

        joinAll(waiterThreads);
        joinAll(notifyThread);

        assertEquals(0, waiterLatch.get());
        assertEquals(1, notifyLatch.get());
    }

    private WaiterThread[] createWaiterThreads() {
        WaiterThread[] threads = new WaiterThread[waiterThreadCount];
        for (int k = 0; k < threads.length; k++)
            threads[k] = new WaiterThread(k);
        return threads;
    }

    class NotifyThread extends TestThread {
        public NotifyThread(int id) {
            super("NotifyThread-" + id);
        }

        @Override
        public void doRun() {
            int wakeupCount;
            while ((wakeupCount = checkout()) > 0) {
                wakeup(wakeupCount);
            }
        }

        public int checkout() {
            int result;

            boolean succes;
            do {
                int remaining = notifyCountDown.get();

                if (remaining >= wakeupCount) {
                    result = wakeupCount;
                } else {
                    result = remaining;
                }
                succes = notifyCountDown.compareAndSet(remaining, remaining - result);
            } while (!succes);

            return result;
        }

        @AtomicMethod
        public void wakeup(final int count) {
            if (notifyLatch.get() == 0) {
                retry();
            }
            notifyLatch.set(0);
            waiterLatch.set(count);
        }
    }

    class WaiterThread extends TestThread {
        public WaiterThread(int id) {
            super("WaiterThread-" + id);
        }

        @Override
        public void doRun() {
            while (wakeupCountDown.getAndDecrement() > 0)
                doWait();

            System.out.println(getName() + " is finished");
        }

        @AtomicMethod
        public void doWait() {
            if (waiterLatch.get() <= 0) {
                retry();
            }
            waiterLatch.dec();

            if (waiterLatch.get() == 0) {
                notifyLatch.set(1);
            }
        }
    }
}
