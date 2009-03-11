package org.codehaus.multiverse.multiversionedheap.standard;

import org.junit.Before;
import org.junit.Test;

public class DefaultMultiversionedHeap_LockNoWaitTest {
    private DefaultMultiversionedHeap heap;

    @Before
    public void setUp() {
        heap = new DefaultMultiversionedHeap();
    }

    @Test
    public void testNonExistingHandle() {
    }

    @Test
    public void testNulHandle() {
        //    heap.lockNoWait(0);
    }

    @Test
    public void testLockIsFree_acquireSharedLock() {
    }

    @Test
    public void testLockIsFree_acquireExclusiveLock() {
    }

    @Test
    public void testUpgradeFromSharedToExclusiveLockMode() {
    }

    @Test
    public void testUpgradeFromExclusiveToSharedLockIsIgnored() {
    }

    @Test
    public void testMultipleLocksAcquired() {
    }

    @Test
    public void testAlreadyLockedByCurrentTransaction() {
    }

    @Test
    public void testAlreadyLockedByDifferentTransaction() {
    }

    public void testLocksAreReleasedAfterTransactionCommits() {

    }

    public void testAbortedTransaction() {

    }
}
