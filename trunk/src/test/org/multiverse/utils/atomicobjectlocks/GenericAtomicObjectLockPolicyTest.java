package org.multiverse.utils.atomicobjectlocks;

import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.DummyTransaction;

public class GenericAtomicObjectLockPolicyTest {
    private GenericAtomicObjectLockPolicy policy;

    @Before
    public void setUp() {
        policy = new GenericAtomicObjectLockPolicy(10, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithTooSmallSpinAttemptsPerLock() {
        new GenericAtomicObjectLockPolicy(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithTooSmallRetryCount() {
        new GenericAtomicObjectLockPolicy(0, -1);
    }

    @Test(expected = NullPointerException.class)
    public void tryLockAll_nullTransactionFails() {
        policy.tryLockAll(new AtomicObjectLock[]{}, null);
    }

    @Test
    public void tryLockAll_nullLocksSucceeds() {
        boolean success = policy.tryLockAll(null, new DummyTransaction());
        assertTrue(success);
    }

    @Test
    public void tryLockAll_emptyLocksSucceeds() {
        boolean success = policy.tryLockAll(new AtomicObjectLock[]{}, new DummyTransaction());
        assertTrue(success);
    }

    @Test
    public void test() {
        //todo
    }
}
