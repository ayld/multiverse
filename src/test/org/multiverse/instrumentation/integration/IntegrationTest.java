package org.multiverse.instrumentation.integration;

import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.SharedStmInstance;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import static org.multiverse.api.TransactionThreadLocal.getTransaction;
import org.multiverse.api.annotations.Atomic;
import org.multiverse.collections.Queue;

import java.util.concurrent.atomic.AtomicLong;

public class IntegrationTest {

    private String item = "foo";

    private int producerCount = 1;
    private int consumerCount = 1;
    private int itemCount;

    private Handle<Queue> queueHandle;

    private long startMs;
    private long endMs;

    private AtomicLong produceCountDown = new AtomicLong();
    private AtomicLong consumeCountDown = new AtomicLong();


    @Before
    public void setUp() throws Exception {
        this.queueHandle = commit(SharedStmInstance.getInstance(), new Queue(100));

        //    new PrintMultiversionedStmStatisticsThread(stm).start();
    }

    @After
    public void tearDown() throws Exception {
        //System.out.println(stm.getStatistics());

        //long timeMs = endMs - startMs;
        //System.out.printf("tranfer of %s items took %s ms\n", itemCount, timeMs);
        //System.out.printf("%s transactions/second\n", (stm.getStatistics().getTransactionCommittedCount() / (timeMs / 1000.0)));
    }

    @Test
    public void testProducerConsumer_100000() {
        testProducerConsumer(100000);
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

    @Atomic
    public void assertQueueIsEmpty() {
        Transaction t = getTransaction();
        Queue queue = t.read(queueHandle);
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
                produce();
                runCount++;
                if (runCount % 500000 == 0)
                    System.out.println(getName() + " at transactioncount: " + runCount);
            }
        }

        @Atomic
        public void produce() {
            Queue queue = getTransaction().read(queueHandle);
            queue.push(item);
        }
    }


    private class ConsumerThread extends TestThread {

        public ConsumerThread(long id) {
            super("ConsumerThread-" + id);
        }

        public void run() {
            int runCount = 0;

            while (consumeCountDown.getAndDecrement() > 0) {
                consume();
                runCount++;
                if (runCount % 500000 == 0)
                    System.out.println(getName() + " at transactioncount: " + runCount);
            }
        }

        @Atomic
        public Object consume() {
            Queue queue = getTransaction().read(queueHandle);
            return queue.pop();
        }
    }
}
