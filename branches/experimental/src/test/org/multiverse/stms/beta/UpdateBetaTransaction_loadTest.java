package org.multiverse.stms.beta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.assertIsAborted;
import static org.multiverse.TestUtils.assertIsCommitted;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.utils.GlobalStmInstance;

public class UpdateBetaTransaction_loadTest {

    private BetaStm stm;

    @Before
    public void setUp() {
        stm = new BetaStm();
        GlobalStmInstance.set(stm);
    }

    private BetaTransaction startTransaction() {
        return (BetaTransaction) stm.startUpdateTransaction(null);
    }

    @Test
    public void load() {
        //todo
    }

    @Test
    public void loadTooOldVersion() {
        //todo
    }

    @Test
    public void loadAttached() {
        //todo
    }

    @Test
    public void loadPrivatized() {
        //todo
    }

    @Test
    public void loadUncommitted() {
        //todo
    }

    @Test
    public void loadLocked() {
        //todo
    }

    @Test
    public void loadOnAbortedTransactionFails() {
        BetaRef ref = new BetaRef();

        BetaTransaction t = startTransaction();
        t.abort();

        long version = stm.getClockVersion();
        try {
            t.load(ref);
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertEquals(version, stm.getClockVersion());
        assertIsAborted(t);
    }

    @Test
    public void loadOnCommittedTransactionFails() {
        BetaRef ref = new BetaRef();

        BetaTransaction t = startTransaction();
        t.commit();

        long version = stm.getClockVersion();
        try {
            t.load(ref);
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertEquals(version, stm.getClockVersion());
        assertIsCommitted(t);
    }
}
