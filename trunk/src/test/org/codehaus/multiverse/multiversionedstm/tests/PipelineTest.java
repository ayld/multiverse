package org.codehaus.multiverse.multiversionedstm.tests;

import org.codehaus.multiverse.TestThread;
import static org.codehaus.multiverse.TestUtils.*;
import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.core.TransactionTemplate;
import org.codehaus.multiverse.multiversionedheap.standard.DefaultMultiversionedHeap;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import org.codehaus.multiverse.multiversionedstm.examples.Queue;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The Pipeline test is an integration test. There is a pipeline of producer/handover...handove/consumer threads
 * where the producer threads produces an item and puts it on the first queue, a consumer thread consumes it from
 * the last queue, and handover threads that handover items from one queue to the next.
 *
 * @author Peter Veentjer.
 */
public class PipelineTest {

    private MultiversionedStm stm;
    private long[] queues;
    private int queueCount = 10;
    private int produceCount = 5000;
    private int delayMs = 5;

    private DefaultMultiversionedHeap heap;

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
    public void test() {
        queues = createQueues();

        ProducerThread producerThread = new ProducerThread();
        ConsumerThread consumerThread = new ConsumerThread();
        HandoverThread[] handoverThreads = createHandoverThreads();

        startAll(producerThread, consumerThread);
        startAll(handoverThreads);

        joinAll(producerThread, consumerThread);
        joinAll(handoverThreads);

        assertQueuesAreEmpty();
        //make sure that all items produced are also consumed, and that the order also is untouched.
        assertEquals(producerThread.producedList, consumerThread.consumedList);
    }

    public HandoverThread[] createHandoverThreads() {
        HandoverThread[] threads = new HandoverThread[queueCount - 1];
        for (int k = 0; k < threads.length; k++) {
            threads[k] = new HandoverThread(queues[k], queues[k + 1]);
        }
        return threads;
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

    private long[] createQueues() {
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
                    Queue<Integer> queue = (Queue<Integer>) t.read(queues[0]);
                    queue.push(item);
                    return null;
                }
            }.execute();
        }

        public void run() {
            for (int k = 1; k <= produceCount; k++) {
                produceOneItem(k);
                producedList.add(k);
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
                    Queue<Integer> queue = (Queue) t.read(queues[queues.length - 1]);
                    return queue.pop();
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

    private class HandoverThread extends TestThread {
        private final long fromHandle;
        private final long toHandle;

        public HandoverThread(long fromHandle, long toHandle) {
            setName("HandoverThread");
            this.fromHandle = fromHandle;
            this.toHandle = toHandle;
        }

        public void moveOneItem() {
            new TransactionTemplate<Integer>(stm) {
                @Override
                protected Integer execute(Transaction t) throws Exception {
                    Queue<Integer> fromQueue = (Queue) t.read(fromHandle);
                    Queue<Integer> toQueue = (Queue) t.read(toHandle);
                    toQueue.push(fromQueue.pop());
                    return null;
                }
            }.execute();
        }

        public void run() {
            for (int k = 0; k < produceCount; k++) {
                moveOneItem();
                sleepRandomMs(delayMs);
            }
        }
    }
}
