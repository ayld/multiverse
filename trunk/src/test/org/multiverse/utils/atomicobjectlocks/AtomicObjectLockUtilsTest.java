package org.multiverse.utils.atomicobjectlocks;

import org.junit.Test;
import org.multiverse.DummyTransaction;
import static org.multiverse.utils.atomicobjectlocks.AtomicObjectLockUtils.releaseLocks;

public class AtomicObjectLockUtilsTest {

    @Test(expected = NullPointerException.class)
    public void releaseLocksWithNullTransactionFails() {
        releaseLocks(new AtomicObjectLock[]{}, null);
    }

    @Test
    public void releaseLocksWithNullLocksSucceeds() {
        releaseLocks(null, new DummyTransaction());
    }

    @Test
    public void releaseLocksWithEmptyLocksSucceeds() {
        releaseLocks(new AtomicObjectLock[]{}, new DummyTransaction());
    }

    @Test
    public void test() {
        //todo        
    }
}
