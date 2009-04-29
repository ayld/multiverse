package org.multiverse.tests;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Originator;
import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionTemplate;
import org.multiverse.examples.Queue;
import org.multiverse.multiversionedstm.MultiversionedStm;

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
    private Originator<Queue<Integer>>[] queueOriginators;
    private int queueCount = 10;
    private int produceCount = 5000;
    private int delayMs = 5;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @After
    public void teatDown() {
        System.out.println(stm.getStatistics());
    }

    @Test
    public void test() {
        queueOriginators = createQueues();

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
            threads[k] = new HandoverThread(queueOriginators[k], queueOriginators[k + 1]);
        }
        return threads;
    }

    public void assertQueuesAreEmpty() {
        Transaction t = stm.startTransaction();
        for (Originator<Queue<Integer>> originator : queueOriginators) {
            Queue<Integer> queue = t.read(originator);
            if (!queue.isEmpty())
                fail();
        }

        t.commit();
    }

    private Originator[] createQueues() {
        Transaction t = stm.startTransaction();
        Originator<Queue>[] result = new Originator[queueCount];
        for (int k = 0; k < queueCount; k++) {
            result[k] = t.attach(new Queue());
        }
        t.commit();
        return result;
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
                    Queue<Integer> queue = t.read(queueOriginators[0]);
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
                    Queue<Integer> queue = t.read(queueOriginators[queueOriginators.length - 1]);
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
        private final Originator<Queue<Integer>> fromOriginator;
        private final Originator<Queue<Integer>> toOriginator;

        public HandoverThread(Originator<Queue<Integer>> fromQueueOriginator, Originator<Queue<Integer>> toQueueOriginator) {
            setName("HandoverThread");
            this.fromOriginator = fromQueueOriginator;
            this.toOriginator = toQueueOriginator;
        }

        public void moveOneItem() {
            new TransactionTemplate<Integer>(stm) {
                @Override
                protected Integer execute(Transaction t) throws Exception {
                    Queue<Integer> fromQueue = t.read(fromOriginator);
                    Queue<Integer> toQueue = t.read(toOriginator);
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
