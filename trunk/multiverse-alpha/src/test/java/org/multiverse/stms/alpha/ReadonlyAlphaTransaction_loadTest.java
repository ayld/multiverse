package org.multiverse.stms.alpha;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.GlobalStmInstance.setGlobalStmInstance;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.LoadUncommittedException;
import org.multiverse.stms.alpha.manualinstrumentation.IntRef;
import org.multiverse.stms.alpha.manualinstrumentation.IntRefTranlocal;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

/**
 * @author Peter Veentjer
 */
public class ReadonlyAlphaTransaction_loadTest {

    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = AlphaStm.createDebug();
        setGlobalStmInstance(stm);
        setThreadLocalTransaction(null);
    }

    @After
    public void after() {
        setThreadLocalTransaction(null);
    }

    public AlphaTransaction startUpdateTransaction() {
        AlphaTransaction t = stm.startUpdateTransaction(null);
        setThreadLocalTransaction(t);
        return t;
    }

    public AlphaTransaction startReadonlyTransaction() {
        AlphaTransaction t = stm.startReadOnlyTransaction(null);
        setThreadLocalTransaction(t);
        return t;
    }

    //====================== loadReadonly ====================================

    @Test
    public void loadNullReturnsNull() {
        AlphaTransaction t = startReadonlyTransaction();
        AlphaTranlocal result = t.load(null);
        assertNull(result);
    }

    @Test
    public void loadNonCommitted() {
        IntRef value = IntRef.createUncommitted();

        AlphaTransaction t = startReadonlyTransaction();

        try {
            t.load(value);
            fail();
        } catch (LoadUncommittedException ex) {
        }

        assertIsActive(t);
    }

    @Test
    public void loadPreviouslyCommitted() {
        IntRef value = new IntRef(10);

        IntRefTranlocal expected = (IntRefTranlocal) value.load(stm.getClockVersion());

        AlphaTransaction t2 = startReadonlyTransaction();
        IntRefTranlocal found = (IntRefTranlocal) t2.load(value);
        assertTrue(found.committed);
        assertSame(expected, found);
    }

    @Test
    public void loadDoesNotObserveChangesMadeByOtherTransactions() {
        IntRef ref = new IntRef(0);

        AlphaTransaction readonlyTransaction = stm.startReadOnlyTransaction(null);
        AlphaTransaction updateTransaction = stm.startUpdateTransaction(null);
        IntRefTranlocal tranlocal = (IntRefTranlocal) updateTransaction.load(ref);
        ref.inc(tranlocal);

        IntRefTranlocal tranlocalIntValue = (IntRefTranlocal) readonlyTransaction.load(ref);
        assertEquals(0, ref.get(tranlocalIntValue));
    }

    @Test
    public void loadOnCommittedTransactionFails() {
        IntRef value = new IntRef(10);

        AlphaTransaction t = startReadonlyTransaction();
        t.commit();

        try {
            t.load(value);
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertIsCommitted(t);
    }


    @Test
    public void loadOnAbortedTransactionFails() {
        IntRef value = new IntRef(10);

        AlphaTransaction t = startReadonlyTransaction();
        t.abort();

        try {
            t.load(value);
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertIsAborted(t);
    }
}