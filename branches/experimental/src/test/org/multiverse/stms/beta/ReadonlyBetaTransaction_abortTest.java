package org.multiverse.stms.beta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.assertIsAborted;
import static org.multiverse.TestUtils.assertIsCommitted;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.utils.GlobalStmInstance;

public class ReadonlyBetaTransaction_abortTest {

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
    public void abortUnusedActivateTransaction() {
        BetaTransaction t = startTransaction();
        long version = stm.getClockVersion();

        t.abort();

        assertIsAborted(t);
        assertEquals(version, stm.getClockVersion());
    }

    @Test
    public void abortOnCommittedTransactionFails() {
        BetaTransaction t = startTransaction();
        t.commit();

        long version = stm.getClockVersion();
        try {
            t.abort();
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertEquals(version, stm.getClockVersion());
        assertIsCommitted(t);
    }

    @Test
    public void abortOnAbortedTransactionIsIgnored() {
        BetaTransaction t = startTransaction();
        t.abort();

        long version = stm.getClockVersion();
        t.abort();

        assertEquals(version, stm.getClockVersion());
        assertIsAborted(t);
    }

}
