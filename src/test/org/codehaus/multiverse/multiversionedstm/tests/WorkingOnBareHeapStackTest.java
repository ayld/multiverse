package org.codehaus.multiverse.multiversionedstm.tests;

import org.codehaus.multiverse.TestThread;
import static org.codehaus.multiverse.TestUtils.joinAll;
import static org.codehaus.multiverse.TestUtils.startAll;
import org.codehaus.multiverse.api.Transaction;
import org.codehaus.multiverse.api.exceptions.RetryError;
import org.codehaus.multiverse.multiversionedheap.HeapSnapshot;
import org.codehaus.multiverse.multiversionedheap.MultiversionedHeap.CommitResult;
import org.codehaus.multiverse.multiversionedheap.standard.DefaultMultiversionedHeap;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import org.codehaus.multiverse.multiversionedstm.MultiversionedTransaction;
import org.codehaus.multiverse.multiversionedstm.examples.Stack;
import org.codehaus.multiverse.util.iterators.InstanceIterator;
import org.codehaus.multiverse.util.latches.CheapLatch;
import org.codehaus.multiverse.util.latches.Latch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class WorkingOnBareHeapStackTest {
    private MultiversionedStm stm;
    private DefaultMultiversionedHeap heap;

    private int itemCount = 1000000 * 10;

    private AtomicInteger producerCountdown = new AtomicInteger();
    private AtomicInteger consumerCountdown = new AtomicInteger();
    private long handle;
    private Object item = "foo";
    private long startMs;

    @Before
    public void setUp() {
        heap = new DefaultMultiversionedHeap();
        stm = new MultiversionedStm(heap);
        createStack();
        startMs = System.currentTimeMillis();
    }

    @After
    public void tearDown() {
        long timeMs = (System.currentTimeMillis() - startMs) + 1;

        System.out.println(heap.getStatistics());
        System.out.println(String.format("%s items took %s ms", itemCount, timeMs));
        System.out.println(String.format("%s transactions/second", heap.getStatistics().commitSuccessCount.longValue() / (timeMs / 1000.0)));
    }

    public void createStack() {
        Transaction t = stm.startTransaction();
        handle = t.attachAsRoot(new Stack());
        t.commit();
    }

    @Test
    public void test() {
        producerCountdown.set(itemCount);
        consumerCountdown.set(itemCount);

        ProducerThread producerThread = new ProducerThread();
        ConsumerThread consumerThread = new ConsumerThread();

        startAll(consumerThread, producerThread);
        joinAll(consumerThread, producerThread);
    }

    class ConsumerThread extends TestThread {
        private MultiversionedTransaction dummmyTransaction;

        public ConsumerThread() {
            super("ConsumerThread");
        }

        @Override
        public void run() {
            dummmyTransaction = stm.startTransaction();

            while (consumerCountdown.decrementAndGet() >= 0) {
                runTransaction();
                log();
            }
        }

        public void runTransaction() {
            boolean succes = false;
            do {
                HeapSnapshot snapshot = heap.getActiveSnapshot();
                try {
                    CommitResult result = runInsideTransaction(snapshot);
                    if (result.isSuccess())
                        succes = true;
                } catch (RetryError e) {
                    Latch latch = new CheapLatch();
                    heap.listen(snapshot, latch, new long[]{handle});
                    latch.awaitUniterruptibly();
                }
            } while (!succes);
        }

        public CommitResult runInsideTransaction(HeapSnapshot snapshot) {
            Stack stack = (Stack) snapshot.read(handle).___inflate(dummmyTransaction);
            stack.pop();
            return heap.commit(snapshot, new InstanceIterator(stack));
        }

        private void log() {
            long count = consumerCountdown.get();

            if (count % 1000000 == 0)
                System.out.printf("Consumer transactions to go %s\n", count);
        }
    }

    class ProducerThread extends TestThread {
        private MultiversionedTransaction dummmyTransaction;

        public ProducerThread() {
            super("ProducerThread");
        }


        @Override
        public void run() {
            dummmyTransaction = stm.startTransaction();

            while (producerCountdown.decrementAndGet() >= 0) {
                runTransaction();
                log();
            }
        }

        private void log() {
            long count = producerCountdown.get();

            if (count % 1000000 == 0)
                System.out.printf("Producer transactions to go %s\n", count);
        }

        public void runTransaction() {
            boolean succes = false;
            do {
                HeapSnapshot snapshot = heap.getActiveSnapshot();
                CommitResult result = runInsideTransaction(snapshot);
                if (result.isSuccess())
                    succes = true;
            } while (!succes);
        }

        public CommitResult runInsideTransaction(HeapSnapshot snapshot) {
            Stack stack = (Stack) snapshot.read(handle).___inflate(dummmyTransaction);
            stack.push(item);
            return heap.commit(snapshot, new InstanceIterator(stack));
        }
    }
}
