package org.multiverse.stms.alpha;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Stm;
import org.multiverse.stms.alpha.Tranlocal;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.LoadUncommittedAtomicObjectException;
import org.multiverse.stms.alpha.manualinstrumentation.IntRef;
import org.multiverse.stms.alpha.manualinstrumentation.IntRefTranlocal;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

public class UpdateTransaction_loadTest {
    private Stm stm;

    @Before
    public void setUp() {
        stm = GlobalStmInstance.get();
    }

    @After
    public void tearDown() {
        setThreadLocalTransaction(null);
    }

    public Transaction startUpdateTransaction() {
        Transaction t = stm.startUpdateTransaction();
        setThreadLocalTransaction(t);
        return t;
    }

    public Transaction startReadonlyTransaction() {
        Transaction t = stm.startReadOnlyTransaction();
        setThreadLocalTransaction(t);
        return t;
    }

    @Test
    public void loadNullReturnsNull() {
        Transaction t = startUpdateTransaction();

        Tranlocal result = t.load(null);
        assertNull(result);
        assertIsActive(t);
    }

    @Test
    public void loadNonTransactionalObject() {
        Transaction t = startUpdateTransaction();

        try {
            t.load("foo");
            fail();
        } catch (IllegalArgumentException ex) {
        }

        assertIsActive(t);
    }

    @Test
    public void loadOfAlreadyAttachedObject() {
        Transaction t1 = startUpdateTransaction();
        IntRef intValue = new IntRef(0);
        t1.commit();

        Transaction t2 = startUpdateTransaction();
        IntRefTranlocal expected = intValue.privatize(stm.getClockVersion());
        t2.attachNew(expected);
        IntRefTranlocal found = (IntRefTranlocal) t2.load(intValue);
        assertSame(expected, found);
    }

    @Test
    public void loadObjectThatIsAlreadyReadPrivatized() {
        IntRef intValue = new IntRef(0);

        Transaction t = startUpdateTransaction();
        IntRefTranlocal loaded1 = (IntRefTranlocal) t.privatize(intValue);

        Tranlocal loaded2 = t.load(intValue);

        assertSame(loaded1, loaded2);
        assertIsActive(t);
    }

    @Test
    public void loadObjectThatAlreadyIsAttachAsNew() {
        Transaction t1 = startUpdateTransaction();
        IntRef intValue = new IntRef(100);

        IntRefTranlocal tranlocalIntValue = (IntRefTranlocal) t1.load(intValue);
        assertEquals(intValue, tranlocalIntValue.getAtomicObject());
        assertEquals(100, tranlocalIntValue.value);
        assertFalse(tranlocalIntValue.committed);
    }

    @Test
    public void loadUncommittedStateFails() {
        IntRef intValue = IntRef.createUncommitted();

        Transaction t = startUpdateTransaction();
        try {
            t.load(intValue);
            fail();
        } catch (LoadUncommittedAtomicObjectException ex) {
           //ignore
        }

        assertIsActive(t);
    }

    @Test
    public void loadFailsIfTransactionAlreadyCommitted() {
        Transaction t1 = startUpdateTransaction();
        IntRef value = new IntRef(0);
        t1.commit();

        Transaction t2 = startUpdateTransaction();
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
        Transaction t1 = startUpdateTransaction();
        IntRef value = new IntRef(0);
        t1.commit();

        Transaction t2 = startUpdateTransaction();
        t2.abort();

        try {
            t2.load(value);
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertIsAborted(t2);
    }
}
