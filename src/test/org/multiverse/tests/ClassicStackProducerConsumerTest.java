package org.multiverse.tests;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A performance test that measures how fast a classic stack is. This helps to compare the performance of
 * STM concurrency with old school concurrency. Not a very good test btw since it doesn't include a warm up time
 * to give the JIT a chance to do its magic.
 *
 * @author Peter Veentjer.
 */
@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
public class ClassicStackProducerConsumerTest {
    private BlockingDeque stack;

    private final AtomicInteger produceCountDown = new AtomicInteger();
    private final AtomicInteger consumeCountDown = new AtomicInteger();
    private final Object item = new Integer(100);

    private int produceCount = 50 * 1000 * 1000;
    private int producerCount = 1;
    private int consumerCount = 1;

    @Before
    public void setUp() {
        stack = new LinkedBlockingDeque();
    }

    @Test
    public void test() {
        produceCountDown.set(produceCount);
        consumeCountDown.set(produceCount);

        ProducerThread[] producerThreads = createProducerThreads();
        ConsumerThread[] consumerThreads = createConsumerThreads();

        startAll(consumerThreads);
        startAll(producerThreads);

        joinAll(producerThreads);
        joinAll(consumerThreads);
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
