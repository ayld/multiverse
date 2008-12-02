package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.TransactionTemplate;
import org.codehaus.multiverse.TestUtils;
import static org.codehaus.multiverse.TestUtils.joinAll;
import static org.codehaus.multiverse.TestUtils.startAll;
import org.codehaus.multiverse.multiversionedstm.AbstractMultiversionedStmTest;
import org.codehaus.multiverse.transaction.Transaction;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class QueueTest extends AbstractMultiversionedStmTest {
    private long queuePtr;
    private Set pushed = Collections.synchronizedSet(new HashSet());
    private Set popped = Collections.synchronizedSet(new HashSet());

    @Override
    public void setUp() throws Exception {
        super.setUp();
        queuePtr = atomicInsert(new Queue());
    }

    public void tearDown() {
        System.out.println(heap.getStatistics());
        assertEquals(pushed, popped);
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

    public void testProducerConsumer() throws InterruptedException {
        Thread producer = new ProducerThread();
        Thread consumer1 = new ConsumerThread();
        Thread consumer2 = new ConsumerThread();
        Thread consumer3 = new ConsumerThread();

        startAll(producer, consumer1, consumer2, consumer3);
        joinAll(producer, consumer1, consumer2, consumer3);
    }

    static AtomicInteger producerCounter = new AtomicInteger();

    private class ProducerThread extends Thread {

        public ProducerThread() {
            super("producer-" + producerCounter.incrementAndGet());
        }

        private int runCount = 0;

        public void run() {
            for (int k = 0; k < 3000; k++) {
                atomicPush("" + k);

                runCount++;
                if (runCount % 100 == 0)
                    System.out.println(getName() + " transactioncount: " + runCount);

                //    sleepRandom(3);
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

    private class ConsumerThread extends Thread {
        private int runCount = 0;

        public ConsumerThread() {
            super("consumer-" + consumerCounter.incrementAndGet());
        }

        public void run() {
            String item;
            do {
                item = atomicPop();
                //System.out.println(toString() + " Consumed: " + item);
                //    sleepRandom(10);

                runCount++;
                if (runCount % 100 == 0)
                    System.out.println(getName() + " transactioncount: " + runCount);


            } while (!"poison".equals(item));
        }
    }
}
