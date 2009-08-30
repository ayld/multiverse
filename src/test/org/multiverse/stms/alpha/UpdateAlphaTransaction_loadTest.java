package org.multiverse.stms.alpha;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.LoadUncommittedException;
import org.multiverse.stms.alpha.manualinstrumentation.IntRef;
import org.multiverse.stms.alpha.manualinstrumentation.IntRefTranlocal;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

public class UpdateAlphaTransaction_loadTest {
    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
        setThreadLocalTransaction(null);
    }

    @After
    public void tearDown() {
        setThreadLocalTransaction(null);
    }

    public AlphaTransaction startUpdateTransaction() {
        AlphaTransaction t = (AlphaTransaction) stm.startUpdateTransaction(null);
        setThreadLocalTransaction(t);
        return t;
    }

    public AlphaTransaction startReadonlyTransaction() {
        AlphaTransaction t = (AlphaTransaction) stm.startReadOnlyTransaction(null);
        setThreadLocalTransaction(t);
        return t;
    }

    @Test
    public void loadNullReturnsNull() {
        AlphaTransaction t = startUpdateTransaction();

        AlphaTranlocal result = t.load(null);
        assertNull(result);
        assertIsActive(t);
    }

    @Test
    public void loadOfAlreadyAttachedObject() {
        AlphaTransaction t1 = startUpdateTransaction();
        IntRef intValue = new IntRef(0);
        t1.commit();

        AlphaTransaction t2 = startUpdateTransaction();
        IntRefTranlocal expected = intValue.privatize(stm.getClockVersion());
        t2.attachNew(expected);
        IntRefTranlocal found = (IntRefTranlocal) t2.load(intValue);
        assertSame(expected, found);
    }

    @Test
    public void loadObjectThatIsAlreadyReadPrivatized() {
        IntRef intValue = new IntRef(0);

        AlphaTransaction t = startUpdateTransaction();
        IntRefTranlocal loaded1 = (IntRefTranlocal) t.privatize(intValue);

        AlphaTranlocal loaded2 = t.load(intValue);

        assertSame(loaded1, loaded2);
        assertIsActive(t);
    }

    @Test
    public void loadObjectThatAlreadyIsAttachAsNew() {
        AlphaTransaction t1 = startUpdateTransaction();
        IntRef intValue = new IntRef(100);

        IntRefTranlocal tranlocalIntValue = (IntRefTranlocal) t1.load(intValue);
        assertEquals(intValue, tranlocalIntValue.getAtomicObject());
        assertEquals(100, tranlocalIntValue.value);
        assertFalse(tranlocalIntValue.committed);
    }

    @Test
    public void loadUncommittedStateFails() {
        IntRef intValue = IntRef.createUncommitted();

        AlphaTransaction t = startUpdateTransaction();
        try {
            t.load(intValue);
            fail();
        } catch (LoadUncommittedException ex) {
            //ignore
        }

        assertIsActive(t);
    }

    @Test
    public void loadFailsIfTransactionAlreadyCommitted() {
        Transaction t1 = startUpdateTransaction();
        IntRef value = new IntRef(0);
        t1.commit();

        AlphaTransaction t2 = startUpdateTransaction();
        t2.commit();

        try {
            t2.load(value);
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertIsCommitted(t2);
    }

    @Test
    public void loadFailsIfTransactionAlreadyAborted() {
        AlphaTransaction t1 = startUpdateTransaction();
        IntRef value = new IntRef(0);
        t1.commit();

        AlphaTransaction t2 = startUpdateTransaction();
        t2.abort();

        try {
            t2.load(value);
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertIsAborted(t2);
    }
}
