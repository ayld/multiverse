package org.multiverse.multiversionedstm.tests;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.multiversionedstm.examples.ExampleIntegerValue;

public class LargeNonParallelWriteonlyTransactionsLongTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @After
    public void tearDown() {
        System.out.println(stm.getStatistics());
    }

    @Test
    public void test_1() {
        test(1);
    }

    @Test
    public void test_10() {
        test(10);
    }

    @Test
    public void test_100() {
        test(100);
    }

    @Test
    public void test_1000() {
        test(1000);
    }

    @Test
    public void test_10000() {
        test(10000);
    }

    @Test
    public void test_100000() {
        test(100000);
    }

    @Test
    public void test_1000000() {
        test(1000000);
    }

    public void test(int x) {
        Transaction t = stm.startTransaction();
        for (int k = 0; k < x; k++) {
            ExampleIntegerValue value = new ExampleIntegerValue();
            t.attach(value);
        }
        t.commit();

        assertEquals(0, stm.getStatistics().getTransactionLockAcquireFailureCount());
        assertEquals(0, stm.getStatistics().getTransactionWriteConflictCount());
        assertEquals(1, stm.getStatistics().getTransactionCommittedCount());
        assertEquals(0, stm.getStatistics().getTransactionRetriedCount());
    }
}
