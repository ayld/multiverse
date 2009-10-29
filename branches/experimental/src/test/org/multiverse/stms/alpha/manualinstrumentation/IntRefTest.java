package org.multiverse.stms.alpha.manualinstrumentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Transaction;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.stms.alpha.AlphaTransaction;
import org.multiverse.stms.alpha.DirtinessStatus;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.getThreadLocalTransaction;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

public class IntRefTest {
    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
        setThreadLocalTransaction(null);
    }

    public AlphaTransaction startTransaction() {
        AlphaTransaction t = stm.startUpdateTransaction(null);
        setThreadLocalTransaction(t);
        return t;
    }

    // =========================== is dirty =========================================

    @Test
    public void dirtinessStateForFreshObject() {
        AlphaTransaction t = startTransaction();
        IntRef value = new IntRef(0);
        IntRefTranlocal tranlocalValue = (IntRefTranlocal) t.privatize(value);
        assertEquals(DirtinessStatus.fresh, tranlocalValue.getDirtinessStatus());
    }

    @Test
    public void dirtinessStateForLoadedObject() {
        IntRef value = new IntRef(0);

        AlphaTransaction t = startTransaction();
        IntRefTranlocal tranlocal = (IntRefTranlocal) t.privatize(value);
        assertEquals(DirtinessStatus.clean, tranlocal.getDirtinessStatus());
    }

    @Test
    public void dirtinessStateForDirtyObject() {
        IntRef value = new IntRef(0);

        AlphaTransaction t = startTransaction();
        value.inc();
        IntRefTranlocal tranlocal = (IntRefTranlocal) t.privatize(value);

        assertEquals(DirtinessStatus.dirty, tranlocal.getDirtinessStatus());
    }

    @Test
    public void dirtinessStateForWriteConflict() {
        IntRef value = new IntRef(0);

        AlphaTransaction t1 = startTransaction();
        value.inc();

        AlphaTransaction t2 = startTransaction();
        value.inc();
        t2.commit();
        setThreadLocalTransaction(t1);

        IntRefTranlocal tranlocal = (IntRefTranlocal) t1.privatize(value);
        assertEquals(DirtinessStatus.dirty, tranlocal.getDirtinessStatus());
    }

    // ========================= atomic behavior ====================================

    @Test
    public void atomicCreation() {
        long startVersion = stm.getClockVersion();

        IntRef intValue = new IntRef(10);

        assertEquals(startVersion + 1, stm.getClockVersion());
        assertNull(getThreadLocalTransaction());
        assertEquals(10, intValue.get());
    }

    @Test
    public void atomicGet() {
        IntRef intValue = new IntRef(10);

        long startVersion = stm.getClockVersion();
        int result = intValue.get();
        assertEquals(10, result);
        assertNull(getThreadLocalTransaction());
        assertEquals(startVersion, stm.getClockVersion());
    }

    @Test
    public void atomicSet() {
        IntRef intValue = new IntRef(10);

        long startVersion = stm.getClockVersion();

        intValue.set(100);

        assertNull(getThreadLocalTransaction());
        assertEquals(startVersion + 1, stm.getClockVersion());
        assertEquals(100, intValue.get());
    }

    @Test
    public void atomicInc() {
        IntRef intValue = new IntRef(10);

        long startVersion = stm.getClockVersion();

        intValue.inc();

        assertNull(getThreadLocalTransaction());
        assertEquals(startVersion + 1, stm.getClockVersion());
        assertEquals(11, intValue.get());
    }

    // ========================= non atomic behavior =============================

    @Test
    public void existingTransaction() {
        Transaction t1 = startTransaction();
        IntRef intValue = new IntRef(10);
        t1.commit();

        Transaction t2 = startTransaction();
        assertEquals(10, intValue.get());

        intValue.inc();
        assertEquals(11, intValue.get());
        t2.commit();
    }

    @Test
    public void existingTransaction2() {
        Transaction t1 = startTransaction();
        IntRef intValue = new IntRef(10);
        t1.commit();

        Transaction t2 = startTransaction();
        assertEquals(10, intValue.get());
    }

    @Test
    public void testSingleTransaction() {
        Transaction t1 = startTransaction();
        IntRef intValue = new IntRef(10);
        intValue.inc();
        assertEquals(11, intValue.get());
        t1.commit();

        Transaction t2 = startTransaction();
        assertEquals(11, intValue.get());
    }

    @Test
    public void testRollback() {
        Transaction t1 = startTransaction();
        IntRef intValue = new IntRef(10);
        t1.commit();

        Transaction t2 = startTransaction();
        intValue.inc();
        t2.abort();

        Transaction t3 = startTransaction();
        assertEquals(10, intValue.get());
    }
}