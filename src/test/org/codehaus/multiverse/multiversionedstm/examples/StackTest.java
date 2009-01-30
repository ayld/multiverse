package org.codehaus.multiverse.multiversionedstm.examples;

import static org.codehaus.multiverse.TestUtils.*;
import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.core.TransactionTemplate;
import org.codehaus.multiverse.multiversionedstm.AbstractMultiversionedStmTest;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class StackTest extends AbstractMultiversionedStmTest {
    private long stackPtr;
    private long startMs;
    private int produceCount;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        stackPtr = atomicInsert(new Stack());
        startMs = System.currentTimeMillis();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        System.out.println(heap.getStatistics());

        long timeMs = (System.currentTimeMillis() - startMs) + 1;
        System.out.println(String.format("%s chain alivecount", heap.getSnapshotChain().getAliveCount()));
        System.out.println(String.format("%s transactions took %s ms", produceCount, timeMs));
        System.out.println(String.format("%s transactions/second", (produceCount / (timeMs / 1000.0))));
    }

    int count;

    public void atomicPush(final String item) {
        new TransactionTemplate(stm) {
            protected Object execute(Transaction t) throws Exception {
                Stack stack = (Stack) t.read(stackPtr);
                stack.push(item);
                return null;
            }
        }.execute();

        //count++;
        //if(count % 1000000==0){
        //    System.out.println("pause");
        //    sleep(1000);
        //    System.out.println("finished pause");
        //}
    }

    public String atomicPop() {
        String item = (String) new TransactionTemplate(stm) {
            protected Object execute(Transaction t) throws Exception {
                //    System.out.println(Thread.currentThread() + " trying to pop");
                Stack stack = (Stack) t.read(stackPtr);
                return stack.pop();
            }
        }.execute();
        return item;
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
                    //   System.out.println(Thread.currentThread() + " consumed: " + result);
                } catch (RuntimeException ex) {
                    ex.printStackTrace();
                }
            }
        }.start();
    }

    public void testSequential() {
        atomicPush("foo");
        atomicPush("bar");

        String item1 = atomicPop();
        assertEquals("bar", item1);
        String item2 = atomicPop();
        assertEquals("foo", item2);
    }


    public void testAsynchronous() {
        asynchronousPop();
        sleep(1000);

        asynchronousPush("Hello");
        sleep(1000);

        //todo: content needs to be checked
    }

    public void testProducerConsumer(int produceCount) {
        this.produceCount = produceCount;
        produceTodoCounter.set(produceCount);
        Thread producer1 = new ProducerThread();
        Thread producer2 = new ProducerThread();
        Thread producer3 = new ProducerThread();

        Thread consumer1 = new ConsumerThread();
        Thread consumer2 = new ConsumerThread();
        Thread consumer3 = new ConsumerThread();

        startAll(producer1, producer2, producer3, consumer1, consumer2, consumer3);
        joinAll(producer1, producer2, producer3, consumer1, consumer2, consumer3);
    }

    public void testProduceConsumer_10() {
        testProducerConsumer(10);
    }

    public void testProduceConsumer_100() {
        testProducerConsumer(100);
    }

    public void testProduceConsumer_1000() {
        testProducerConsumer(1000);
    }

    public void testProduceConsumer_10000() {
        testProducerConsumer(10000);
    }

    public void testProduceConsumer_100000() {
        testProducerConsumer(100000);
    }

    public void testProduceConsumer_1000000() {
        testProducerConsumer(1000000);
    }

    public void _testProduceConsumer_10000000() {
        testProducerConsumer(10000000);
    }

    final static AtomicInteger producerThreadCounter = new AtomicInteger();
    final static AtomicLong produceTodoCounter = new AtomicLong();
    final static AtomicInteger consumerThreadCounter = new AtomicInteger();
    final static AtomicInteger producedItemCounter = new AtomicInteger();

    private class ProducerThread extends Thread {

        public ProducerThread() {
            super("producer-" + producerThreadCounter.incrementAndGet());
        }

        public void run() {
            while (produceTodoCounter.decrementAndGet() > 0) {
                long itemCount = producedItemCounter.incrementAndGet();
                atomicPush("" + itemCount);

                if (itemCount % 500000 == 0) {
                    System.out.println(String.format("Produced %s items", itemCount));
                }
            }

            atomicPush("poison");
        }
    }

    private class ConsumerThread extends Thread {

        public ConsumerThread() {
            super("consumer-" + consumerThreadCounter.incrementAndGet());
        }

        public void run() {
            String item;
            do {
                item = atomicPop();
                //System.out.println(Thread.currentThread() + " consumed: " + item);
                //TestUtils.sleepRandom(10);
            } while (!"poison".equals(item));
        }
    }
}
