package org.multiverse.benchmarks.drivers.shared;

import org.benchy.TestCaseResult;
import org.benchy.executor.AbstractDriver;
import org.benchy.executor.TestCase;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Handle;
import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionTemplate;
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.multiversionedstm.manualinstrumented.ManualIntValue;

import java.util.concurrent.TimeUnit;

/**
 * The ManuallyUnsharedDriver improves upon the UnsharedDriver that the instrumentation
 * is not used. The consequence of using instrumentation, is that the global stm instance
 * needs to be read, and this requires a volatile read. But the transactionthreadlocal
 * is still needed.
 *
 * @author Peter Veentjer.
 */
public class SharedStmNoSharedDataAndManualDriver extends AbstractDriver {

    private IncThread[] threads;
    private long incCount;
    private int threadCount;
    private Stm stm;

    @Override
    public void preRun(TestCase testCase) {
        stm = new MultiversionedStm(null);
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
        private Handle<ManualIntValue> handle;

        public IncThread(int id, long incCount) {
            super("IncThread-" + id);
            this.incCount = incCount;
            this.handle = commit(stm, new ManualIntValue());
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
                    ManualIntValue intValue = t.read(handle);
                    intValue.inc();
                    return null;
                }
            }.execute();
        }
    }
}

