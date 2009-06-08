package org.multiverse.benchmark;

import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Handle;
import static org.multiverse.api.TransactionThreadLocal.getTransaction;
import org.multiverse.api.annotations.Atomic;
import org.multiverse.tmutils.LinkedTmStack;
import org.multiverse.tmutils.TmStack;

import java.util.concurrent.atomic.AtomicLong;

public class StackStressTestDriver extends AbstractDriver {

    private final AtomicLong toProduceCounter = new AtomicLong();
    private Handle<? extends TmStack<String>> handle;
    private ProduceThread[] producers;

    @Override
    public void setUp() {
        handle = commit(new LinkedTmStack<String>());
    }

    @Override
    public void initRun(TestCase testCase) {
        int producerCount = testCase.getIntProperty("producercount");
        producers = new ProduceThread[producerCount];
        for(int k=0;k<producerCount;k++){
           producers[k]=new ProduceThread(k);
        }

        handle = commit(new LinkedTmStack<String>());
        toProduceCounter.set(testCase.getIntProperty("itemcount"));
    }

    @Override
    public void run(TestResult testResult) {
        startAll(producers);
        joinAll(producers);
    }

    public class ProduceThread extends TestThread {

        public ProduceThread(int id) {
            super("Producer-" + id);
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
