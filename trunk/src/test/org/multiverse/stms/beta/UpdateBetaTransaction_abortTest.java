package org.multiverse.stms.beta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.assertIsAborted;
import static org.multiverse.TestUtils.assertIsCommitted;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.LoadUncommittedException;

public class UpdateBetaTransaction_abortTest {

    private BetaStm stm;

    @Before
    public void setUp() {
        stm = new BetaStm();
    }

    private BetaTransaction startTransaction() {
        return (BetaTransaction) stm.startUpdateTransaction();
    }

    @Test
    public void abortDoesNotCommitInsert() {
        long version = stm.getClockVersion();

        BetaTransaction t = startTransaction();
        BetaRef<String> ref = new BetaRef<String>(t);
        t.abort();

        assertEquals(version, stm.getClockVersion());
        assertIsAborted(t);

        try {
            ref.load(stm.getClockVersion());
            fail();
        } catch (LoadUncommittedException ex) {
        }
    }

    @Test
    public void abortedDoesNotCommitUpdates() {
        long version = stm.getClockVersion();

        BetaTransaction t = startTransaction();
        BetaRef<String> ref = new BetaRef<String>(t);
        t.abort();

        assertEquals(version, stm.getClockVersion());
        assertIsAborted(t);
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
