package org.multiverse.multiversionedstm.tests;

import static junit.framework.Assert.assertEquals;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Handle;
import org.multiverse.api.StmUtils;
import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionTemplate;
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.multiversionedstm.examples.ExampleIntValue;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * A test that check if the system is able to deal with large amount of waiters.
 * <p/>
 * The test: there are 2 integervalue's in the stm: one containing the number of wakeup signals, and
 * one containing the number of notify signals. The notify thread will place a number of wakeup signals in the
 * wakeup latch and sets the notifylatch to zero. Once the transaction completes, the waiterthreads will
 * all wake up and decrease the waiterlatch until it reaches zero. Once it reaches zero, the notify latch
 * will be set to 1 so that a notify thread will store new waiters.
 *
 * @author Peter Veentjer.
 */
public class LargeNumberOfWaitersLongTest {

    private MultiversionedStm stm;
    private Handle<ExampleIntValue> waiterLatchHandle;
    private Handle<ExampleIntValue> notifyLatchHandle;

    private int totalWakeupCount = 1000000;
    private int wakeupCount = 1000;
    private int waiterThreadCount = 20;

    private AtomicInteger wakeupCountDown = new AtomicInteger();
    private AtomicInteger notifyCountDown = new AtomicInteger();

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
        //new PrintMultiversionedStmStatisticsThread(stm).start();
    }

    @After
    public void tearDown() {
        System.out.println(stm.getStatistics());
    }

    @Test
    public void test() {
        wakeupCountDown.set(totalWakeupCount);
        notifyCountDown.set(totalWakeupCount);
        waiterLatchHandle = commit(stm, new ExampleIntValue(0));
        notifyLatchHandle = commit(stm, new ExampleIntValue(1));

        System.out.println(stm.getStatistics());

        NotifyThread notifyThread = new NotifyThread(0);
        WaiterThread[] waiterThreads = createWaiterThreads();

        startAll(waiterThreads);
        startAll(notifyThread);

        joinAll(waiterThreads);
        joinAll(notifyThread);

        Transaction t = stm.startTransaction();
        ExampleIntValue waiterLatch = t.read(waiterLatchHandle);
        assertEquals(0, waiterLatch.get());
        ExampleIntValue notifyLatch = t.read(notifyLatchHandle);
        assertEquals(1, notifyLatch.get());
        t.commit();
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
        public void run() {
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

        public void wakeup(final int count) {
            new TransactionTemplate(stm) {
                protected Object execute(Transaction t) throws Exception {
                    ExampleIntValue notifyLatch = t.read(notifyLatchHandle);
                    if (notifyLatch.get() == 0)
                        StmUtils.retry();
                    notifyLatch.set(0);

                    ExampleIntValue waiterLatch = t.read(waiterLatchHandle);
                    waiterLatch.set(count);
                    return null;
                }
            }.execute();
        }
    }

    class WaiterThread extends TestThread {
        public WaiterThread(int id) {
            super("WaiterThread-" + id);
        }

        @Override
        public void run() {
            while (wakeupCountDown.getAndDecrement() > 0)
                doWait();

            System.out.println(getName() + " is finished");
        }

        public void doWait() {
            //System.out.println("wait");
            new TransactionTemplate(stm) {
                protected Object execute(Transaction t) throws Exception {
                    ExampleIntValue waiterLatch = t.read(waiterLatchHandle);
                    if (waiterLatch.get() <= 0)
                        StmUtils.retry();
                    waiterLatch.dec();

                    if (waiterLatch.get() == 0) {
                        ExampleIntValue notifyLatch = t.read(notifyLatchHandle);
                        notifyLatch.set(1);
                    }
                    return null;
                }
            }.execute();
        }
    }
}
