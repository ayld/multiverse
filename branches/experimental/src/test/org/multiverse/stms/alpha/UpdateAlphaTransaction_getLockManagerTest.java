package org.multiverse.stms.alpha;

import org.junit.After;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.locks.LockManager;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

/**
 * @author Peter Veentjer
 */
public class UpdateAlphaTransaction_getLockManagerTest {

    private Stm stm;

    @Before
    public void setUp() {
        setThreadLocalTransaction(null);
        stm = GlobalStmInstance.get();
    }

    @After
    public void tearDown() {
        setThreadLocalTransaction(null);
    }

    public Transaction startUpdateTransaction() {
        Transaction t = stm.startUpdateTransaction(null);
        setThreadLocalTransaction(t);
        return t;
    }

    @Test
    public void getLockManager() {
        Transaction t = startUpdateTransaction();
        LockManager m = t.getLockManager();
        assertSame(t, m);
    }

    @Test
    public void getLockManagerFailsIfTransactionAlreadyAborted() {
        Transaction t = startUpdateTransaction();
        t.abort();

        try {
            t.getLockManager();
            fail();
        } catch (DeadTransactionException ex) {

        }
    }

    @Test
    public void getLockManagerFailsIfTransactionAlreadyCommitted() {
        Transaction t = startUpdateTransaction();
        t.commit();

        try {
            t.getLockManager();
            fail();
        } catch (DeadTransactionException ex) {
        }
    }
}
