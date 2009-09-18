package org.multiverse.integrationtests;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Transaction;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.stms.alpha.AlphaStmConfig;
import org.multiverse.stms.alpha.manualinstrumentation.IntQueue;
import org.multiverse.templates.AtomicTemplate;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;
import org.multiverse.utils.profiling.Profiler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The Pipeline test is an integration test. There is a pipeline of producer/handover...handove/consumer threads
 * where the producer threads produces an item and puts it on the first queue, a consumer thread consumes it from
 * the last queue, and handover threads that handover items from one queue to the next.
 * <p/>
 * This is also an example of the classic producer/consumer problem:
 * http://en.wikipedia.org/wiki/Producers-consumers_problem
 *
 * @author Peter Veentjer.
 */
public class PipelineLongTest {

    private IntQueue[] queues;
    private int queueCount = 100;
    private int produceCount = 5000;
    private int delayMs = 5;
    private AlphaStm stm;
    private Profiler profiler;

    @Before
    public void setUp() {
        AlphaStmConfig config = AlphaStmConfig.createDebugConfig();
        profiler = config.profiler;
        stm = new AlphaStm(config);
        setThreadLocalTransaction(null);
        GlobalStmInstance.set(stm);
    }

    @After
    public void tearDown() {
        if (profiler != null) {
            profiler.print();
        }
    }

    @Test
    public void test() {
        queues = createQueues();

        ProducerThread producerThread = new ProducerThread();
        ConsumerThread consumerThread = new ConsumerThread();
        HandoverThread[] handoverThreads = createHandoverThreads();

        startAll(producerThread, consumerThread);
        startAll(handoverThreads);

        joinAll(producerThread);
        joinAll(handoverThreads);
        joinAll(consumerThread);

        assertQueuesAreEmpty();
        //make sure that all items produced are also consumed, and that the order also is untouched.
        assertEquals(producerThread.producedList, consumerThread.consumedList);
    }

    public HandoverThread[] createHandoverThreads() {
        HandoverThread[] threads = new HandoverThread[queueCount - 1];

        for (int k = 0; k < threads.length; k++) {
            IntQueue from = queues[k];
            IntQueue to = queues[k + 1];
            threads[k] = new HandoverThread(k, from, to);
        }
        return threads;
    }

    public void assertQueuesAreEmpty() {
        for (IntQueue queue : queues) {
            if (!queue.isEmpty()) {
                fail();
            }
        }
    }

    private IntQueue[] createQueues() {
        IntQueue[] result = new IntQueue[queueCount];
        for (int k = 0; k < queueCount; k++) {
            result[k] = new IntQueue();
        }
        return result;
    }

    private class ProducerThread extends TestThread {
        private final List<Integer> producedList = new ArrayList<Integer>(produceCount);

        public ProducerThread() {
            setName("ProducerThread");
        }

        public void run() {
            for (int k = 1; k <= produceCount; k++) {
                produceOneItem(k);
                producedList.add(k);
                sleepRandomMs(delayMs);
            }
        }

        public void produceOneItem(final int item) {
            IntQueue queue = queues[0];
            queue.push(item);
        }
    }

    private class ConsumerThread extends TestThread {
        private final List consumedList = new LinkedList();

        public ConsumerThread() {
            setName("QueueThread");
        }

        public void run() {
            for (int k = 0; k < produceCount; k++) {
                int item = consumeOneItem();
                sleepRandomMs(delayMs);
                consumedList.add(item);
            }
        }

        public int consumeOneItem() {
            IntQueue queue = queues[queues.length - 1];
            return queue.pop();
        }
    }

    private class HandoverThread extends TestThread {
        private final IntQueue from;
        private final IntQueue to;

        public HandoverThread(int id, IntQueue from, IntQueue to) {
            setName("HandoverThread-" + id);
            this.from = from;
            this.to = to;
        }

        public void run() {
            for (int k = 0; k < produceCount; k++) {
                moveOneItem();
                sleepRandomMs(delayMs);
            }
        }

        //@Atomic
        public void moveOneItem() {
            new AtomicTemplate() {
                @Override
                public Object execute(Transaction t) throws Exception {
                    int item = from.pop();
                    to.push(item);
                    return null;  //todo
                }
            }.execute();
        }
    }
}
