package org.codehaus.multiverse.multiversionedstm.tests;

import static junit.framework.Assert.assertEquals;
import org.codehaus.multiverse.TestThread;
import static org.codehaus.multiverse.TestUtils.*;
import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.core.TransactionTemplate;
import org.codehaus.multiverse.multiversionedheap.standard.DefaultMultiversionedHeap;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import static org.codehaus.multiverse.multiversionedstm.TransactionMethods.retry;
import org.codehaus.multiverse.multiversionedstm.examples.IntegerValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
public class LargeNumberOfWaitersTest {

    private MultiversionedStm stm;
    private DefaultMultiversionedHeap heap;
    private long waiterLatchHandle;
    private long notifyLatchHandle;

    private int totalWakeupCount = 1000000;
    private int wakeupCount = 1000;
    private int waiterThreadCount = 100;

    private AtomicInteger wakeupCountDown = new AtomicInteger();
    private AtomicInteger notifyCountDown = new AtomicInteger();

    @Before
    public void setUp() {
        heap = new DefaultMultiversionedHeap();
        stm = new MultiversionedStm(heap);
    }

    @After
    public void tearDown() {
        System.out.println(stm.getStatistics());
        System.out.println(heap.getStatistics());
    }

    @Test
    public void test() {
        wakeupCountDown.set(totalWakeupCount);
        notifyCountDown.set(totalWakeupCount);
        waiterLatchHandle = commit(stm, new IntegerValue(0));
        notifyLatchHandle = commit(stm, new IntegerValue(1));

        NotifyThread notifyThread = new NotifyThread(0);
        WaiterThread[] waiterThreads = createWaiterThreads();

        startAll(waiterThreads);
        startAll(notifyThread);

        joinAll(waiterThreads);
        joinAll(notifyThread);

        Transaction t = stm.startTransaction();
        IntegerValue waiterLatch = (IntegerValue) t.read(waiterLatchHandle);
        assertEquals(0, waiterLatch.get());
        IntegerValue notifyLatch = (IntegerValue) t.read(notifyLatchHandle);
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
                    IntegerValue notifyLatch = (IntegerValue) t.read(notifyLatchHandle);
                    if (notifyLatch.get() == 0)
                        retry();
                    notifyLatch.setValue(0);

                    IntegerValue waiterLatch = (IntegerValue) t.read(waiterLatchHandle);
                    waiterLatch.setValue(count);
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
                    IntegerValue waiterLatch = (IntegerValue) t.read(waiterLatchHandle);
                    if (waiterLatch.get() <= 0)
                        retry();
                    waiterLatch.dec();

                    if (waiterLatch.get() == 0) {
                        IntegerValue notifyLatch = (IntegerValue) t.read(notifyLatchHandle);
                        notifyLatch.setValue(1);
                    }
                    return null;
                }
            }.execute();
        }
    }
}
