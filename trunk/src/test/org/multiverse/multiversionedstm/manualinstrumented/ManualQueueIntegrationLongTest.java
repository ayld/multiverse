package org.multiverse.multiversionedstm.manualinstrumented;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionTemplate;
import org.multiverse.multiversionedstm.MultiversionedStm;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"ClassExplicitlyExtendsThread"})
public class ManualQueueIntegrationLongTest {

    private List<String> producedList = new LinkedList<String>();
    private List<String> consumedList = new LinkedList<String>();

    private AtomicInteger produceCountDown = new AtomicInteger();
    private AtomicInteger consumeCountDown = new AtomicInteger();

    private MultiversionedStm stm;
    private Handle<ManualQueue<String>> queueHandle;

    private int consumeMaxSleepMs = 10;
    private int produceMaxSleepMs = 10;

    @Before
    public void setUp() throws Exception {
        stm = new MultiversionedStm();
        queueHandle = commit(stm, new ManualQueue<String>());
    }

    @After
    public void tearDown() throws Exception {
        System.out.println(stm.getStatistics());
    }

    public void atomicPush(final String item) {
        new TransactionTemplate(stm) {
            protected Object execute(Transaction t) throws Exception {
                ManualQueue<String> queue = t.read(queueHandle);
                queue.push(item);
                return null;
            }
        }.execute();
    }

    public String atomicPop() {
        return new TransactionTemplate<String>(stm) {
            protected String execute(Transaction t) throws Exception {
                ManualQueue<String> queue = t.read(queueHandle);
                return queue.pop();
            }
        }.execute();
    }

    public int atomicSize() {
        return new TransactionTemplate<Integer>(stm) {
            protected Integer execute(Transaction t) throws Exception {
                ManualQueue<String> queue = t.read(queueHandle);
                return queue.size();
            }
        }.execute();
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
    public void testProducerConsumer_1() {
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
        ManualQueue<String> queue = t.read(queueHandle);
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
            super("QueueThread");
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
