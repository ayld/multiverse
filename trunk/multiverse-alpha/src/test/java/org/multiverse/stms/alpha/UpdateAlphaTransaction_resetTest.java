package org.multiverse.stms.alpha;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.assertIsActive;
import static org.multiverse.api.GlobalStmInstance.setGlobalStmInstance;
import org.multiverse.api.Transaction;
import org.multiverse.stms.alpha.manualinstrumentation.IntRef;

/**
 * @author Peter Veentjer
 */
public class UpdateAlphaTransaction_resetTest {
    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        setGlobalStmInstance(stm);
    }

    @Test
    public void resetOnAbortedTransaction() {
        Transaction t = stm.startUpdateTransaction(null);
        t.abort();

        //commit some dummy change
        new IntRef(20);

        t.restart();
        assertIsActive(t);
        assertEquals(stm.getClockVersion(), t.getReadVersion());
    }

    @Test
    public void resetOnCommittedTransaction() {
        Transaction t = stm.startUpdateTransaction(null);
        t.commit();

        //commit some dummy change
        new IntRef(20);

        t.restart();
        assertIsActive(t);
        assertEquals(stm.getClockVersion(), t.getReadVersion());
    }
}
