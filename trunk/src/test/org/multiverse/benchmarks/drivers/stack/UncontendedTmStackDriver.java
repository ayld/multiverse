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

import java.util.concurrent.atomic.AtomicLong;

public class UncontendedTmStackDriver extends AbstractDriver {

    //todo: this shared counter should be removed.
    private final AtomicLong toProduceCounter = new AtomicLong();
    private ProduceThread[] producers;


    @Override
    public void preRun(TestCase testCase) {
        int producerCount = testCase.getIntProperty("producerCount");
        producers = new ProduceThread[producerCount];
        for (int k = 0; k < producerCount; k++) {
            producers[k] = new ProduceThread(k);
        }

        toProduceCounter.set(testCase.getIntProperty("itemCount"));
    }

    @Override
    public void run() {
        startAll(producers);
        joinAll(producers);
    }

    public class ProduceThread extends TestThread {
        private Handle<? extends TmStack<String>> handle;

        public ProduceThread(int id) {
            super("Producer-" + id);

            handle = commit(new LinkedTmStack<String>());
        }

        @Override
        public void run() {
            while (toProduceCounter.decrementAndGet() >= 0) {
                doIt();
            }
        }

        @Atomic
        public void doIt() {
            TmStack<String> stack = getTransaction().read(handle);
            stack.push("foo");
        }
    }
}
