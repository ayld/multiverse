package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.TestThread;
import static org.codehaus.multiverse.TestUtils.*;
import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.core.TransactionTemplate;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import org.codehaus.multiverse.multiversionedstm.growingheap.SmartGrowingMultiversionedHeap;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class StackIntegrationTest {
    private long stackHandle;
    private long startMs;
    private long endMs;

    private final static AtomicInteger producedItemCounter = new AtomicInteger();
    private final static AtomicInteger produceTodoCounter = new AtomicInteger();
    private SmartGrowingMultiversionedHeap heap;

    private int produceCount;
    private int producerCount = 1;
    private int consumerCount = 1;
    private MultiversionedStm stm;

    @Before
    public void setUp() throws Exception {
        heap = new SmartGrowingMultiversionedHeap();
        stm = new MultiversionedStm(heap);
        stackHandle = atomicInsert(stm, new Stack());

    }

    @After
    public void tearDown() throws Exception {
        System.out.println(heap.getStatistics());

        long timeMs = (endMs - startMs) + 1;
        System.out.println(String.format("%s chain alivecount", heap.getSnapshotChain().getAliveCount()));
        System.out.println(String.format("%s items took %s ms", produceCount, timeMs));
        System.out.println(String.format("%s transactions/second", stm.getStatistics().getTransactionsCommitedCount() / (timeMs / 1000.0)));
    }

    public void atomicPush(final String item) {
        new TransactionTemplate(stm) {
            protected Object execute(Transaction t) throws Exception {
                Stack<String> stack = (Stack) t.read(stackHandle);
                stack.push(item);
                return null;
            }
        }.execute();
    }

    public String atomicPop() {
        return new TransactionTemplate<String>(stm) {
            protected String execute(Transaction t) throws Exception {
                Stack stack = (Stack) t.read(stackHandle);
                return (String) stack.pop();
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
                } catch (RuntimeException ex) {
                    ex.printStackTrace();
                }
            }
        }.start();
    }

    @Test
    public void testSequential() {
        atomicPush("foo");
        atomicPush("bar");

        String item1 = atomicPop();
        assertEquals("bar", item1);
        String item2 = atomicPop();
        assertEquals("foo", item2);
    }

    @Test
    public void testAsynchronous() {
        asynchronousPop();
        asynchronousPush("Hello");

        //todo: content needs to be checked
    }

    @Test
    public void testPopOnEmptyStackDoesntLoop() {
        long started = stm.getStatistics().getTransactionsStartedCount();

        asynchronousPop();
        sleep(500);

        assertEquals(started + 1, stm.getStatistics().getTransactionsStartedCount());
    }

    @Test
    public void testProduceConsumer_10() {
        testProducerConsumer(10);
    }

    @Test
    public void testProduceConsumer_100() {
        testProducerConsumer(100);
    }

    @Test
    public void testProduceConsumer_1000() {
        testProducerConsumer(1000);
    }

    @Test
    public void testProduceConsumer_10000() {
        testProducerConsumer(10000);
    }

    @Test
    public void testProduceConsumer_100000() {
        testProducerConsumer(100000);
    }

    @Test
    public void testProduceConsumer_1000000() {
        testProducerConsumer(2000000);
    }

    @Test
    public void testProduceConsumer_10000000() {
        testProducerConsumer(100000000);
    }

    public void testProducerConsumer(int produceCount) {
        this.produceCount = produceCount;
        produceTodoCounter.set(produceCount);

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
            threads[k] = new ConsumerThread();
        return threads;
    }

    private ProducerThread[] createProducerThreads() {
        ProducerThread[] threads = new ProducerThread[producerCount];
        for (int k = 0; k < threads.length; k++)
            threads[k] = new ProducerThread();
        return threads;
    }

    final static AtomicInteger producerThreadIdGenerator = new AtomicInteger();

    private class ProducerThread extends TestThread {

        public ProducerThread() {
            super("producer-" + producerThreadIdGenerator.incrementAndGet());
        }

        public void run() {
            while (produceTodoCounter.decrementAndGet() > 0) {
                long itemCount = producedItemCounter.incrementAndGet();
                atomicPush("" + itemCount);

                if (itemCount % 500000 == 0) {
                    System.out.printf("Produced %s items\n", itemCount);
                }
            }

            atomicPush("poison");
        }
    }

    final static AtomicInteger consumerThreadIdGenerator = new AtomicInteger();

    private class ConsumerThread extends TestThread {

        public ConsumerThread() {
            super("consumer-" + consumerThreadIdGenerator.incrementAndGet());
        }

        public void run() {
            String item;
            do {
                item = atomicPop();
                //System.out.println(Thread.currentThread() + " consumed: " + item);
                //TestUtils.sleepRandomMs(10);
            } while (!"poison".equals(item));
        }
    }
}
