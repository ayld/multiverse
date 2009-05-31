package org.multiverse.instrumentation.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import static org.multiverse.api.TransactionThreadLocal.getTransaction;
import org.multiverse.api.annotations.Atomic;
import org.multiverse.tmutils.DoubleEndedTmQueue;
import org.multiverse.tmutils.TmQueue;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class QueueIntegrationTest {

    private Handle<? extends TmQueue<String>> queueHandle;
    private AtomicLong produceCountDown = new AtomicLong();
    private AtomicLong consumeCountDown = new AtomicLong();
    private List<String> producedList;
    private List<String> consumedList;

    @Before
    public void setUp() throws Exception {
        queueHandle = commit(new DoubleEndedTmQueue<String>(100));
        producedList = new LinkedList<String>();
        consumedList = new LinkedList<String>();
    }

    @Test
    public void testProducerConsumer_100000() {
        testProducerConsumer(10000);
    }

    public void testProducerConsumer(int itemCount) {
        produceCountDown.set(itemCount);
        consumeCountDown.set(itemCount);

        ProducerThread producerThread = new ProducerThread();
        ConsumerThread consumerThread = new ConsumerThread();

        startAll(consumerThread, producerThread);
        joinAll(producerThread, consumerThread);

        assertQueueIsEmpty();
        assertEquals(producedList, consumedList);
    }

    @Atomic
    public void assertQueueIsEmpty() {
        Transaction t = getTransaction();
        TmQueue queue = t.read(queueHandle);
        assertTrue(queue.isEmpty());
        t.commit();
    }

    private class ProducerThread extends TestThread {

        public ProducerThread() {
            super("ProducerThread");
        }

        @Override
        public void run() {
            int runCount = 0;

            while (produceCountDown.getAndDecrement() > 0) {
                String item = "" + System.nanoTime();
                produce(item);
                producedList.add(item);

                runCount++;
                if (runCount % 500000 == 0)
                    System.out.println(getName() + " at transactioncount: " + runCount);
            }
        }

        @Atomic
        public void produce(String item) {
            TmQueue queue = getTransaction().read(queueHandle);
            queue.push(item);
        }
    }


    private class ConsumerThread extends TestThread {

        public ConsumerThread() {
            super("ConsumerThread");
        }

        @Override
        public void run() {
            int runCount = 0;

            while (consumeCountDown.getAndDecrement() > 0) {
                String item = consume();
                consumedList.add(item);

                runCount++;
                if (runCount % 500000 == 0)
                    System.out.println(getName() + " at transactioncount: " + runCount);
            }
        }

        @Atomic
        public String consume() {
            TmQueue<String> queue = getTransaction().read(queueHandle);
            return queue.pop();
        }
    }
}
