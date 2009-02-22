package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.TestThread;
import org.codehaus.multiverse.TestUtils;
import static org.codehaus.multiverse.TestUtils.*;
import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.core.TransactionTemplate;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import org.codehaus.multiverse.multiversionedstm.growingheap.GrowingMultiversionedHeap;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class QueueIntegrationTest {
    private int producerCount = 1;
    private int consumerCount = 1;
    private int produceCount;

    private long queueHandle;
    //private Set pushed = Collections.synchronizedSet(new HashSet());
    //private Set popped = Collections.synchronizedSet(new HashSet());

    private long startMs;
    private long endMs;

    private AtomicInteger produceCounter = new AtomicInteger();

    private GrowingMultiversionedHeap heap;
    private MultiversionedStm stm;

    @Before
    public void setUp() throws Exception {
        heap = new GrowingMultiversionedHeap();
        stm = new MultiversionedStm(heap);
        queueHandle = atomicInsert(stm, new Queue());
    }

    @After
    public void tearDown() throws Exception {
        //    assertEquals(pushed, popped);

        System.out.println(heap.getStatistics());

        long timeMs = endMs - startMs;
        System.out.printf("%s chain alivecount\n", heap.getSnapshotChain().getAliveCount());
        System.out.printf("tranfer of %s took %s ms\n", produceCount, timeMs);
        System.out.printf("%s transactions/second\n", (heap.getStatistics().commitSuccessCount.longValue() / (timeMs / 1000.0)));
    }

    public void atomicPush(final String item) {
        new TransactionTemplate(stm) {
            protected Object execute(Transaction t) throws Exception {
                Queue queue = (Queue) t.read(queueHandle);
                queue.push(item);
                return null;
            }
        }.execute();

        //     pushed.add(item);
    }

    public String atomicPop() {
        String object = (String) new TransactionTemplate(stm) {
            protected Object execute(Transaction t) throws Exception {
                Queue queue = (Queue) t.read(queueHandle);
                return queue.pop();
            }
        }.execute();
        //    popped.add(object);
        return object;
    }

    public int atomicSize() {
        return (Integer) new TransactionTemplate(stm) {
            protected Integer execute(Transaction t) throws Exception {
                Queue queue = (Queue) t.read(queueHandle);
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
                    String result = atomicPop();
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

    @Test
    public void testProducerConsumer_10000() {
        testProducerConsumer(10000);
    }

    @Test
    public void testProducerConsumer_100000() {
        testProducerConsumer(100000);
    }

    @Test
    public void testProducerConsumer_1000000() {
        testProducerConsumer(1000000);
    }

    @Test
    public void testProducerConsumer_5000000() {
        testProducerConsumer(50000000);
    }

    public void testProducerConsumer(int commitCount) {
        this.produceCount = commitCount;
        produceCounter.set(commitCount);

        ProducerThread[] producerThreads = createProducerThreads();
        ConsumerThread[] consumerThreads = createConsumerThreads();

        startMs = System.currentTimeMillis();

        startAll(consumerThreads);
        startAll(producerThreads);

        joinAll(consumerThreads);
        joinAll(producerThreads);

        endMs = System.currentTimeMillis();
    }

    public ProducerThread[] createProducerThreads() {
        ProducerThread[] threads = new ProducerThread[producerCount];
        for (int k = 0; k < threads.length; k++)
            threads[k] = new ProducerThread();
        return threads;
    }

    public ConsumerThread[] createConsumerThreads() {
        ConsumerThread[] threads = new ConsumerThread[consumerCount];
        for (int k = 0; k < threads.length; k++)
            threads[k] = new ConsumerThread();
        return threads;
    }

    static AtomicInteger producerCounter = new AtomicInteger();

    private class ProducerThread extends TestThread {

        public ProducerThread() {
            super("producer-" + producerCounter.incrementAndGet());
        }

        private int runCount = 0;

        public void run() {
            int k = produceCounter.decrementAndGet();
            while (k > 0) {
                atomicPush("foo");

                runCount++;
                if (runCount % 100000 == 0)
                    System.out.println(getName() + " transactioncount: " + runCount);

                //    sleepRandomMs(3);
                k = produceCounter.decrementAndGet();
            }

            atomicPush("poison");
            atomicPush("poison");
            atomicPush("poison");
            atomicPush("poison");
            atomicPush("poison");
            atomicPush("poison");
        }
    }

    static AtomicInteger consumerCounter = new AtomicInteger();

    private class ConsumerThread extends TestThread {
        private int runCount = 0;

        public ConsumerThread() {
            super("consumer-" + consumerCounter.incrementAndGet());
        }

        public void run() {
            String item;
            do {
                item = atomicPop();
                //System.out.println(toString() + " Consumed: " + item);
                //    sleepRandomMs(10);

                runCount++;
                if (runCount % 100000 == 0)
                    System.out.println(getName() + " transactioncount: " + runCount);


            } while (!"poison".equals(item));
        }
    }
}
