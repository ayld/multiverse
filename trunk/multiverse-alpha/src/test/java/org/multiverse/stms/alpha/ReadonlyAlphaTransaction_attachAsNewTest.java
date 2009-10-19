package org.multiverse.stms.alpha;

import org.junit.After;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.GlobalStmInstance;
import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.stms.alpha.manualinstrumentation.IntRef;
import org.multiverse.stms.alpha.manualinstrumentation.IntRefTranlocal;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

/**
 * @author Peter Veentjer
 */
public class ReadonlyAlphaTransaction_attachAsNewTest {

    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = AlphaStm.createDebug();
        GlobalStmInstance.set(stm);
        setThreadLocalTransaction(null);
    }

    @After
    public void after() {
        setThreadLocalTransaction(null);
    }

    public AlphaTransaction startReadonlyTransaction() {
        AlphaTransaction t = stm.startReadOnlyTransaction(null);
        setThreadLocalTransaction(t);
        return t;
    }

    @Test
    public void attachAsNewOnStartedTransactionShouldFail() {
        AlphaTransaction t = startReadonlyTransaction();

        try {
            t.attachNew(new IntRefTranlocal(IntRef.createUncommitted(), 0));
            fail();
        } catch (ReadonlyException ex) {
        }

        assertIsActive(t);
    }

    @Test
    public void attachAsNewOnCommittedTransactionShouldFail() {
        AlphaTransaction t = startReadonlyTransaction();
        t.commit();

        try {
            t.attachNew(new IntRefTranlocal(IntRef.createUncommitted(), 0));
            fail();
        } catch (ReadonlyException ex) {
        }

        assertIsCommitted(t);
    }

    @Test
    public void attachAsNewOnAbortedTransactionShouldFail() {
        AlphaTransaction t = startReadonlyTransaction();
        t.abort();

        try {
            t.attachNew(new IntRefTranlocal(IntRef.createUncommitted(), 0));
            fail();
        } catch (ReadonlyException ex) {
        }

        assertIsAborted(t);
    }

}
