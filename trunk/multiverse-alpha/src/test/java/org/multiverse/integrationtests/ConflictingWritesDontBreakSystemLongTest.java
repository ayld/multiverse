package org.multiverse.integrationtests;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.datastructures.refs.IntRef;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

import java.util.concurrent.atomic.AtomicInteger;

public class ConflictingWritesDontBreakSystemLongTest {
    private AtomicInteger transactionCountDown = new AtomicInteger();
    private IntRef[] values;

    private int structureCount = 100;
    private int writerThreadCount = 10;
    private int transactionCount = 100;

    @Before
    public void setUp() {
        setThreadLocalTransaction(null);
    }

    @After
    public void tearDown() {
        //stm.getProfiler().print();
        setThreadLocalTransaction(null);
    }

    @Test
    public void test() {
        transactionCountDown.set(transactionCount);
        setUpStructures();

        WriterThread[] threads = createWriterThreads();
        startAll(threads);
        joinAll(threads);

        //the 10 is quite arbitrary.. but we should have quite a number of conflicts.
        //stm.getProfiler().print();
        //assertTrue(stm.getStatistics().getUpdateTransactionWriteConflictCount() > 10);
        assertValues(transactionCount);
    }

    private void assertValues(int value) {
        for (IntRef intValue : values) {
            assertEquals(value, intValue.get());
        }
    }

    private void setUpStructures() {
        values = new IntRef[structureCount];
        for (int k = 0; k < values.length; k++) {
            values[k] = new IntRef(0);
        }
    }

    private WriterThread[] createWriterThreads() {
        WriterThread[] threads = new WriterThread[writerThreadCount];
        for (int k = 0; k < threads.length; k++)
            threads[k] = new WriterThread(k);

        return threads;
    }

    private class WriterThread extends TestThread {
        private WriterThread(int id) {
            super("WriterThread-" + id);
        }

        @Override
        public void doRun() {
            while (transactionCountDown.decrementAndGet() >= 0) {
                doTransaction();
            }
        }

        @AtomicMethod
        public void doTransaction() {
            for (int k = 0; k < values.length; k++) {
                IntRef value = values[k];
                sleepRandomMs(5);
                value.inc();
            }
        }
    }
}
