package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.TestThread;
import org.codehaus.multiverse.TestUtils;
import static org.codehaus.multiverse.TestUtils.joinAll;
import static org.codehaus.multiverse.TestUtils.startAll;
import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.core.TransactionTemplate;
import org.codehaus.multiverse.multiversionedstm.AbstractMultiversionedStmTest;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import org.codehaus.multiverse.multiversionedstm.growingheap.GrowingMultiversionedHeap;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class QueueIntegrationTest extends AbstractMultiversionedStmTest {
    private long queuePtr;
    private Set pushed = Collections.synchronizedSet(new HashSet());
    private Set popped = Collections.synchronizedSet(new HashSet());
    private long startMs;
    private AtomicInteger produceCounter = new AtomicInteger();
    private int produceCount;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        startMs = System.currentTimeMillis();
        queuePtr = atomicInsert(new Queue());
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        assertEquals(pushed, popped);

        long timeMs = (System.currentTimeMillis() - startMs) + 1;
        System.out.println(String.format("%s chain alivecount", heap.getSnapshotChain().getAliveCount()));
        System.out.println(String.format("%s transactions took %s ms", produceCount, timeMs));
        System.out.println(String.format("%s transactions/second", (produceCount / (timeMs / 1000.0))));
    }

    public MultiversionedStm createStm() {
        heap = new GrowingMultiversionedHeap();
        return new MultiversionedStm(heap);
    }

    public void atomicPush(final String item) {
        new TransactionTemplate(stm) {
            protected Object execute(Transaction t) throws Exception {
                Queue queue = (Queue) t.read(queuePtr);
                queue.push(item);
                return null;
            }
        }.execute();

        pushed.add(item);

        //System.out.println(Thread.currentThread() + " pushed: " + item);
    }

    public String atomicPop() {
        String object = (String) new TransactionTemplate(stm) {
            protected Object execute(Transaction t) throws Exception {
                Queue queue = (Queue) t.read(queuePtr);
                return queue.pop();
            }
        }.execute();
        popped.add(object);
        return object;
    }

    public int atomicSize() {
        return (Integer) new TransactionTemplate(stm) {
            protected Integer execute(Transaction t) throws Exception {
                Queue queue = (Queue) t.read(queuePtr);
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

    public void testSequential() {
        atomicPush("item1");
        atomicPush("item2");

        String result;
        result = atomicPop();
        assertEquals("item1", result);
        result = atomicPop();
        assertEquals("item2", result);
    }

    public void test() {
        asynchronousPop();
        asynchronousPop();
        TestUtils.sleep(1000);
        asynchronousPush("foo");
        asynchronousPush("bar");
        TestUtils.sleep(1000);

        //todo: check that content has been returned.
    }

    public void testProducerConsumer_1() {
        testProducerConsumer(1);
    }

    public void testProducerConsumer_10() {
        testProducerConsumer(10);
    }

    public void testProducerConsumer_100() {
        testProducerConsumer(100);
    }

    public void testProducerConsumer_1000() {
        testProducerConsumer(1000);
    }

    public void testProducerConsumer_10000() {
        testProducerConsumer(10000);
    }

    public void testProducerConsumer_100000() {
        testProducerConsumer(100000);
    }

    public void _testProducerConsumer_1000000() {
        testProducerConsumer(1000000);
    }

    public void _testProducerConsumer_5000000() {
        testProducerConsumer(5000000);
    }

    public void testProducerConsumer(int commitCount) {
        this.produceCount = commitCount;
        produceCounter.set(commitCount);
        ProducerThread producer = new ProducerThread();
        ConsumerThread consumer1 = new ConsumerThread();
        ConsumerThread consumer2 = new ConsumerThread();
        ConsumerThread consumer3 = new ConsumerThread();

        startAll(producer, consumer1, consumer2, consumer3);
        joinAll(producer, consumer1, consumer2, consumer3);
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
                atomicPush("" + k);

                runCount++;
                if (runCount % 1000 == 0)
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
                if (runCount % 1000 == 0)
                    System.out.println(getName() + " transactioncount: " + runCount);


            } while (!"poison".equals(item));
        }
    }
}
