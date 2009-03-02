package org.codehaus.multiverse.multiversionedstm.tests;

import org.codehaus.multiverse.TestThread;
import static org.codehaus.multiverse.TestUtils.*;
import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.core.TransactionTemplate;
import org.codehaus.multiverse.multiversionedheap.standard.DefaultMultiversionedHeap;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import org.codehaus.multiverse.multiversionedstm.examples.IntegerValue;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 * A test that checks if non conflicting writes can execute in parallel.
 *
 * @author Peter Veentjer.
 */
public class NonConflcitingWritesCanBeExecutedInParallelTest {

    private int valueCount = 10;
    private int writeCount = 1000;
    private int delayMs = 50;

    private long[] handles;
    private DefaultMultiversionedHeap heap;
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        heap = new DefaultMultiversionedHeap();
        stm = new MultiversionedStm(heap);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void test() {
        createIntegerValues();

        WriteThread[] writeThreads = createWriteThreads();
        startAll(writeThreads);
        joinAll(writeThreads);

        assertAllValues(writeCount);
        assertEquals(0, heap.getStatistics().commitWriteConflictCount.longValue());
    }

    private void assertAllValues(int value) {
        Transaction t = stm.startTransaction();
        for (long handle : handles) {
            IntegerValue integerValue = (IntegerValue) t.read(handle);
            assertEquals(value, integerValue.get());
        }
        t.commit();
    }

    private WriteThread[] createWriteThreads() {
        WriteThread[] threads = new WriteThread[valueCount];
        for (int k = 0; k < threads.length; k++) {
            long handle = handles[k];
            WriteThread thread = new WriteThread(handle);
            threads[k] = thread;
        }
        return threads;
    }

    private void createIntegerValues() {
        handles = new long[valueCount];
        Transaction t = stm.startTransaction();
        for (int k = 0; k < handles.length; k++)
            handles[k] = t.attachAsRoot(new IntegerValue(0));
        t.commit();
    }

    private class WriteThread extends TestThread {
        private final long handle;

        public WriteThread(long handle) {
            setName("WriteThread");
            this.handle = handle;
        }

        @Override
        public void run() {
            for (int k = 0; k < writeCount; k++)
                doIncrease();
        }

        public void doIncrease() {
            new TransactionTemplate(stm) {
                @Override
                protected Object execute(Transaction t) throws Exception {
                    IntegerValue value = (IntegerValue) t.read(handle);

                    sleepRandomMs(delayMs);

                    value.inc();
                    return null;
                }
            }.execute();
        }
    }
}
