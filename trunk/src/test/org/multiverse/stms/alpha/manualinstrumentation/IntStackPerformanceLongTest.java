package org.multiverse.stms.alpha.manualinstrumentation;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;
import org.multiverse.stms.alpha.AlphaStm;

import java.util.concurrent.TimeUnit;

public class IntStackPerformanceLongTest {

    private int count = 30 * 1000 * 1000;

    private Stm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        setThreadLocalTransaction(null);
    }

    public Transaction startTransaction() {
        Transaction t = stm.startUpdateTransaction();
        setThreadLocalTransaction(t);
        return t;
    }

    @Test
    public void test() {
        Transaction t = startTransaction();
        IntStack stack = new IntStack();
        t.commit();

        long startNs = System.nanoTime();

        for (int k = 0; k < count; k++) {
            Transaction t2 = startTransaction();
            stack.push(10);
            t2.commit();

            Transaction t3 = startTransaction();
            stack.pop();
            t3.commit();
        }

        long periodNs = System.nanoTime() - startNs;
        double transactionPerSecond = (count * 2.0d * TimeUnit.SECONDS.toNanos(1)) / periodNs;
        System.out.printf("%s Transaction/second\n", transactionPerSecond);
    }

    @Test
    public void testOptimized() {
        Transaction t = startTransaction();
        IntStack stack = new IntStack();
        t.commit();

        long startNs = System.nanoTime();

        for (int k = 0; k < count; k++) {
            Transaction t2 = startTransaction();
            stack.push(t2, 10);
            t2.commit();

            Transaction t3 = startTransaction();
            stack.pop(t3);
            t3.commit();
        }

        long periodNs = System.nanoTime() - startNs;
        double transactionPerSecond = (count * 2.0d * TimeUnit.SECONDS.toNanos(1)) / periodNs;
        System.out.printf("%s Transaction/second\n", transactionPerSecond);
    }
}
