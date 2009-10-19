package org.multiverse.stms.alpha;

import org.junit.After;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.GlobalStmInstance;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.ReadonlyException;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

/**
 * @author Peter Veentjer
 */
public class ReadonlyAlphaTransaction_abortAndWaitForRetryTest {

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
    public void abortAndRetryFailsIfTransactionIsStarted() {
        Transaction t = startReadonlyTransaction();

        try {
            t.abortAndWaitForRetry();
            fail();
        } catch (ReadonlyException ex) {
        }

        assertIsActive(t);
    }

    @Test
    public void abortAndRetryFailsIfTransactionIsCommitted() {
        Transaction t = startReadonlyTransaction();
        t.commit();

        try {
            t.abortAndWaitForRetry();
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertIsCommitted(t);
    }

    @Test
    public void abortAndRetryFailsIfTransactionIsAborted() {
        Transaction t = startReadonlyTransaction();
        t.abort();

        try {
            t.abortAndWaitForRetry();
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertIsAborted(t);
    }
}
