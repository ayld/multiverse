package org.codehaus.multiverse.multiversionedheap.standard;

import org.codehaus.multiverse.api.LockMode;
import org.codehaus.multiverse.api.TransactionId;
import org.codehaus.multiverse.api.exceptions.NoSuchObjectException;
import org.codehaus.multiverse.multiversionedheap.Deflatable;
import org.codehaus.multiverse.multiversionedheap.HeapSnapshot;
import org.codehaus.multiverse.multiversionedheap.MultiversionedHeap.LockNoWaitResult;
import org.codehaus.multiverse.multiversionedstm.examples.IntegerValue;
import org.codehaus.multiverse.utils.Pair;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class DefaultMultiversionedHeap_LockNoWaitTest {
    private DefaultMultiversionedHeap heap;

    @Before
    public void setUp() {
        heap = new DefaultMultiversionedHeap();
    }

    private void assertActiveSnapshot(HeapSnapshot expected) {
        assertSame(expected, heap.getActiveSnapshot());
    }

    private void assertLockInfo(HeapSnapshot snapshot, TransactionId owner, LockMode lockMode, long handle) {
        Pair<TransactionId, LockMode> lockInfo = snapshot.readLockInfo(handle);
        assertEquals(lockInfo.getA(), owner);
        assertEquals(lockInfo.getB(), lockMode);
    }

    private void commit(Deflatable deflatable) {
        heap.commit(heap.getActiveSnapshot(), deflatable);
    }

    @Test
    public void testNonExistingHandle() {
        HeapSnapshot snapshot = heap.getActiveSnapshot();
        try {
            heap.lockNoWait(new TransactionId("1"), LockMode.free, 10);
            fail();
        } catch (NoSuchObjectException ex) {
            //todo: the correct exception needs to be thrown.
        }

        assertActiveSnapshot(snapshot);
    }

    @Test
    public void testNulHandle() {
        //    heap.lockNoWaitOrFail(0);
    }

    @Test
    public void testLockIsFree_acquireSharedLock() {
        testLockIsFree_acquireLock(LockMode.shared);
    }

    @Test
    public void testLockIsFree_acquireExclusiveLock() {
        testLockIsFree_acquireLock(LockMode.exclusive);
    }

    public void testLockIsFree_acquireLock(LockMode lockMode) {
        IntegerValue value = new IntegerValue();
        commit(value);

        TransactionId owner = new TransactionId("10");

        LockNoWaitResult result = heap.lockNoWait(owner, lockMode, value.___getHandle());

        assertActiveSnapshot(result.getResultSnapshot());

        assertLockInfo(heap.getActiveSnapshot(), owner, lockMode, value.___getHandle());
    }

    @Test
    public void testLockIsFree_acquireNoLockIsIgnored() {
        IntegerValue value = new IntegerValue();
        commit(value);

        TransactionId owner = new TransactionId("10");

        LockNoWaitResult result = heap.lockNoWait(owner, LockMode.free, value.___getHandle());

        //assertActiveSnapshot(result.getResultSnapshot());

        //assertLockInfo(heap.getActiveSnapshot(), null, LockMode.free, value.___getHandle());
    }

    @Test
    public void testLock_noChange() {
        //todo
    }

    @Test
    public void testUpgradeFromExclusiveToExclusiveIsIgnored() {
        //todo
    }

    @Test
    public void testUpgradeFromSharedToExclusiveLockMode() {
        IntegerValue value = new IntegerValue();
        commit(value);

        TransactionId owner = new TransactionId("10");

        heap.lockNoWait(owner, LockMode.shared, value.___getHandle());
        LockNoWaitResult result = heap.lockNoWait(owner, LockMode.exclusive, value.___getHandle());

        assertTrue(result.isSuccess());
        assertActiveSnapshot(result.getResultSnapshot());

        assertLockInfo(heap.getActiveSnapshot(), owner, LockMode.exclusive, value.___getHandle());
    }

    @Test
    public void testUpgradeFromExclusiveToSharedLockIsIgnored() {
        //todo
    }

    @Test
    public void testMultipleLocksAcquired() {
        //todo
    }

    @Test
    public void testAlreadyLockedByCurrentTransaction() {
        //todo
    }

    @Test
    public void testAlreadyLockedByDifferentTransaction() {
        IntegerValue value = new IntegerValue();
        commit(value);

        TransactionId owner1 = new TransactionId("Transaction1");
        TransactionId owner2 = new TransactionId("Transaction2");

        LockNoWaitResult result1 = heap.lockNoWait(owner1, LockMode.shared, value.___getHandle());
        LockNoWaitResult result2 = heap.lockNoWait(owner2, LockMode.shared, value.___getHandle());

        assertActiveSnapshot(result1.getResultSnapshot());

        assertFalse(result2.isSuccess());
        assertLockInfo(heap.getActiveSnapshot(), owner1, LockMode.shared, value.___getHandle());
    }

    public void testLocksAreReleasedAfterTransactionCommits() {
        //todo
    }
}
