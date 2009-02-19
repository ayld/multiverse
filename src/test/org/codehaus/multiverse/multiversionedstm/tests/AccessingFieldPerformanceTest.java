package org.codehaus.multiverse.multiversionedstm.tests;

import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import org.codehaus.multiverse.multiversionedstm.examples.IntegerValue;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Multiverse doesn't instrument reads/writes of normal fields, so  the performance of reads and writes is not
 * impacted and the compiler can do all his tricks to make code faster. This tests makes sure that the reads and
 * writes are f*cking fast. On my dual core laptop, the test runs in a few hundred ms.
 *
 * @author Peter Veentjer.
 */
public class AccessingFieldPerformanceTest {

    private MultiversionedStm stm;
    private long handle;
    private int transactionCount = Integer.MAX_VALUE;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void test() {
        setUpStructures();

        long startNs = System.nanoTime();

        Transaction t = stm.startTransaction();
        IntegerValue value = (IntegerValue) t.read(handle);
        for (int k = 0; k < transactionCount; k++)
            value.inc();
        t.commit();

        long endNs = System.nanoTime();

        double transactionPerSec = (1.0 * TimeUnit.SECONDS.toNanos(1) * transactionCount) / (endNs - startNs);
        System.out.printf("Performance is %s inc()/second\n", transactionPerSec);

        assertValue(transactionCount);
    }

    private void assertValue(int expected) {
        Transaction t = stm.startTransaction();
        IntegerValue integerValue = (IntegerValue) t.read(handle);
        assertEquals(expected, integerValue.getValue());
        t.commit();
    }

    private void setUpStructures() {
        Transaction t = stm.startTransaction();
        handle = t.attachAsRoot(new IntegerValue());
        t.commit();
    }
}
