package org.multiverse.stms.alpha;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.assertIsCommitted;
import org.multiverse.api.GlobalStmInstance;
import org.multiverse.api.Transaction;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

/**
 * @author Peter Veentjer
 */
public class ReadonlyAlphaTransaction_commitTest {

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
    public void commitStartedTransaction() {
        long startVersion = stm.getClockVersion();
        Transaction t = startReadonlyTransaction();
        t.commit();

        assertEquals(startVersion, stm.getClockVersion());
        assertIsCommitted(t);
    }
}
