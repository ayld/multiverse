package org.multiverse.utils.commitlock;

import org.junit.Test;
import org.multiverse.DummyTransaction;
import static org.multiverse.utils.commitlock.CommitLockUtils.releaseLocks;

public class CommitLockUtilsTest {

    @Test(expected = NullPointerException.class)
    public void releaseLocksWithNullTransactionFails() {
        releaseLocks(new CommitLock[]{}, null);
    }

    @Test
    public void releaseLocksWithNullLocksSucceeds() {
        releaseLocks(null, new DummyTransaction());
    }

    @Test
    public void releaseLocksWithEmptyLocksSucceeds() {
        releaseLocks(new CommitLock[]{}, new DummyTransaction());
    }

    @Test
    public void test() {
        //todo        
    }
}
