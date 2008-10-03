package org.codehaus.stm.multiversionedstm;

import org.codehaus.stm.TransactionTemplate;
import org.codehaus.stm.multiversionedstm.examples.Queue;
import org.codehaus.stm.transaction.Transaction;

public class QueueTest extends AbstractStmTest {
    private long queuePtr;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        queuePtr = insert(new Queue());
    }

    public void atomicPush(final String item) {
        new TransactionTemplate(stm) {
            protected Object execute(Transaction t) throws Exception {
                Queue queue = (Queue) t.read(queuePtr);
                queue.push(item);
                return null;
            }
        }.execute();

        System.out.println(Thread.currentThread()+" pushed: " + item);
    }

    public String atomicPop() {
        return (String) new TransactionTemplate(stm) {
            protected Object execute(Transaction t) throws Exception {
                Queue queue = (Queue) t.read(queuePtr);
                return queue.pop();
            }
        }.execute();
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
                    System.out.println(Thread.currentThread()+" consumed: " + result);
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
        sleep(1000);
        asynchronousPush("foo");
        asynchronousPush("bar");
        sleep(1000);
    }

    public void testProducerConsumer() throws InterruptedException {
        Thread producerThread = new ProducerThread();
        Thread consumerThread1 = new ConsumerThread();
        Thread consumerThread2 = new ConsumerThread();
        Thread consumerThread3 = new ConsumerThread();

        producerThread.start();
        consumerThread1.start();
        consumerThread2.start();
        consumerThread3.start();

        producerThread.join();
        consumerThread1.join();
        consumerThread2.join();
        consumerThread3.join();
    }

    private class ProducerThread extends Thread {

        public void run() {
            for (int k = 0; k < 1000; k++) {
                atomicPush("" + k);
                sleepRandom(1000);
            }

            atomicPush("poison");
        }
    }

    private class ConsumerThread extends Thread {

        public void run() {
            String item;
            do {
                item = atomicPop();
                System.out.println(toString()+" Consumed: " + item);
                sleepRandom(3000);
            } while (!"poison".equals(item));
        }
    }
}
