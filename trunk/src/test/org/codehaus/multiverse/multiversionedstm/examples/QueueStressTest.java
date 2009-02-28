package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.TestThread;
import static org.codehaus.multiverse.TestUtils.*;
import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.core.TransactionTemplate;
import org.codehaus.multiverse.multiversionedheap.standard.DefaultMultiversionedHeap;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

public class QueueStressTest {

    private String item = "foo";

    private int producerCount = 1;
    private int consumerCount = 1;
    private int itemCount;

    private long queueHandle;

    private long startMs;
    private long endMs;

    private AtomicLong produceCountDown = new AtomicLong();
    private AtomicLong consumeCountDown = new AtomicLong();

    private DefaultMultiversionedHeap heap;
    private MultiversionedStm stm;

    @Before
    public void setUp() throws Exception {
        heap = new DefaultMultiversionedHeap();
        stm = new MultiversionedStm(heap);
        queueHandle = atomicInsert(stm, new Queue());
    }

    @After
    public void tearDown() throws Exception {
        System.out.println(heap.getStatistics());

        long timeMs = endMs - startMs;
        System.out.printf("%s chain alivecount\n", heap.getSnapshotChain().getAliveCount());
        System.out.printf("tranfer of %s items took %s ms\n", itemCount, timeMs);
        System.out.printf("%s transactions/second\n", (heap.getStatistics().commitSuccessCount.longValue() / (timeMs / 1000.0)));
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
        Queue queue = (Queue) t.read(queueHandle);
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
                        Queue queue = (Queue) t.read(queueHandle);
                        queue.push(item);
                        return null;
                    }
                }.execute();

                runCount++;
                if (runCount % 100000 == 0)
                    System.out.println(getName() + " at transactioncount: " + runCount);
            }
        }
    }

    private class ConsumerThread extends TestThread {

        public ConsumerThread(long id) {
            super("ConsumerThread-" + id);
        }

        public void run() {
            int runCount = 0;

            while (consumeCountDown.getAndDecrement() > 0) {
                new TransactionTemplate(stm) {
                    protected Object execute(Transaction t) throws Exception {
                        Queue queue = (Queue) t.read(queueHandle);
                        return queue.pop();
                    }
                }.execute();

                runCount++;
                if (runCount % 100000 == 0)
                    System.out.println(getName() + " at transactioncount: " + runCount);
            }
        }
    }
}
