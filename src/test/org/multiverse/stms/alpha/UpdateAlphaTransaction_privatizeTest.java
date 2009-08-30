package org.multiverse.stms.alpha;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.DummyTransaction;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.LoadUncommittedException;
import org.multiverse.stms.alpha.manualinstrumentation.IntRef;
import org.multiverse.stms.alpha.manualinstrumentation.IntRefTranlocal;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

/**
 * @author Peter Veentjer
 */
public class UpdateAlphaTransaction_privatizeTest {

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

    @Test
    public void loadFailsIfLocked() {
        IntRef value = new IntRef(0);
        Transaction owner = new DummyTransaction();
        value.tryLock(owner);

        AlphaTransaction t = startUpdateTransaction();
        IntRefTranlocal read = (IntRefTranlocal) t.privatize(value);
        //todo
    }

    @Test
    public void loadWithNullArgumentReturnsNull() {
        AlphaTransaction t = startUpdateTransaction();

        AlphaTranlocal result = t.privatize(null);

        assertNull(result);
        assertIsActive(t);
    }

    @Test
    public void loadOnCommittedValue() {
        AlphaTransaction t1 = startUpdateTransaction();
        IntRef intValue = new IntRef(0);
        t1.commit();

        IntRefTranlocal committed = (IntRefTranlocal) intValue.load(stm.getClockVersion());

        AlphaTransaction t2 = startUpdateTransaction();
        IntRefTranlocal read = (IntRefTranlocal) t2.privatize(intValue);
        assertTrue(committed != read);
        assertEquals(intValue, read.getAtomicObject());
        assertEquals(committed.value, read.value);
        assertFalse(read.committed);
        //version doesn't need to be checked since it is not defined for a non committed value
        assertIsActive(t2);
    }

    @Test
    public void loadOnAlreadyLoadedValue() {
        AlphaTransaction t1 = startUpdateTransaction();
        IntRef intValue = new IntRef(0);
        t1.commit();

        AlphaTransaction t2 = startUpdateTransaction();
        IntRefTranlocal read1 = (IntRefTranlocal) t2.privatize(intValue);
        IntRefTranlocal read2 = (IntRefTranlocal) t2.privatize(intValue);
        assertSame(read1, read2);
        assertIsActive(t2);
    }

    @Test
    public void loadOnAlreadyAttachedValue() {
        AlphaTransaction t1 = startUpdateTransaction();
        IntRef intValue = new IntRef(20);
        IntRefTranlocal read1 = (IntRefTranlocal) t1.privatize(intValue);

        assertEquals(intValue, read1.getAtomicObject());
        assertFalse(read1.committed);
        assertIsActive(t1);
        t1.commit();

        IntRefTranlocal read2 = (IntRefTranlocal) intValue.load(stm.getClockVersion());
        assertSame(read1, read2);
    }

    @Test
    public void loadOnDifferentTransactionsReturnDifferentInstances() {
        IntRef value = new IntRef(1);

        AlphaTransaction t1 = (AlphaTransaction) stm.startUpdateTransaction(null);
        IntRefTranlocal found1 = (IntRefTranlocal) t1.privatize(value);

        AlphaTransaction t2 = (AlphaTransaction) stm.startUpdateTransaction(null);
        IntRefTranlocal found2 = (IntRefTranlocal) t2.privatize(value);

        assertNotSame(found1, found2);
        assertEquals(found1.value, found2.value);
        assertEquals(found1.committed, found2.committed);
        assertEquals(found1.getAtomicObject(), found2.getAtomicObject());
        //version doesn't need to be checked since it is undefined while not committed.
    }

    @Test
    public void loadFailsOnUncommittedObject() {
        IntRef value = IntRef.createUncommitted();

        AlphaTransaction t = (AlphaTransaction) stm.startUpdateTransaction(null);

        try {
            t.privatize(value);
            fail();
        } catch (LoadUncommittedException ex) {
        }

        assertIsActive(t);
    }

    @Test
    public void loadFailsIfTransactionAlreadyCommitted() {
        AlphaTransaction t1 = startUpdateTransaction();
        IntRef value = new IntRef(0);
        t1.commit();

        AlphaTransaction t2 = startUpdateTransaction();
        t2.commit();

        try {
            t2.privatize(value);
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
            t2.privatize(value);
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertIsAborted(t2);
    }
}
