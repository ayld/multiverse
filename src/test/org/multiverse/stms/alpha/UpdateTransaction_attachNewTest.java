package org.multiverse.stms.alpha;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.DummyTranlocal;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.PanicError;
import org.multiverse.stms.alpha.manualinstrumentation.IntRef;
import org.multiverse.stms.alpha.manualinstrumentation.IntRefTranlocal;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

/**
 * @author Peter Veentjer
 */
public class UpdateTransaction_attachNewTest {
    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
        setThreadLocalTransaction(null);
    }

    public void tearDown() {
        setThreadLocalTransaction(null);
    }

    public AlphaTransaction startUpdateTransaction() {
        AlphaTransaction t = (AlphaTransaction) stm.startUpdateTransaction();
        setThreadLocalTransaction(t);
        return t;
    }

    public AlphaTransaction startReadonlyTransaction() {
        AlphaTransaction t = (AlphaTransaction) stm.startReadOnlyTransaction();
        setThreadLocalTransaction(t);
        return t;
    }

    @Test
    public void attachNew() {
        Transaction t = startUpdateTransaction();
        IntRef atomicObject = IntRef.createUncommitted();
        IntRefTranlocal tranlocal = new IntRefTranlocal(atomicObject, 0);

        long version = stm.getClockVersion();
        ((AlphaTransaction) t).attachNew(tranlocal);

        assertIsActive(t);
        assertEquals(version, stm.getClockVersion());
    }

    @Test
    public void attachCommittedShouldFail() {
        //todo: this test is going to fail if the stm is not running with the sanity check disabled.

        IntRef ref = new IntRef(10);
        IntRefTranlocal committedTranlocal = (IntRefTranlocal) ref.load(stm.getClockVersion());

        AlphaTransaction t = startUpdateTransaction();

        long version = stm.getClockVersion();

        try {
            t.attachNew(committedTranlocal);
            fail();
        } catch (PanicError e) {

        }

        assertIsActive(t);
        assertEquals(version, stm.getClockVersion());
    }

    @Test
    public void attachNewCommittedObject() {
        AlphaTransaction t1 = startUpdateTransaction();
        IntRef intValue = new IntRef(0);
        t1.commit();

        AlphaTransaction t2 = startUpdateTransaction();
        IntRefTranlocal x = intValue.privatize(stm.getClockVersion());
        t2.attachNew(x);

        assertIsActive(t2);
    }

    @Test
    public void attachNewAlreadyAttached() {
        AlphaTransaction t1 = startUpdateTransaction();
        IntRef value = new IntRef(0);
        t1.commit();

        AlphaTransaction t2 = startUpdateTransaction();
        IntRefTranlocal x = value.privatize(stm.getClockVersion());
        t2.attachNew(x);
        t2.attachNew(x);

        assertIsActive(t2);
    }

    @Test
    public void attachNewReadPrivatizedValue() {
        AlphaTransaction t1 = startUpdateTransaction();
        IntRef value = new IntRef(0);
        t1.commit();

        AlphaTransaction t2 = startUpdateTransaction();
        IntRefTranlocal tranlocalValue = (IntRefTranlocal) t2.privatize(value);
        t2.attachNew(tranlocalValue);

        assertIsActive(t2);
    }

    @Test
    public void attachNewWithNullArgumentFails() {
        AlphaTransaction t = startUpdateTransaction();
        try {
            t.attachNew(null);
            fail();
        } catch (NullPointerException ex) {
        }

        assertIsActive(t);
    }

    @Test
    public void attachNewOnAbortedTransactionFails() {
        AlphaTransaction t = startUpdateTransaction();
        t.abort();

        try {
            t.attachNew(new DummyTranlocal());
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertIsAborted(t);
    }

    @Test
    public void attachNewOnCommittedTransactionFails() {
        AlphaTransaction t = startUpdateTransaction();
        t.commit();

        try {
            t.attachNew(new DummyTranlocal());
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertIsCommitted(t);
    }
}
