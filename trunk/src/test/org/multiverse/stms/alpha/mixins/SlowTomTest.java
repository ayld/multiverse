package org.multiverse.stms.alpha.mixins;

import org.junit.Before;
import org.multiverse.api.Stm;
import org.multiverse.utils.GlobalStmInstance;

/**
 * @author Peter Veentjer
 */
public class SlowTomTest {
    private Stm stm;

    @Before
    public void setUp() {
        stm = GlobalStmInstance.get();
    }

    // ============= tryAcquireWriteLockAndDetectWriteConflict ==================================

    /*
    @Test
    public void acquireLock_firstTime() {
        IntValue intValue = IntValue.createUninitialized();

        Transaction owner = new DummyTransaction();
        boolean result = intValue.acquireLock(owner);
        assertTrue(result);
        assertSame(owner, intValue.getWriteLockOwner());
    }

    @Test
    public void tryAcquireWriteLockAndDetectWritConflict_lockIsFree() {
        IntValue intValue = new IntValue(1);

        Transaction owner = new DummyTransaction();
        boolean result = intValue.acquireLock(owner, stm.getClockVersion());
        assertTrue(result);
        assertSame(owner, intValue.getWriteLockOwner());
    }

    @Test
    public void tryAcquireWriteLockAndDetectWriteConflict_lockIsNotFree() {
        IntValue intValue = new IntValue(1);

        Transaction owner1 = new DummyTransaction();
        intValue.acquireLock(owner1, stm.getClockVersion());

        Transaction owner2 = new DummyTransaction();
        boolean result = intValue.acquireLock(owner1, stm.getClockVersion());
        assertFalse(result);
        assertSame(owner1, intValue.getWriteLockOwner());
    }

    @Test
    public void tryAcquireWriteLockAndDetectWriteConflicts_optimisticLockingFailure() {
        IntValue intValue = new IntValue(1);

        Transaction owner1 = new DummyTransaction();
        try {
            intValue.acquireLock(owner1, stm.getClockVersion() - 1);
            fail();
        } catch (WriteConflictError e) {
        }

        assertNull(intValue.getWriteLockOwner());
    }

    @Test
    public void tryAcquireWriteLockAndDetectWriteConflicts_lockIsNotHoldAndOptimisticLockingFailure() {
        IntValue intValue = new IntValue(1);

        Transaction owner1 = new DummyTransaction();
        intValue.acquireLock(owner1, stm.getClockVersion());

        Transaction owner2 = new DummyTransaction();
        boolean result = intValue.acquireLock(owner2, stm.getClockVersion() - 1);
        assertFalse(result);
        assertSame(owner1, intValue.getWriteLockOwner());
    }

    // ====================== releaseWriteLock ===========================

    @Test
    public void releaseWriteLockThatIsNotLocked() {
        IntValue intValue = new IntValue(1);

        Transaction owner = new DummyTransaction();
        intValue.releaseLock(owner);
        assertNull(intValue.getWriteLockOwner());
    }

    @Test
    public void releaseWriteLockIsOwnedByReleasingTransaction() {
        IntValue intValue = new IntValue(1);

        Transaction owner = new DummyTransaction();
        intValue.acquireLock(owner, stm.getClockVersion());

        intValue.releaseLock(owner);
        assertNull(intValue.getWriteLockOwner());
    }

    @Test
    public void releaseWriteLockIfLockNotOwned() {
        IntValue intValue = new IntValue(1);

        Transaction owner = new DummyTransaction();
        intValue.acquireLock(owner, stm.getClockVersion());

        Transaction badOwner = new DummyTransaction();
        intValue.releaseLock(badOwner);
        assertSame(owner, intValue.getWriteLockOwner());
    } */
}