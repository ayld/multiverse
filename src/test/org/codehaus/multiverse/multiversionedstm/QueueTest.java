package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.TransactionTemplate;
import org.codehaus.multiverse.multiversionedstm.examples.Queue;
import org.codehaus.multiverse.transaction.Transaction;

public class QueueTest extends AbstractMultiversionedStmTest {
    private long queuePtr;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        queuePtr = atomicInsert(new Queue());
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
        System.out.println("producerThread Finished");
        consumerThread1.join();
        System.out.println("consumer1 Finished");
        consumerThread2.join();
        System.out.println("consumer2 Finished");
        consumerThread3.join();
        System.out.println("consumer3 Finished");
    }

    private class ProducerThread extends Thread {

        public void run() {
            for (int k = 0; k < 50; k++) {
                atomicPush("" + k);
                sleepRandom(1000);
            }

            atomicPush("poison");
            atomicPush("poison");
            atomicPush("poison");
            atomicPush("poison");
            atomicPush("poison");
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
