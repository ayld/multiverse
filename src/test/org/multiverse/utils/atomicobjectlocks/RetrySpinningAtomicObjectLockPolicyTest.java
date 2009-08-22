package org.multiverse.utils.atomicobjectlocks;

import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.DummyTransaction;

public class RetrySpinningAtomicObjectLockPolicyTest {
    private RetrySpinningAtomicObjectLockPolicy policy;

    @Before
    public void setUp() {
        policy = new RetrySpinningAtomicObjectLockPolicy(10, 10);
    }

    @Test(expected = NullPointerException.class)
    public void tryLocksNullTransactionFails() {
        policy.tryLocks(new AtomicObjectLock[]{}, null);
    }

    @Test
    public void tryLocksNullLocksSucceeds() {
        boolean success = policy.tryLocks(null, new DummyTransaction());
        assertTrue(success);
    }

    @Test
    public void tryLocksEmptyLocksSucceeds() {
        boolean success = policy.tryLocks(new AtomicObjectLock[]{}, new DummyTransaction());
        assertTrue(success);
    }

    @Test
    public void test() {
        //todo
    }
}
