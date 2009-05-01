package org.multiverse.multiversionedstm.tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A performance test that measures how fast a classic stack is (LinkedBlockingDeque). This helps to compare
 * the performance of STM concurrency with old school concurrency. Not a very good test btw since it doesn't
 * include a warm up time to give the JIT a chance to do its magic.
 *
 * @author Peter Veentjer.
 */
@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
public class ClassicStackProducerConsumerTest {
    private BlockingDeque stack;

    private final AtomicInteger produceCountDown = new AtomicInteger();
    private final AtomicInteger consumeCountDown = new AtomicInteger();
    private final Object item = 100;

    private int produceCount = 50 * 1000 * 1000;
    private int producerCount = 1;
    private int consumerCount = 1;

    private long startMs;
    private long endMs;

    @Before
    public void setUp() {
        stack = new LinkedBlockingDeque();
    }

    @After
    public void tearDown() {
        long timeMs = endMs - startMs;
        System.out.printf("tranfer of %s items took %s ms\n", produceCount, timeMs);
        System.out.printf("%s transactions/second\n", (produceCount * 2 / (timeMs / 1000.0)));
    }

    @Test
    public void test() {
        produceCountDown.set(produceCount);
        consumeCountDown.set(produceCount);

        ProducerThread[] producerThreads = createProducerThreads();
        ConsumerThread[] consumerThreads = createConsumerThreads();

        startMs = System.currentTimeMillis();

        startAll(consumerThreads);
        startAll(producerThreads);

        joinAll(producerThreads);
        joinAll(consumerThreads);

        endMs = System.currentTimeMillis();
    }

    private ProducerThread[] createProducerThreads() {
        ProducerThread[] threads = new ProducerThread[producerCount];
        for (int k = 0; k < threads.length; k++)
            threads[k] = new ProducerThread(k);
        return threads;
    }

    private ConsumerThread[] createConsumerThreads() {
        ConsumerThread[] threads = new ConsumerThread[consumerCount];
        for (int k = 0; k < threads.length; k++)
            threads[k] = new ConsumerThread(k);
        return threads;
    }

    class ConsumerThread extends TestThread {
        public ConsumerThread(int id) {
            super("ConsumerThread-" + id);
        }

        @Override
        public void run() {
            while (consumeCountDown.decrementAndGet() >= 0)
                try {
                    stack.takeLast();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
        }
    }


    class ProducerThread extends TestThread {
        public ProducerThread(int id) {
            super("ProducerThread-" + id);
        }

        @Override
        public void run() {
            while (consumeCountDown.decrementAndGet() >= 0) {
                try {
                    stack.putLast(item);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
