package org.multiverse.stms.alpha;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.assertIsActive;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.ResetFailureException;
import org.multiverse.stms.alpha.manualinstrumentation.IntRef;
import org.multiverse.utils.GlobalStmInstance;

/**
 * @author Peter Veentjer
 */
public class UpdateTransaction_resetTest {
    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
    }

    @Test
    public void resetOnActiveTransactionFails() {
        Transaction t = stm.startUpdateTransaction();
        try {
            t.reset();
            fail();
        } catch (ResetFailureException ex) {
        }

        assertIsActive(t);
    }

    @Test
    public void resetOnAbortedTransaction() {
        Transaction t = stm.startUpdateTransaction();
        t.abort();

        //commit some dummy change
        new IntRef(20);

        t.reset();
        assertIsActive(t);
        assertEquals(stm.getClockVersion(), t.getReadVersion());
    }

    @Test
    public void resetOnCommittedTransaction() {
        Transaction t = stm.startUpdateTransaction();
        t.commit();

        //commit some dummy change
        new IntRef(20);

        t.reset();
        assertIsActive(t);
        assertEquals(stm.getClockVersion(), t.getReadVersion());
    }
}
