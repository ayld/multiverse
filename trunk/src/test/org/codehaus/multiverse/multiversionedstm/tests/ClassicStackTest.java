package org.codehaus.multiverse.multiversionedstm.tests;

import org.codehaus.multiverse.TestThread;
import static org.codehaus.multiverse.TestUtils.joinAll;
import static org.codehaus.multiverse.TestUtils.startAll;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A performance test that measures how fast a classic stack is. This helps to compare the performance of
 * STM concurrency with old school concurrency.
 *
 * @author Peter Veentjer.
 */
public class ClassicStackTest {
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
            threads[k] = new ProducerThread();
        return threads;
    }

    private ConsumerThread[] createConsumerThreads() {
        ConsumerThread[] threads = new ConsumerThread[consumerCount];
        for (int k = 0; k < threads.length; k++)
            threads[k] = new ConsumerThread();
        return threads;
    }

    private AtomicInteger consumerThreadIdGenerator = new AtomicInteger();

    class ConsumerThread extends TestThread {
        public ConsumerThread() {
            super("ConsumerThread-" + consumerThreadIdGenerator.incrementAndGet());
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


    private AtomicInteger producerThreadIdGenerator = new AtomicInteger();

    class ProducerThread extends TestThread {
        public ProducerThread() {
            super("ProducerThread-" + producerThreadIdGenerator.incrementAndGet());
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
