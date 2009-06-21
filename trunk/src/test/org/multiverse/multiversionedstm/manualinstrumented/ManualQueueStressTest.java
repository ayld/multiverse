package org.multiverse.multiversionedstm.manualinstrumented;

import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionTemplate;
import org.multiverse.multiversionedstm.MultiversionedStm;

import java.util.concurrent.atomic.AtomicLong;

public class ManualQueueStressTest {

    private String item = "foo";

    private int producerCount = 1;
    private int consumerCount = 1;
    private int itemCount;

    private Handle queueHandle;

    private long startMs;
    private long endMs;

    private AtomicLong produceCountDown = new AtomicLong();
    private AtomicLong consumeCountDown = new AtomicLong();

    private MultiversionedStm stm;

    @Before
    public void setUp() throws Exception {
        stm = new MultiversionedStm();
        this.queueHandle = commit(stm, new ManualQueue(100000));

        //    new PrintMultiversionedStmStatisticsThread(stm).start();
    }

    @After
    public void tearDown() throws Exception {
        System.out.println(stm.getStatistics());

        long timeMs = endMs - startMs;
        System.out.printf("tranfer of %s items took %s ms\n", itemCount, timeMs);
        System.out.printf("%s transactions/second\n", (stm.getStatistics().getTransactionCommittedCount() / (timeMs / 1000.0)));
    }

    @Test
    public void testProducerConsumer_100() {
        testProducerConsumer(100);
    }

    @Test
    public void testProducerConsumer_100000() {
        testProducerConsumer(100000);
    }

    @Test
    public void testProducerConsumer_1000000() {
        testProducerConsumer(1000000);
    }

    //@Test
    public void testProducerConsumer_5000000() {
        testProducerConsumer(50000000);
    }

    public void testProducerConsumer(int itemCount) {
        this.itemCount = itemCount;
        produceCountDown.set(itemCount);
        consumeCountDown.set(itemCount);

        ProducerThread[] producerThreads = createProducerThreads();
        ConsumerThread[] consumerThreads = createConsumerThreads();

        startMs = System.currentTimeMillis();

        startAll(consumerThreads);
        startAll(producerThreads);

        joinAll(consumerThreads);
        joinAll(producerThreads);

        endMs = System.currentTimeMillis();

        assertQueueIsEmpty();
    }

    public void assertQueueIsEmpty() {
        Transaction t = stm.startTransaction();
        ManualQueue queue = (ManualQueue) t.read(queueHandle);
        assertTrue(queue.isEmpty());
        t.commit();
    }

    public ProducerThread[] createProducerThreads() {
        ProducerThread[] threads = new ProducerThread[producerCount];
        for (int k = 0; k < threads.length; k++)
            threads[k] = new ProducerThread(k);
        return threads;
    }

    public ConsumerThread[] createConsumerThreads() {
        ConsumerThread[] threads = new ConsumerThread[consumerCount];
        for (int k = 0; k < threads.length; k++)
            threads[k] = new ConsumerThread(k);
        return threads;
    }

    private class ProducerThread extends TestThread {

        public ProducerThread(long id) {
            super("ProducerThread-" + id);
        }

        public void run() {
            int runCount = 0;

            while (produceCountDown.getAndDecrement() > 0) {
                new TransactionTemplate(stm) {
                    protected Object execute(Transaction t) throws Exception {
                        ManualQueue queue = (ManualQueue) t.read(queueHandle);
                        queue.push(item);
                        return null;
                    }
                }.execute();

                runCount++;
                if (runCount % 500000 == 0)
                    System.out.println(getName() + " at transactioncount: " + runCount);
            }
        }
    }

    private class ConsumerThread extends TestThread {

        public ConsumerThread(long id) {
            super("QueueThread-" + id);
        }

        public void run() {
            int runCount = 0;

            while (consumeCountDown.getAndDecrement() > 0) {
                new TransactionTemplate(stm) {
                    protected Object execute(Transaction t) throws Exception {
                        ManualQueue queue = (ManualQueue) t.read(queueHandle);
                        return queue.pop();
                    }
                }.execute();

                runCount++;
                if (runCount % 500000 == 0)
                    System.out.println(getName() + " at transactioncount: " + runCount);
            }
        }
    }
}
