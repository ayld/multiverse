package org.multiverse.multiversionedstm.examples;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.TestUtils;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Originator;
import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionTemplate;
import org.multiverse.multiversionedstm.MultiversionedStm;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"ClassExplicitlyExtendsThread"})
public class QueueIntegrationTest {

    private List<String> producedList = new LinkedList<String>();
    private List<String> consumedList = new LinkedList<String>();

    private AtomicInteger produceCountDown = new AtomicInteger();
    private AtomicInteger consumeCountDown = new AtomicInteger();

    private MultiversionedStm stm;
    private Originator<Queue<String>> queueOriginator;

    private int consumeMaxSleepMs = 10;
    private int produceMaxSleepMs = 10;

    @Before
    public void setUp() throws Exception {
        stm = new MultiversionedStm();
        queueOriginator = commit(stm, new Queue<String>());
    }

    @After
    public void tearDown() throws Exception {
        System.out.println(stm.getStatistics());
    }

    public void atomicPush(final String item) {
        new TransactionTemplate(stm) {
            protected Object execute(Transaction t) throws Exception {
                Queue<String> queue = t.read(queueOriginator);
                queue.push(item);
                return null;
            }
        }.execute();
    }

    public String atomicPop() {
        return new TransactionTemplate<String>(stm) {
            protected String execute(Transaction t) throws Exception {
                Queue<String> queue = t.read(queueOriginator);
                return queue.pop();
            }
        }.execute();
    }

    public int atomicSize() {
        return new TransactionTemplate<Integer>(stm) {
            protected Integer execute(Transaction t) throws Exception {
                Queue<String> queue = t.read(queueOriginator);
                return queue.size();
            }
        }.execute();
    }

    public void asynchronousPush(final String item) {
        new Thread() {
            public void run() {
                atomicPush(item);
            }
        }.start();
    }

    public void asynchronousPop() {
        new Thread() {
            public void run() {
                try {
                    atomicPop();
                    //System.out.println(Thread.currentThread() + " consumed: " + result);
                } catch (RuntimeException ex) {
                    ex.printStackTrace();
                }
            }
        }.start();
    }

    @Test
    public void testSequential() {
        atomicPush("item1");
        atomicPush("item2");

        String result;
        result = atomicPop();
        assertEquals("item1", result);
        result = atomicPop();
        assertEquals("item2", result);
    }

    @Test
    public void test() {
        asynchronousPop();
        asynchronousPop();
        TestUtils.sleep(1000);
        asynchronousPush("foo");
        asynchronousPush("bar");
        TestUtils.sleep(1000);

        //todo: check that content has been returned.
    }

    @Test
    public void testProducerConsumer_1() {
        //new PrintMultiversionedStmStatisticsThread(multiversionedstm).start();

        testProducerConsumer(1);
    }

    @Test
    public void testProducerConsumer_10() {
        testProducerConsumer(10);
    }

    @Test
    public void testProducerConsumer_100() {
        testProducerConsumer(100);
    }

    @Test
    public void testProducerConsumer_1000() {
        testProducerConsumer(1000);
    }

    public void testProducerConsumer(int itemCount) {
        produceCountDown.set(itemCount);
        consumeCountDown.set(itemCount);

        ProducerThread producerThread = new ProducerThread();
        ConsumerThread consumerThread = new ConsumerThread();

        startAll(consumerThread, producerThread);
        joinAll(producerThread, consumerThread);

        assertQueueIsEmpty();
        assertEquals(producedList, consumedList);
    }

    public void assertQueueIsEmpty() {
        Transaction t = stm.startTransaction();
        Queue<String> queue = t.read(queueOriginator);
        assertTrue(queue.isEmpty());
        t.commit();
    }

    private class ProducerThread extends TestThread {

        public ProducerThread() {
            super("ProducerThread");
        }

        private int runCount = 0;

        public void run() {
            while (produceCountDown.getAndDecrement() >= 0) {
                String item = "item" + System.nanoTime();
                producedList.add(item);
                atomicPush(item);

                runCount++;
                if (runCount % 100 == 0)
                    System.out.println(getName() + " transactioncount: " + runCount);

                sleepRandomMs(produceMaxSleepMs);
            }
        }
    }

    private class ConsumerThread extends TestThread {
        private int runCount = 0;

        public ConsumerThread() {
            super("ConsumerThread");
        }

        public void run() {
            while (consumeCountDown.getAndDecrement() >= 0) {
                String item = atomicPop();
                consumedList.add(item);


                runCount++;
                if (runCount % 100 == 0)
                    System.out.println(getName() + " transactioncount: " + runCount);

                sleepRandomMs(consumeMaxSleepMs);
            }
        }
    }
}
