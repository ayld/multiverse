package org.codehaus.multiverse.multiversionedstm.tests;

import org.codehaus.multiverse.TestThread;
import static org.codehaus.multiverse.TestUtils.*;
import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.core.TransactionTemplate;
import org.codehaus.multiverse.multiversionedheap.standard.DefaultMultiversionedHeap;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import org.codehaus.multiverse.multiversionedstm.examples.IntegerValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class ConflictingWritesDontBreakSystemTest {
    private MultiversionedStm stm;
    private DefaultMultiversionedHeap heap;
    private AtomicInteger transactionCountDown = new AtomicInteger();
    private long[] handles;

    private int structureCount = 100;
    private int writerThreadCount = 10;
    private int transactionCount = 100;

    @Before
    public void setUp() {
        heap = new DefaultMultiversionedHeap();
        stm = new MultiversionedStm(heap);
    }

    @Test
    public void test() {
        transactionCountDown.set(transactionCount);
        setUpStructures();

        WriterThread[] threads = createWriterThreads();
        startAll(threads);
        joinAll(threads);

        //the 100 is quite arbitrary.. but we should have quite a number of conflicts.
        assertTrue(heap.getStatistics().commitWriteConflictCount.longValue() > 100);
        assertValues(transactionCount);
    }

    private void assertValues(int value) {
        Transaction t = stm.startTransaction();
        for (long handle : handles) {
            IntegerValue integerValue = (IntegerValue) t.read(handle);
            assertEquals(value, integerValue.get());
        }
        t.commit();
    }

    private void setUpStructures() {
        Transaction t = stm.startTransaction();
        handles = new long[structureCount];
        for (int k = 0; k < handles.length; k++)
            handles[k] = t.attachAsRoot(new IntegerValue());
        t.commit();
    }

    private WriterThread[] createWriterThreads() {
        WriterThread[] threads = new WriterThread[writerThreadCount];
        for (int k = 0; k < threads.length; k++)
            threads[k] = new WriterThread();

        return threads;
    }

    private final AtomicInteger writerThreadIdGenerator = new AtomicInteger();

    private class WriterThread extends TestThread {
        private WriterThread() {
            super("WriterThread-" + writerThreadIdGenerator.incrementAndGet());
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
                    for (int k = 0; k < handles.length; k++) {
                        IntegerValue value = (IntegerValue) t.read(handles[k]);
                        sleepRandomMs(5);
                        value.inc();
                    }
                    return null;
                }
            }.execute();
        }
    }
}
