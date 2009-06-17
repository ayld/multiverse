package org.multiverse.benchmarks.drivers.shared;

import org.benchy.TestCaseResult;
import org.benchy.executor.AbstractDriver;
import org.benchy.executor.TestCase;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Handle;
import static org.multiverse.api.TransactionThreadLocal.getTransaction;
import org.multiverse.api.annotations.Atomic;
import org.multiverse.instrumentation.IntValue;

import java.util.concurrent.TimeUnit;

public class SharedStmNoSharedDataDriver extends AbstractDriver {

    private IncThread[] threads;
    private long incCount;
    private int threadCount;

    @Override
    public void preRun(TestCase testCase) {
        incCount = testCase.getLongProperty("incCount");
        threadCount = testCase.getIntProperty("threadCount");
        threads = new IncThread[threadCount];
        for (int k = 0; k < threadCount; k++) {
            threads[k] = new IncThread(k, incCount);
        }
    }

    @Override
    public void run() {
        startAll(threads);
        joinAll(threads);
    }

    @Override
    public void postRun(TestCaseResult caseResult) {
        long transactions = incCount * threadCount;
        long durationNs = caseResult.getLongProperty("duration(ns)");
        double transactionSec = (TimeUnit.SECONDS.toNanos(1) * transactions / durationNs);
        caseResult.put("transactions/second", transactionSec);
    }

    public class IncThread extends TestThread {
        private final long incCount;
        private Handle<IntValue> handle;

        public IncThread(int id, long incCount) {
            super("IncThread-" + id);
            this.incCount = incCount;
            this.handle = commit(new IntValue());
        }

        @Override
        public void run() {
            for (int k = 0; k < incCount; k++) {
                doIt();
            }
        }

        @Atomic
        public void doIt() {
            IntValue intValue = getTransaction().read(handle);
            intValue.inc();
        }
    }
}

