package org.multiverse.stms.alpha.instrumentation.asm;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.stms.alpha.AlphaTransaction;
import org.multiverse.stms.alpha.ReadonlyAlphaTransaction;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.getThreadLocalTransaction;

public class AtomicMethod_ReadonlyUpdateTransactionTest {

    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
    }

    @Test
    public void instanceReadonlyMethod() {
        long version = stm.getClockVersion();

        InstanceReadonlyMethod method = new InstanceReadonlyMethod();
        method.execute();

        AlphaTransaction transaction = method.transaction;

        assertEquals(version, stm.getClockVersion());
        assertNotNull(transaction);
        assertEquals(version, transaction.getReadVersion());
        assertTrue(transaction instanceof ReadonlyAlphaTransaction);
    }

    static class InstanceReadonlyMethod {
        AlphaTransaction transaction;

        @AtomicMethod(readonly = true)
        public void execute() {
            transaction = (AlphaTransaction) getThreadLocalTransaction();
        }
    }

    @Test
    public void staticReadonlyMethod() {
        long version = stm.getClockVersion();

        StaticReadonlyMethod.execute();

        AlphaTransaction transaction = StaticReadonlyMethod.transaction;

        assertEquals(version, stm.getClockVersion());
        assertNotNull(transaction);
        assertEquals(version, transaction.getReadVersion());
        assertTrue(transaction instanceof ReadonlyAlphaTransaction);
    }

    static class StaticReadonlyMethod {

        static AlphaTransaction transaction;

        @AtomicMethod(readonly = true)
        public static void execute() {
            transaction = (AlphaTransaction) getThreadLocalTransaction();
        }
    }
}
