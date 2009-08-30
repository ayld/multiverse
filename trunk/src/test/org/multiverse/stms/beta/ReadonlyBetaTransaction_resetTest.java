package org.multiverse.stms.beta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.assertIsActive;
import org.multiverse.api.exceptions.ResetFailureException;
import org.multiverse.utils.GlobalStmInstance;

public class ReadonlyBetaTransaction_resetTest {
    private BetaStm stm;

    @Before
    public void setUp() {
        stm = new BetaStm();
        GlobalStmInstance.set(stm);
    }

    private BetaTransaction startTransaction() {
        return (BetaTransaction) stm.startReadOnlyTransaction(null);
    }

    @Test
    public void resetFailsOnActiveTransaction() {
        BetaTransaction t = startTransaction();

        try {
            t.reset();
            fail();
        } catch (ResetFailureException ex) {
        }

        assertIsActive(t);
    }

    @Test
    public void abortedTransaction() {
        BetaTransaction t = startTransaction();
        t.abort();

        t.reset();
        assertIsActive(t);
        assertEquals(stm.getClockVersion(), t.getReadVersion());
    }

    @Test
    public void committedTransaction() {
        BetaTransaction t = startTransaction();
        t.commit();

        t.reset();
        assertIsActive(t);
        assertEquals(stm.getClockVersion(), t.getReadVersion());
    }
}
