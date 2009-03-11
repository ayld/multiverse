package org.codehaus.multiverse.multiversionedstm.tests;

import org.codehaus.multiverse.api.Transaction;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import org.codehaus.multiverse.multiversionedstm.examples.IntegerValue;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class WritePerformanceTest {
    private MultiversionedStm stm;
    private long handle;
    private int transactionCount = 5 * 1000 * 1000;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void test() {
        setUpStructures();

        long startNs = System.nanoTime();

        for (int k = 0; k < transactionCount; k++) {
            Transaction t = stm.startTransaction();
            IntegerValue value = (IntegerValue) t.read(handle);
            value.inc();
            t.commit();
        }

        long endNs = System.nanoTime();
        double transactionPerSec = (1.0 * TimeUnit.SECONDS.toNanos(1) * transactionCount) / (endNs - startNs);
        System.out.printf("Performance is %s transactions/second\n", transactionPerSec);
    }

    private void setUpStructures() {
        Transaction t = stm.startTransaction();
        handle = t.attachAsRoot(new IntegerValue());
        t.commit();
    }
}