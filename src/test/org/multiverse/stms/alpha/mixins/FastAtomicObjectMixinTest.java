package org.multiverse.stms.alpha.mixins;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.DummyTransaction;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.LoadLockedException;
import org.multiverse.api.exceptions.LoadTooOldVersionException;
import org.multiverse.stms.alpha.*;
import org.multiverse.stms.alpha.manualinstrumentation.IntRef;
import org.multiverse.utils.GlobalStmInstance;

public class FastAtomicObjectMixinTest {
    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
    }

    // ================ storeAndReleaseLock ========================

    @Test
    public void storeAndReleaseLock() {

    }

    // ================= validate ==================================

    @Test
    public void validate() {
        //todo
    }

    // ================ load ==========================

    @Test
    public void loadEqualVersion() {
        DummyFastAtomicObjectMixin atomicObject = new DummyFastAtomicObjectMixin();

        DummyTranlocal tranlocal = new DummyTranlocal();
        //tranlocal.atomicObject = atomicObject;
        long writeVersion = 10;
        atomicObject.storeAndReleaseLock(tranlocal, writeVersion);

        Tranlocal result = atomicObject.load(writeVersion);
        assertSame(tranlocal, result);
    }

    @Test
    public void loadWithNewVersion() {
        DummyFastAtomicObjectMixin object = new DummyFastAtomicObjectMixin();

        DummyTranlocal tranlocal = new DummyTranlocal();
        long writeVersion = 10;
        object.storeAndReleaseLock(tranlocal, writeVersion);

        Tranlocal result = object.load(writeVersion + 1);
        assertSame(tranlocal, result);
    }

    @Test
    public void loadUncommittedData() {
        DummyFastAtomicObjectMixin object = new DummyFastAtomicObjectMixin();

        Tranlocal result = object.load(1);
        assertNull(result);
    }

    @Test
    public void loadTooNewVersion() {
        IntRef intValue = new IntRef(0);

        long version = stm.getClockVersion();
        intValue.inc();

        try {
            intValue.load(version);
            fail();
        } catch (LoadTooOldVersionException ex) {
        }
    }

    @Test
    public void loadWhileLockedSucceedsIfCommittedVersionIsEqualToReadVersion() {
        IntRef intValue = new IntRef(0);

        long readVersion = stm.getClockVersion();

        Transaction owner = new DummyTransaction();
        Tranlocal expected = intValue.load(readVersion);

        intValue.tryLock(owner);

        Tranlocal found = intValue.load(readVersion);
        assertSame(expected, found);
    }

    @Test
    public void loadWhileLockedFailsIfTheCommittedVersionIsOlderThanReadVersion() {
        IntRef intValue = new IntRef(0);

        Transaction owner = new DummyTransaction();
        intValue.tryLock(owner);

        long readVersion = stm.getClockVersion() + 1;

        try {
            intValue.load(readVersion);
            fail();
        } catch (LoadLockedException ex) {
        }
    }

    @Test
    public void loadWhileLockedFailsIfTheCommittedVersionIsNewerThanReadVersion() {
        IntRef intValue = new IntRef(0);
        long readVersion = stm.getClockVersion();
        intValue.inc();

        Transaction owner = new DummyTransaction();
        intValue.tryLock(owner);

        try {
            intValue.load(readVersion);
            fail();
        } catch (LoadTooOldVersionException ex) {
        }
    }


    // ================ acquireLock ==========================

    @Test
    public void acquireFreeLock() {
        Transaction lockOwner = new DummyTransaction();
        DummyFastAtomicObjectMixin object = new DummyFastAtomicObjectMixin();

        boolean result = object.tryLock(lockOwner);
        assertTrue(result);
        assertSame(lockOwner, object.getLockOwner());
    }

    @Test
    public void acquireAlreadyOwnedLock() {
        //todo
    }

    @Test
    public void acquireLockedOwnedByOther() {
        Transaction oldOwner = new DummyTransaction();
        DummyFastAtomicObjectMixin object = new DummyFastAtomicObjectMixin();

        object.tryLock(oldOwner);

        Transaction newOwner = new DummyTransaction();
        boolean result = object.tryLock(newOwner);
        assertFalse(result);
        assertSame(oldOwner, object.getLockOwner());
    }

    // ======================= release lock ====================

    @Test
    public void releaseOwnedLock() {
        Transaction owner = new DummyTransaction();
        DummyFastAtomicObjectMixin object = new DummyFastAtomicObjectMixin();
        object.tryLock(owner);

        object.releaseLock(owner);
        assertNull(object.getLockOwner());
    }

    @Test
    public void releaseFreeLock() {
        Transaction owner = new DummyTransaction();
        DummyFastAtomicObjectMixin object = new DummyFastAtomicObjectMixin();

        object.releaseLock(owner);
        assertNull(object.getLockOwner());
    }

    @Test
    public void releaseNotOwnedLock() {
        Transaction otherOwner = new DummyTransaction();
        Transaction thisOwner = new DummyTransaction();
        DummyFastAtomicObjectMixin object = new DummyFastAtomicObjectMixin();
        object.tryLock(otherOwner);

        object.releaseLock(thisOwner);
        assertSame(otherOwner, object.getLockOwner());
    }

    // ==========================================

    static class DummyFastAtomicObjectMixin extends FastAtomicObjectMixin {

        @Override
        public Tranlocal privatize(long readVersion) {
            throw new RuntimeException();
        }
    }

    static class DummyTranlocal extends Tranlocal {

        @Override
        public void prepareForCommit(long writeVersion) {
            throw new RuntimeException();
        }

        @Override
        public TranlocalSnapshot takeSnapshot() {
            throw new RuntimeException();
        }

        @Override
        public DirtinessStatus getDirtinessStatus() {
            throw new RuntimeException();
        }

        @Override
        public AlphaAtomicObject getAtomicObject() {
            throw new RuntimeException();
        }
    }
}
