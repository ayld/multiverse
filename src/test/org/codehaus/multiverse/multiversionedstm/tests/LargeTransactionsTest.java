package org.codehaus.multiverse.multiversionedstm.tests;

import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import org.codehaus.multiverse.multiversionedstm.examples.IntegerValue;
import org.codehaus.multiverse.multiversionedstm.growingheap.GrowingMultiversionedHeap;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 * A Test to simulate transactions with a lot of writes.
 *
 * @author Peter Veentjer.
 */
public class LargeTransactionsTest {
    private GrowingMultiversionedHeap heap;
    private MultiversionedStm stm;
    private long[] handles;

    private int transactionCount = 100;
    private int transactionLength = 100000;

    @Before
    public void setUp() {
        heap = new GrowingMultiversionedHeap();
        stm = new MultiversionedStm(heap);
    }

    @Test
    public void test() {
        createContent();

        for (int k = 0; k < transactionCount; k++) {
            Transaction t = stm.startTransaction();
            for (long handle : handles) {
                IntegerValue value = (IntegerValue) t.read(handle);
                value.inc();
            }
            t.commit();
            System.out.println("Executed transaction: " + k);
        }

        assertAllValues();
    }

    private void assertAllValues() {
        Transaction t = stm.startTransaction();
        for (long handle : handles) {
            IntegerValue value = (IntegerValue) t.read(handle);
            assertEquals(transactionCount, value.getValue());
        }
        t.commit();
    }

    public void createContent() {
        handles = new long[transactionLength];
        Transaction t = stm.startTransaction();
        for (int k = 0; k < handles.length; k++) {
            handles[k] = t.attachAsRoot(new IntegerValue(0));
        }
        t.commit();
    }
}
