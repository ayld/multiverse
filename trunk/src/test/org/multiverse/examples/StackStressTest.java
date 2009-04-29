package org.multiverse.examples;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Originator;
import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionTemplate;
import org.multiverse.multiversionedstm.MultiversionedStm;

import java.util.concurrent.atomic.AtomicInteger;

public class StackStressTest {

    public static String item = "foo";

    private final AtomicInteger producedItemCounter = new AtomicInteger();
    private final AtomicInteger consumeCountDown = new AtomicInteger();
    private final AtomicInteger produceCountDown = new AtomicInteger();

    private MultiversionedStm stm;
    private Originator<Stack<String>> stackOriginator;
    private long startMs;
    private long endMs;
    private int itemCount;

    private int producerCount = 1;
    private int consumerCount = 1;

    @Before
    public void setUp() throws Exception {
        stm = new MultiversionedStm();
        stackOriginator = commit(stm, new Stack<String>());
    }

    @After
    public void tearDown() throws Exception {
        System.out.println(stm.getStatistics());

        long timeMs = (endMs - startMs) + 1;
        System.out.println(String.format("%s items took %s ms", itemCount, timeMs));
        double performance = stm.getStatistics().getTransactionCommittedCount() / (timeMs / 1000.0);
        System.out.println(String.format("%s transactions/second", performance));
    }

    @Test
    public void testProduceConsumer_100000() {
        testProducerConsumer(100000);
    }

    @Test
    public void testProduceConsumer_1000000() {
        testProducerConsumer(1000000);
    }

    @Test
    public void testProduceConsumer_10000000() {
        testProducerConsumer(10000000);
    }

    //@Test
    //public void testProduceConsumer_100000000() {
    //    testProducerConsumer(100000000);
    //}

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
                        Stack<String> stack = t.read(stackOriginator);
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
                        Stack<String> stack = t.read(stackOriginator);
                        return stack.pop();
                    }
                }.execute();
            }
        }
    }
}

