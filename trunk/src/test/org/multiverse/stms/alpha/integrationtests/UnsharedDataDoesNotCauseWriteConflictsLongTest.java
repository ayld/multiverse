package org.multiverse.stms.alpha.integrationtests;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;
import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.stms.alpha.manualinstrumentation.IntRef;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

import java.util.concurrent.TimeUnit;

public class UnsharedDataDoesNotCauseWriteConflictsLongTest {
    private AlphaStm stm;
    private IntRef[] values;
    private int threadCount = 4;// * Runtime.getRuntime().availableProcessors();
    private int updateCountPerThread = 20000000;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
        setThreadLocalTransaction(null);
        //new PrintMultiversionedStmStatisticsThread(multiversionedstm).start();
    }

    @After
    public void tearDown() {
        stm.getStatistics().print();
    }

    @Test
    public void test() {
        createValues();
        TestThread[] writeThreads = createWriteThreads();

        long startNs = System.nanoTime();

        startAll(writeThreads);
        joinAll(writeThreads);

        long periodNs = System.nanoTime() - startNs;
        double transactionPerSecond = (updateCountPerThread * threadCount * 1.0d * TimeUnit.SECONDS.toNanos(1)) / periodNs;
        System.out.printf("%s Transaction/second\n", transactionPerSecond);

        assertEquals(0, stm.getStatistics().getUpdateTransactionLockAcquireFailureCount());
        assertEquals(0, stm.getStatistics().getUpdateTransactionWriteConflictCount());
    }

    private void createValues() {
        values = new IntRef[threadCount];
        for (int k = 0; k < threadCount; k++) {
            values[k] = new IntRef(0);
        }
    }

    private WriteThread[] createWriteThreads() {
        WriteThread[] result = new WriteThread[threadCount];
        for (int k = 0; k < threadCount; k++) {
            result[k] = new WriteThread(k);
        }
        return result;
    }

    private class WriteThread extends TestThread {
        private final int id;

        WriteThread(int id) {
            super("TestThread-" + id);
            this.id = id;
        }

        public void run() {
            for (int k = 0; k < updateCountPerThread; k++) {
                doIt();
            }
        }

        @AtomicMethod
        private void doIt() {
            IntRef value = values[id];
            value.inc();
        }
    }
}