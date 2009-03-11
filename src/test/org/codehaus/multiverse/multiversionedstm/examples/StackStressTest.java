package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.TestThread;
import static org.codehaus.multiverse.TestUtils.*;
import org.codehaus.multiverse.api.Transaction;
import org.codehaus.multiverse.api.TransactionTemplate;
import org.codehaus.multiverse.multiversionedheap.standard.DefaultMultiversionedHeap;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class StackStressTest {

    public static String item = "foo";

    private final AtomicInteger producedItemCounter = new AtomicInteger();
    private final AtomicInteger consumeCountDown = new AtomicInteger();
    private final AtomicInteger produceCountDown = new AtomicInteger();

    private MultiversionedStm stm;
    private DefaultMultiversionedHeap heap;
    private long stackHandle;
    private long startMs;
    private long endMs;
    private int itemCount;


    private int producerCount = 1;
    private int consumerCount = 1;

    @Before
    public void setUp() throws Exception {
        heap = new DefaultMultiversionedHeap();
        stm = new MultiversionedStm(heap);
        stackHandle = commit(stm, new Stack());
    }

    @After
    public void tearDown() throws Exception {
        System.out.println(heap.getStatistics());

        long timeMs = (endMs - startMs) + 1;
        System.out.println(String.format("%s items took %s ms", itemCount, timeMs));
        System.out.println(String.format("%s transactions/second", stm.getStatistics().getTransactionsCommitedCount() / (timeMs / 1000.0)));
    }

    @Test
    public void testProduceConsumer_1000000() {
        testProducerConsumer(1000000);
    }

    @Test
    public void testProduceConsumer_10000000() {
        testProducerConsumer(10000000);
    }

    public void testProducerConsumer(int produceCount) {
        this.itemCount = produceCount;
        produceCountDown.set(produceCount);
        consumeCountDown.set(produceCount);

        ProducerThread[] producerThreads = createProducerThreads();
        ConsumerThread[] consumerThreads = createConsumerThreads();

        startMs = System.currentTimeMillis();

        startAll(consumerThreads);
        startAll(producerThreads);

        joinAll(producerThreads);
        joinAll(consumerThreads);

        endMs = System.currentTimeMillis();
    }

    private ConsumerThread[] createConsumerThreads() {
        ConsumerThread[] threads = new ConsumerThread[consumerCount];
        for (int k = 0; k < threads.length; k++)
            threads[k] = new ConsumerThread(k);
        return threads;
    }

    private ProducerThread[] createProducerThreads() {
        ProducerThread[] threads = new ProducerThread[producerCount];
        for (int k = 0; k < threads.length; k++)
            threads[k] = new ProducerThread(k);
        return threads;
    }

    private class ProducerThread extends TestThread {

        public ProducerThread(int id) {
            super("ProducerThread-" + id);
        }

        public void run() {
            while (produceCountDown.getAndDecrement() > 0) {

                new TransactionTemplate(stm) {
                    protected Object execute(Transaction t) throws Exception {
                        Stack<String> stack = (Stack) t.read(stackHandle);
                        stack.push(item);
                        return null;
                    }
                }.execute();

                long itemCount = producedItemCounter.incrementAndGet();
                if (itemCount % 500000 == 0) {
                    System.out.printf("Produced %s items\n", itemCount);
                }
            }
        }
    }

    private class ConsumerThread extends TestThread {

        public ConsumerThread(long id) {
            super("ConsumerThread-" + id);
        }

        public void run() {
            while (consumeCountDown.getAndDecrement() > 0) {
                new TransactionTemplate<String>(stm) {
                    protected String execute(Transaction t) throws Exception {
                        Stack stack = (Stack) t.read(stackHandle);
                        return (String) stack.pop();
                    }
                }.execute();
            }
        }
    }
}

