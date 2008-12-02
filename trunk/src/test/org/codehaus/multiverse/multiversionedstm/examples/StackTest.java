package org.codehaus.multiverse.multiversionedstm.examples;

import static org.codehaus.multiverse.TestUtils.*;
import org.codehaus.multiverse.TransactionTemplate;
import org.codehaus.multiverse.multiversionedstm.AbstractMultiversionedStmTest;
import org.codehaus.multiverse.transaction.Transaction;

import java.util.concurrent.atomic.AtomicInteger;

public class StackTest extends AbstractMultiversionedStmTest {
    private long stackPtr;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        stackPtr = atomicInsert(new Stack());
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        System.out.println(heap.getStatistics());
    }

    public void atomicPush(final String item) {
        new TransactionTemplate(stm) {
            protected Object execute(Transaction t) throws Exception {
                Stack stack = (Stack) t.read(stackPtr);
                stack.push(item);
                return null;
            }
        }.execute();
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

        asynchronousPush("Hallo");
        sleep(1000);

        //todo: content needs to be checked
    }

    public void testProducerConsumer(int produceCount) {
        Thread producer1 = new ProducerThread(produceCount);
        Thread producer2 = new ProducerThread(produceCount);
        Thread producer3 = new ProducerThread(produceCount);

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

    final static AtomicInteger producerCounter = new AtomicInteger();
    final static AtomicInteger consumerCounter = new AtomicInteger();
    final static AtomicInteger itemCounter = new AtomicInteger();

    private class ProducerThread extends Thread {
        private int produceCount;

        public ProducerThread(int produceCount) {
            super("producer-" + producerCounter.incrementAndGet());
            this.produceCount = produceCount;
        }

        public void run() {
            for (int k = 0; k < produceCount; k++) {
                atomicPush("" + itemCounter.incrementAndGet());
                //TestUtils.sleepRandom(10);
            }

            atomicPush("poison");
        }
    }

    private class ConsumerThread extends Thread {

        public ConsumerThread() {
            super("consumer-" + consumerCounter.incrementAndGet());
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
