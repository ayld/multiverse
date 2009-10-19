package org.multiverse.stms.alpha;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.assertIsActive;
import org.multiverse.api.GlobalStmInstance;
import org.multiverse.api.Transaction;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

public class ReadonlyAlphaTransaction_miscTest {
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

    public AlphaTransaction startUpdateTransaction() {
        AlphaTransaction t = stm.startUpdateTransaction(null);
        setThreadLocalTransaction(t);
        return t;
    }

    @Test
    public void start() {
        long version = stm.getClockVersion();
        Transaction t = stm.startReadOnlyTransaction(null);
        assertIsActive(t);
        assertEquals(version, stm.getClockVersion());
    }
}
