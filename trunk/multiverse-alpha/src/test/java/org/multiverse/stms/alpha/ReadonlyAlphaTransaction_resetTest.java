package org.multiverse.stms.alpha;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.assertIsActive;
import static org.multiverse.api.GlobalStmInstance.setGlobalStmInstance;
import org.multiverse.stms.alpha.manualinstrumentation.IntRef;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

/**
 * @author Peter Veentjer
 */
public class ReadonlyAlphaTransaction_resetTest {

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

    public AlphaTransaction startReadonlyTransaction() {
        AlphaTransaction t = stm.startReadOnlyTransaction(null);
        setThreadLocalTransaction(t);
        return t;
    }

    @Test
    public void resetOnAbortedTransaction() {
        IntRef value = new IntRef(10);

        AlphaTransaction t = startReadonlyTransaction();
        t.abort();

        value.inc();

        long version = stm.getClockVersion();
        t.reset();
        assertEquals(version, stm.getClockVersion());
        assertIsActive(t);
    }

    @Test
    public void resetOnCommittedTransaction() {
        IntRef value = new IntRef(10);

        AlphaTransaction t = startReadonlyTransaction();
        t.commit();

        value.inc();

        long version = stm.getClockVersion();
        t.reset();
        assertEquals(version, stm.getClockVersion());
        assertIsActive(t);
    }
}
