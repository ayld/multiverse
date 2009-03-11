package org.codehaus.multiverse.multiversionedstm.tests;

import org.codehaus.multiverse.TestThread;
import static org.codehaus.multiverse.TestUtils.joinAll;
import static org.codehaus.multiverse.TestUtils.sleepRandomMs;
import org.codehaus.multiverse.api.Transaction;
import org.codehaus.multiverse.api.TransactionTemplate;
import org.codehaus.multiverse.multiversionedheap.standard.DefaultMultiversionedHeap;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import static org.codehaus.multiverse.multiversionedstm.TransactionMethods.retry;
import org.codehaus.multiverse.multiversionedstm.examples.Queue;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * The test checks if a wait can be done on multiple condition variables. Normally with a BlockingQueue you can only
 * block on 1 queue, if you have multiple queues, waiting on an update on one of them is not possible, because each
 * queue has his own waitset.
 * <p/>
 * With STM's this limitation doesn't exist. This tests checks if that works. It does that be creating a
 * bunch of queues and a producer threads random places items in of the queues, and a consumer thread that
 * waits on the availability of an item on all queues.
 *
 * @author Peter Veentjer.
 */
public class WaitOnMultiConditionVariableTest {

    private MultiversionedStm stm;
    private long[] queues;
    private DefaultMultiversionedHeap heap;

    private int queueCount = 10;
    private int produceCount = 2000;
    private int delayMs = 5;

    @Before
    public void setUp() {
        heap = new DefaultMultiversionedHeap();
        stm = new MultiversionedStm(heap);
    }

    @After
    public void teatDown() {
        System.out.println(stm.getStatistics());
        System.out.println(heap.getStatistics());
    }

    @Test
    public void test() throws InterruptedException {
        queues = createQueues(queueCount);

        ProducerThread producerThread = new ProducerThread();
        producerThread.start();

        ConsumerThread consumerThread = new ConsumerThread();
        consumerThread.start();

        joinAll(producerThread, consumerThread);

        assertQueuesAreEmpty();
        assertEquals(new HashSet(producerThread.producedList), new HashSet(consumerThread.consumedList));
    }

    public void assertQueuesAreEmpty() {
        Transaction t = stm.startTransaction();
        for (long handle : queues) {
            Queue queue = (Queue) t.read(handle);
            if (!queue.isEmpty())
                fail();
        }

        t.commit();
    }

    private long[] createQueues(int queueCount) {
        Transaction t = stm.startTransaction();
        long[] handles = new long[queueCount];
        for (int k = 0; k < queueCount; k++)
            handles[k] = t.attachAsRoot(new Queue());
        t.commit();
        return handles;
    }

    private class ProducerThread extends TestThread {
        private final List<Integer> producedList = new ArrayList<Integer>(produceCount);

        public ProducerThread() {
            setName("ProducerThread");
        }

        public void produceOneItem(final int item) {
            new TransactionTemplate(stm) {
                @Override
                protected Object execute(Transaction t) throws Exception {
                    long queueHandle = queues[item % queues.length];
                    Queue<Integer> queue = (Queue<Integer>) t.read(queueHandle);
                    queue.push(item);
                    return null;
                }
            }.execute();

            producedList.add(item);
        }

        public void run() {
            for (int k = 1; k <= produceCount; k++) {
                produceOneItem(k);
                sleepRandomMs(delayMs);
            }
        }
    }

    private class ConsumerThread extends TestThread {
        private final List consumedList = new LinkedList();

        public ConsumerThread() {
            setName("ConsumerThread");
        }

        public int consumeOneItem() {

            return new TransactionTemplate<Integer>(stm) {
                @Override
                protected Integer execute(Transaction t) throws Exception {
                    for (int k = 0; k < queues.length; k++) {
                        Queue<Integer> queue = (Queue) t.read(queues[k]);
                        Integer item = queue.peek();
                        if (item != null)
                            return item;
                    }

                    retry();
                    return null;
                }
            }.execute();
        }

        public void run() {
            for (int k = 0; k < produceCount; k++) {
                int item = consumeOneItem();
                sleepRandomMs(delayMs);
                consumedList.add(item);
            }
        }
    }
}
