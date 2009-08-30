package org.multiverse.utils.commitlock;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.DummyTransaction;

public class GenericCommitLockPolicyTest {
    private GenericCommitLockPolicy policy;

    @Before
    public void setUp() {
        policy = new GenericCommitLockPolicy(10, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithTooSmallSpinAttemptsPerLock() {
        new GenericCommitLockPolicy(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithTooSmallRetryCount() {
        new GenericCommitLockPolicy(0, -1);
    }

    @Test(expected = NullPointerException.class)
    public void tryLockAll_nullTransactionFails() {
        policy.tryLockAllAndDetectConflicts(new CommitLock[]{}, null);
    }

    @Test
    public void tryLockAll_nullLocksSucceeds() {
        CommitLockResult result = policy.tryLockAllAndDetectConflicts(null, new DummyTransaction());
        assertEquals(CommitLockResult.success, result);
    }

    @Test
    public void tryLockAll_emptyLocksSucceeds() {
        CommitLockResult result = policy.tryLockAllAndDetectConflicts(new CommitLock[]{}, new DummyTransaction());
        assertEquals(CommitLockResult.success, result);
    }

    @Test
    public void test() {
        //todo
    }
}
