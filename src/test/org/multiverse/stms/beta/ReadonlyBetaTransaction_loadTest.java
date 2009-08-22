package org.multiverse.stms.beta;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.LoadTooOldVersionException;
import org.multiverse.utils.GlobalStmInstance;

public class ReadonlyBetaTransaction_loadTest {
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
    public void load() {
        BetaRef<Long> ref = new BetaRef<Long>(10L);

        long version = stm.getClockVersion();
        BetaTransaction t = startTransaction();
        BetaRefTranlocal tranlocal = t.load(ref);
        assertSame(ref.load(stm.getClockVersion()), tranlocal);
        assertIsActive(t);
        assertEquals(version, stm.getClockVersion());
    }

    @Test
    public void loadNullFails() {
        BetaTransaction t = startTransaction();
        try {
            t.load(null);
            fail();
        } catch (NullPointerException expected) {
        }
        assertIsActive(t);
    }

    @Test
    public void loadTranlocalWithOlderVersion() {
        BetaRef<Long> firstRef = new BetaRef<Long>(10L);
        //the secondRef is executed under its own transaction, so will increase the clock
        BetaRef<Long> secondRef = new BetaRef<Long>(10L);

        long version = stm.getClockVersion();
        BetaTransaction t = startTransaction();
        BetaRefTranlocal tranlocal = t.load(firstRef);
        assertSame(firstRef.load(stm.getClockVersion()), tranlocal);
        assertIsActive(t);
        assertEquals(version, stm.getClockVersion());
    }

    @Test
    public void loadLockedButExpectedVersion() {
        BetaRef<Long> ref = new BetaRef<Long>();
        BetaTransaction t1 = startTransaction();
        ref.acquireLockAndDetectWriteConflict(t1);

        BetaTransaction t2 = startTransaction();
        BetaRefTranlocal<Long> tranlocal = t2.load(ref);
        assertSame(ref.load(stm.getClockVersion()), tranlocal);

        assertIsActive(t2);
    }

    @Test
    public void loadLockedButOlderVersionFound() {
        //todo
    }

    @Test
    public void loadNonCommitted() {
        //todo
    }

    @Test
    public void loadTooOld() {
        BetaRef<Long> ref = new BetaRef<Long>();

        BetaTransaction t1 = startTransaction();

        BetaTransaction t2 = (BetaTransaction) stm.startUpdateTransaction();
        ref.set(t2, 20L);
        t2.commit();

        try {
            t1.load(ref);
            fail();
        } catch (LoadTooOldVersionException ex) {
        }

        assertIsActive(t1);
    }

    @Test
    public void loadAgainGivesSameTranlocal() {
        BetaRef<Long> ref = new BetaRef<Long>(10L);

        long version = stm.getClockVersion();
        BetaTransaction t = startTransaction();
        BetaRefTranlocal tranlocal1 = t.load(ref);
        BetaRefTranlocal tranlocal2 = t.load(ref);
        assertSame(tranlocal1, tranlocal2);
        assertIsActive(t);
        assertEquals(version, stm.getClockVersion());
    }

    @Test
    public void testLoadedTranlocalIsImmutable() {
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
