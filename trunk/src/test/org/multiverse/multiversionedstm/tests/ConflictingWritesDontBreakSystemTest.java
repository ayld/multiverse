package org.multiverse.multiversionedstm.tests;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Originator;
import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionTemplate;
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.multiversionedstm.examples.IntegerValue;

import java.util.concurrent.atomic.AtomicInteger;

public class ConflictingWritesDontBreakSystemTest {
    private MultiversionedStm stm;
    private AtomicInteger transactionCountDown = new AtomicInteger();
    private Originator<IntegerValue>[] originators;

    private int structureCount = 100;
    private int writerThreadCount = 10;
    private int transactionCount = 100;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @After
    public void tearDown() {
        System.out.println(stm.getStatistics());
    }

    @Test
    public void test() {
        transactionCountDown.set(transactionCount);
        setUpStructures();

        WriterThread[] threads = createWriterThreads();
        startAll(threads);
        joinAll(threads);

        //the 10 is quite arbitrary.. but we should have quite a number of conflicts.
        assertTrue(stm.getStatistics().getTransactionWriteConflictCount() > 10);
        assertValues(transactionCount);
    }

    private void assertValues(int value) {
        Transaction t = stm.startTransaction();
        for (Originator<IntegerValue> originator : originators) {
            IntegerValue integerValue = t.read(originator);
            assertEquals(value, integerValue.get());
        }
        t.commit();
    }

    private void setUpStructures() {
        Transaction t = stm.startTransaction();
        originators = new Originator[structureCount];
        for (int k = 0; k < originators.length; k++) {
            originators[k] = t.attach(new IntegerValue());
        }
        t.commit();
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
        public void run() {
            while (transactionCountDown.decrementAndGet() >= 0)
                doTransaction();
        }

        public void doTransaction() {
            new TransactionTemplate(stm) {
                @Override
                protected Object execute(Transaction t) {
                    for (int k = 0; k < originators.length; k++) {
                        IntegerValue value = (IntegerValue) t.read(originators[k]);
                        sleepRandomMs(5);
                        value.inc();
                    }
                    return null;
                }
            }.execute();
        }
    }
}
