package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.TransactionTemplate;
import org.codehaus.multiverse.multiversionedstm.examples.Stack;
import org.codehaus.multiverse.multiversionedstm.AbstractMultiversionedStmTest;
import org.codehaus.multiverse.transaction.Transaction;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class StackTest extends AbstractMultiversionedStmTest {
    private long stackPtr;

    public void setUp() throws Exception {
        super.setUp();
        stackPtr = atomicInsert(new Stack());
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


    public void test() {
        asynchronousPop();
        //asynchronousPop();
        //asynchronousPop();

        sleep(1000);
        System.out.println("pushing");
        asynchronousPush("Hallo");
        sleep(1000);
    }

    public void testProducerConsumer() throws InterruptedException {
        Thread producerThread1 = new ProducerThread();
        Thread producerThread2 = new ProducerThread();
        Thread producerThread3 = new ProducerThread();
        Thread consumerThread1 = new ConsumerThread();
        Thread consumerThread2 = new ConsumerThread();
        Thread consumerThread3 = new ConsumerThread();

        producerThread1.start();
        producerThread2.start();
        producerThread3.start();
        consumerThread1.start();
        consumerThread2.start();
        consumerThread3.start();

        producerThread1.join();
        producerThread2.join();
        producerThread3.join();

        consumerThread1.join();
        consumerThread2.join();
        consumerThread3.join();
    }

    final static AtomicInteger producerCounter = new AtomicInteger();
    final static AtomicInteger consumerCounter = new AtomicInteger();
    final static AtomicInteger itemCounter = new AtomicInteger();

    private class ProducerThread extends Thread {

        int count = producerCounter.incrementAndGet();

        public void run() {
            for (int k = 0; k < 600; k++) {
                atomicPush("" + itemCounter.incrementAndGet());
                sleepRandom(10);
            }

            atomicPush("poison");
        }

        public String toString() {
            return "ProducerThread" + count;
        }
    }

    private class ConsumerThread extends Thread {

        int count = consumerCounter.incrementAndGet();

        public void run() {
            String item;
            do {
                item = atomicPop();
                //System.out.println(Thread.currentThread() + " consumed: " + item);
                sleepRandom(10);
            } while (!"poison".equals(item));
        }

        public String toString() {
            return "ConsumerThread" + count;
        }
    }
}
