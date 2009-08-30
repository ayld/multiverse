package org.multiverse.stms.beta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.assertIsAborted;
import static org.multiverse.TestUtils.assertIsCommitted;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.utils.GlobalStmInstance;

public class ReadonlyBetaTransaction_commitTest {

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
    public void commitUnusedTransaction() {
        BetaTransaction t = startTransaction();
        long version = stm.getClockVersion();

        t.commit();

        assertEquals(version, stm.getClockVersion());
        assertIsCommitted(t);
    }

    @Test
    public void commitOnAbortedTransactionFails() {
        BetaTransaction t = startTransaction();
        t.abort();

        long version = stm.getClockVersion();
        try {
            t.commit();
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertEquals(version, stm.getClockVersion());
        assertIsAborted(t);
    }

    @Test
    public void commitOnCommittedTransactionIsIgnored() {
        BetaTransaction t = startTransaction();
        t.commit();

        long version = stm.getClockVersion();
        t.commit();

        assertEquals(version, stm.getClockVersion());
        assertIsCommitted(t);
    }
}
