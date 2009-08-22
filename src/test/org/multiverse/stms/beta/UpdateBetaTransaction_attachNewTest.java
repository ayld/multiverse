package org.multiverse.stms.beta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.utils.GlobalStmInstance;

public class UpdateBetaTransaction_attachNewTest {

    private BetaStm stm;

    @Before
    public void setUp() {
        stm = new BetaStm();
        GlobalStmInstance.set(stm);
    }

    private BetaTransaction startTransaction() {
        return (BetaTransaction) stm.startUpdateTransaction();
    }

    @Test
    public void attachNewAlreadyAttachedValueDoesntCauseProblems() {
        //todo
    }

    @Test
    public void attachNewAlreadyPrivatizedValueDoesntCauseProblems() {
        //todo
    }

    @Test
    public void attachNewFailsWithNullArgument() {
        BetaTransaction t = startTransaction();

        long version = stm.getClockVersion();
        try {
            t.attachNew(null);
            fail();
        } catch (NullPointerException ex) {
        }

        assertIsActive(t);
        assertEquals(version, stm.getClockVersion());
    }

    @Test
    public void attachNewFailsIfTransactionIsAborted() {
        BetaTransaction t = startTransaction();
        t.abort();

        long version = stm.getClockVersion();
        try {
            new BetaRef(t);
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertEquals(version, stm.getClockVersion());
        assertIsAborted(t);
    }

    @Test
    public void attachNewFailsIfTransactionIsCommitted() {
        BetaTransaction t = startTransaction();
        t.commit();

        long version = stm.getClockVersion();
        try {
            new BetaRef(t);
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertEquals(version, stm.getClockVersion());
        assertIsCommitted(t);
    }
}
