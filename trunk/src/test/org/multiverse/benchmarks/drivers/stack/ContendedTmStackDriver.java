package org.multiverse.benchmarks.drivers.stack;

import org.benchy.executor.AbstractDriver;
import org.benchy.executor.TestCase;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Handle;
import static org.multiverse.api.TransactionThreadLocal.getTransaction;
import org.multiverse.api.annotations.Atomic;
import org.multiverse.tmutils.LinkedTmStack;
import org.multiverse.tmutils.TmStack;

/**
 * todo: itemCount & producers/consumers.. if there is a difference between
 * the number of producers and consumers, the test breaks.
 *
 * @author Peter Veentjer.
 */
public class ContendedTmStackDriver extends AbstractDriver {

    private Handle<? extends TmStack<String>> stackHandle;
    private ProduceThread[] producers;
    private ConsumerThread[] consumers;
    private long itemCount;
    private int producerCount;
    private int consumerCount;

    @Override
    public void preRun(TestCase testCase) {
        itemCount = testCase.getLongProperty("itemCount");
        producerCount = testCase.getIntProperty("producerCount");
        consumerCount = testCase.getIntProperty("consumerCount");

        consumers = new ConsumerThread[consumerCount];
        for (int k = 0; k < consumerCount; k++) {
            consumers[k] = new ConsumerThread(k);
        }

        producers = new ProduceThread[producerCount];
        for (int k = 0; k < producerCount; k++) {
            producers[k] = new ProduceThread(k);
        }

        stackHandle = commit(new LinkedTmStack<String>());
    }

    @Override
    public void run() {
        startAll(producers);
        joinAll(producers);
    }

    public class ProduceThread extends TestThread {

        public ProduceThread(int id) {
            super("ProducerThread-" + id);
        }

        @Override
        public void run() {
            for (long k = 0; k < itemCount; k++) {
                doIt();
            }
        }

        @Atomic
        public void doIt() {
            TmStack<String> stack = getTransaction().read(stackHandle);
            stack.push("foo");
        }
    }

    public class ConsumerThread extends TestThread {

        public ConsumerThread(int id) {
            super("QueueThread-" + id);
        }

        @Override
        public void run() {
            for (long k = 0; k < itemCount; k++) {
                doIt();
            }
        }

        @Atomic
        public void doIt() {
            TmStack<String> stack = getTransaction().read(stackHandle);
            stack.pop();
        }
    }
}
