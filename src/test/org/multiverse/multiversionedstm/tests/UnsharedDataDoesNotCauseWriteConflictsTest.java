package org.multiverse.multiversionedstm.tests;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionTemplate;
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.multiversionedstm.examples.IntegerValue;

public class UnsharedDataDoesNotCauseWriteConflictsTest {
    private MultiversionedStm stm;
    private Handle<IntegerValue>[] handles;
    private int threadCount = 10 * Runtime.getRuntime().availableProcessors();
    private int updateCountPerThread = 100000;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
        //new PrintMultiversionedStmStatisticsThread(multiversionedstm).start();
    }

    @After
    public void tearDown() {
        System.out.println(stm.getStatistics());
    }

    @Test
    public void test() {
        createValues();
        TestThread[] writeThreads = createWriteThreads();
        startAll(writeThreads);
        joinAll(writeThreads);

        assertEquals(0, stm.getStatistics().getTransactionLockAcquireFailureCount());
        assertEquals(0, stm.getStatistics().getTransactionWriteConflictCount());
    }

    private void createValues() {
        Transaction t = stm.startTransaction();
        handles = new Handle[threadCount];
        for (int k = 0; k < threadCount; k++) {
            handles[k] = t.attach(new IntegerValue());
        }
        t.commit();
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
                new TransactionTemplate(stm) {
                    @Override
                    protected Object execute(Transaction t) throws Exception {
                        IntegerValue value = t.read(handles[id]);
                        value.inc();
                        return null;
                    }
                }.execute();
            }
        }
    }
}
