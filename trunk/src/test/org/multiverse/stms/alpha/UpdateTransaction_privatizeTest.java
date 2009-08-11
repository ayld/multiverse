package org.multiverse.stms.alpha;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.DummyTransaction;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Stm;
import org.multiverse.api.Tranlocal;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.stms.alpha.manualinstrumentation.IntRef;
import org.multiverse.stms.alpha.manualinstrumentation.IntRefTranlocal;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

/**
 * @author Peter Veentjer
 */
public class UpdateTransaction_privatizeTest {

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

    @Test
    public void loadFailsWithNonTransactionalObjectAsArgument() {
        Transaction t = startUpdateTransaction();

        try {
            t.privatize("foo");
            fail();
        } catch (IllegalArgumentException ex) {
        }

        assertIsActive(t);
    }

    @Test
    public void loadFailsIfLocked() {
        IntRef value = new IntRef(0);
        Transaction owner = new DummyTransaction();
        value.acquireLock(owner);

        Transaction t = startUpdateTransaction();
        IntRefTranlocal read = (IntRefTranlocal) t.privatize(value);
        //todo
    }

    @Test
    public void loadWithNullArgumentReturnsNull() {
        Transaction t = startUpdateTransaction();

        Tranlocal result = t.privatize(null);

        assertNull(result);
        assertIsActive(t);
    }

    @Test
    public void loadOnCommittedValue() {
        Transaction t1 = startUpdateTransaction();
        IntRef intValue = new IntRef(0);
        t1.commit();

        IntRefTranlocal committed = (IntRefTranlocal) intValue.load(stm.getClockVersion());

        Transaction t2 = startUpdateTransaction();
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
        Transaction t1 = startUpdateTransaction();
        IntRef intValue = new IntRef(0);
        t1.commit();

        Transaction t2 = startUpdateTransaction();
        IntRefTranlocal read1 = (IntRefTranlocal) t2.privatize(intValue);
        IntRefTranlocal read2 = (IntRefTranlocal) t2.privatize(intValue);
        assertSame(read1, read2);
        assertIsActive(t2);
    }

    @Test
    public void loadOnAlreadyAttachedValue() {
        Transaction t1 = startUpdateTransaction();
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

        Transaction t1 = stm.startUpdateTransaction();
        IntRefTranlocal found1 = (IntRefTranlocal) t1.privatize(value);

        Transaction t2 = stm.startUpdateTransaction();
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

        Transaction t = stm.startUpdateTransaction();
        Tranlocal result = t.privatize(value);
        assertNull(result);

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
            t2.privatize(value);
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
            t2.privatize(value);
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertIsAborted(t2);
    }
}
