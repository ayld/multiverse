package org.multiverse.stms.alpha.manualinstrumentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.DirtinessStatus;
import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;
import org.multiverse.stms.alpha.AlphaStm;

public class IntStackTest {
    private Stm stm;

    @Before
    public void setUp() {
        setThreadLocalTransaction(null);
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
    }

    public Transaction startTransaction() {
        Transaction t = stm.startUpdateTransaction();
        setThreadLocalTransaction(t);
        return t;
    }

    @Test
    public void testNewStackIsDirtyByDefault() {
        Transaction t = startTransaction();
        IntStack intStack = new IntStack();
        IntStackTranlocal tranlocalIntStack = (IntStackTranlocal) t.privatize(intStack);
        assertEquals(DirtinessStatus.fresh, tranlocalIntStack.getDirtinessStatus());
    }

    @Test
    public void loadedStackIsNotDirty() {
        IntStack intStack = new IntStack();

        Transaction t = startTransaction();
        IntStackTranlocal tranlocalIntStack = (IntStackTranlocal) t.privatize(intStack);
        assertEquals(DirtinessStatus.clean, tranlocalIntStack.getDirtinessStatus());
    }

    @Test
    public void modifiedStackIsDirty() {
        IntStack intStack = new IntStack();

        Transaction t = startTransaction();
        intStack.push(1);
        IntStackTranlocal tranlocalIntStack = (IntStackTranlocal) t.privatize(intStack);

        assertEquals(DirtinessStatus.dirty, tranlocalIntStack.getDirtinessStatus());
    }

    @Test
    public void testEmptyStack() {
        Transaction t1 = startTransaction();
        IntStack intStack = new IntStack();
        assertTrue(intStack.isEmpty());
        t1.commit();

        Transaction t2 = startTransaction();
        assertTrue(intStack.isEmpty());
    }

    @Test
    public void testNonEmptyStack() {
        Transaction t1 = startTransaction();
        IntStack intStack = new IntStack();
        intStack.push(5);
        intStack.push(10);
        assertEquals(2, intStack.size());
        t1.commit();

        Transaction t2 = startTransaction();
        assertEquals(2, intStack.size());
        assertEquals(10, intStack.pop());
        assertEquals(5, intStack.pop());
    }

    @Test
    public void testRollback() {
        Transaction t1 = startTransaction();
        IntStack intStack = new IntStack();
        intStack.push(10);
        t1.commit();

        Transaction t2 = startTransaction();
        assertEquals(10, intStack.pop());
        t2.abort();

        Transaction t3 = startTransaction();
        assertEquals(1, intStack.size());
        assertEquals(10, intStack.pop());
    }

    @Test
    public void testPushAndPop() {
        Transaction t1 = startTransaction();
        IntStack intStack = new IntStack();
        t1.commit();

        Transaction t2 = startTransaction();
        intStack.push(1);
        t2.commit();

        Transaction t3 = startTransaction();
        int popped = intStack.pop();
        t3.commit();

        assertEquals(1, popped);
    }
}
