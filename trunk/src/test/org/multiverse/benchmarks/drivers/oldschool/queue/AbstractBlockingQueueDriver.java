package org.multiverse.benchmarks.drivers.oldschool.queue;

import org.benchy.executor.AbstractDriver;
import org.benchy.executor.TestCase;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;

import java.util.concurrent.BlockingQueue;

public abstract class AbstractBlockingQueueDriver extends AbstractDriver {

    //the object moved in the queue, same object is reused to prevent unwanted gc.
    private static final Object ITEM = new Object();

    private int producerCount;
    private long count;
    private int consumerCount;

    private ProducerThread[] producerThreads;
    private ConsumerThread[] consumerThreads;
    private BlockingQueue linkedBlockingQueue;

    public abstract BlockingQueue createQueue();

    @Override
    public void preRun(TestCase testCase) {
        producerCount = testCase.getIntProperty("producerCount");
        consumerCount = testCase.getIntProperty("consumerCount");
        count = testCase.getLongProperty("count");
        linkedBlockingQueue = createQueue();

        producerThreads = new ProducerThread[producerCount];
        for (int k = 0; k < producerCount; k++) {
            producerThreads[k] = new ProducerThread(k, count);
        }

        consumerThreads = new ConsumerThread[consumerCount];
        for (int k = 0; k < consumerCount; k++) {
            consumerThreads[k] = new ConsumerThread(k, count);
        }
    }

    @Override
    public void run() {
        startAll(producerThreads);
        startAll(consumerThreads);

        joinAll(producerThreads);
        joinAll(consumerThreads);
    }

    //todo: transactions/second

    private class ProducerThread extends TestThread {
        private final long count;

        public ProducerThread(int id, long count) {
            super("ProducerThread-" + id);
            this.count = count;
        }

        @Override
        public void run() {
            try {
                for (long k = 0; k < count; k++) {
                    linkedBlockingQueue.put(ITEM);
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException();
            }
        }
    }

    private class ConsumerThread extends TestThread {
        private final long count;

        public ConsumerThread(int id, long count) {
            super("ConsumerThread-" + id);
            this.count = count;
        }

        @Override
        public void run() {
            try {
                for (long k = 0; k < count; k++) {
                    linkedBlockingQueue.take();
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException();
            }
        }
    }
}
