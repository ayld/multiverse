package org.multiverse.stms.alpha;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.assertIsAborted;
import org.multiverse.api.GlobalStmInstance;
import org.multiverse.api.Transaction;
import org.multiverse.stms.alpha.manualinstrumentation.IntRef;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

/**
 * @author Peter Veentjer
 */
public class UpdateAlphaTransaction_abortTest {
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
        AlphaTransaction t = stm.startUpdateTransaction(null);
        setThreadLocalTransaction(t);
        return t;
    }

    @Test
    public void changesOnReadPrivatizedObjectsAreNotCommitted() {
        IntRef value = new IntRef(10);

        long version = stm.getClockVersion();
        Transaction t2 = startUpdateTransaction();
        value.inc();
        t2.abort();

        assertEquals(version, stm.getClockVersion());
        assertIsAborted(t2);

        startUpdateTransaction();
        int r = value.get();
        assertEquals(10, r);
    }

    @Test
    public void changesOnAttachedAsNewObjectsAreNotCommitted() {
        long startVersion = stm.getClockVersion();

        Transaction t = startUpdateTransaction();
        IntRef intValue = new IntRef(10);
        t.abort();

        assertIsAborted(t);
        assertEquals(startVersion, stm.getClockVersion());

        AlphaTranlocal result = intValue.load(stm.getClockVersion());
        assertNull(result);
    }

    @Test
    public void abortComplexScenario() {
        IntRef value1 = new IntRef(1);
        IntRef value2 = new IntRef(1);
        IntRef value3 = new IntRef(1);

        long startVersion = stm.getClockVersion();

        Transaction t = startUpdateTransaction();
        value1.inc();
        value2.inc();
        value3.inc();
        t.abort();
        setThreadLocalTransaction(null);

        assertIsAborted(t);
        assertEquals(startVersion, stm.getClockVersion());
        assertEquals(1, value1.get());
        assertEquals(1, value2.get());
        assertEquals(1, value3.get());
    }

    @Test
    public void unusedStartedTransaction() {
        Transaction t = startUpdateTransaction();

        long version = stm.getClockVersion();
        t.abort();
        assertEquals(version, stm.getClockVersion());
        assertIsAborted(t);
    }

 
}
