package org.multiverse.utils.atomicobjectlocks;

import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.DummyTransaction;

public class SpinningAtomicObjectLockPolicyTest {
    private SpinningAtomicObjectLockPolicy policy;

    @Before
    public void setUp() {
        policy = new SpinningAtomicObjectLockPolicy(1);
    }

    @Test
    public void tryLocksSucceedsWithNullLocks() {
        boolean success = policy.tryLocks(null, new DummyTransaction());
        assertTrue(success);
    }

    @Test
    public void tryLocksSucceedsWithEmptyLocks() {
        boolean success = policy.tryLocks(new AtomicObjectLock[]{}, new DummyTransaction());
        assertTrue(success);
    }

    @Test(expected = NullPointerException.class)
    public void tryLocksFailsWithNullTransaction() {
        policy.tryLocks(new AtomicObjectLock[]{}, null);
    }

    @Test
    public void test() {
        //todo
    }
}
