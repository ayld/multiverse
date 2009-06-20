package org.multiverse.benchmarks.drivers.shared;

import org.benchy.TestCaseResult;
import org.benchy.executor.AbstractDriver;
import org.benchy.executor.TestCase;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.*;
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.multiversionedstm.examples.ExampleIntValue;

import java.util.concurrent.TimeUnit;

public class NoSharedStmNoSharedDataAndManualDriver extends AbstractDriver {

    private IncThread[] threads;
    private long incCount;
    private int threadCount;

    @Override
    public void preRun(TestCase testCase) {
        GlobalStmInstance.setInstance(new MultiversionedStm(null));
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
        private Handle<ExampleIntValue> handle;
        private final Stm stm = new MultiversionedStm(null);

        public IncThread(int id, long incCount) {
            super("IncThread-" + id);
            this.incCount = incCount;
            this.handle = commit(stm, new ExampleIntValue());
        }

        @Override
        public void run() {
            for (int k = 0; k < incCount; k++) {
                doIt();
            }
        }

        public void doIt() {
            new TransactionTemplate(stm, false) {
                @Override
                protected Object execute(Transaction t) throws Exception {
                    ExampleIntValue intValue = t.read(handle);
                    intValue.inc();
                    return null;
                }
            }.execute();
        }
    }
}

