package org.codehaus.multiverse.multiversionedheap.standard;

import org.codehaus.multiverse.api.LockMode;
import org.codehaus.multiverse.api.TransactionId;
import org.codehaus.multiverse.multiversionedheap.Deflatable;
import org.codehaus.multiverse.multiversionedheap.StringDeflatable;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

public class Block_createNewForUpdatingLockTest {

    Block createLocklessBlock() {
        return new Block(new StringDeflatable(handleGenerator.incrementAndGet()).___deflate(1));
    }

    private final AtomicLong handleGenerator = new AtomicLong();

    Block createBlock(TransactionId lockOwner, LockMode lockMode) {
        Deflatable def = new StringDeflatable(handleGenerator.incrementAndGet());
        return new Block(def.___deflate(1), lockOwner, lockMode, null);
    }

    private final AtomicLong lockOwnerId = new AtomicLong();

    TransactionId createLockOwner() {
        return new TransactionId("LockOwner-" + lockOwnerId.incrementAndGet());
    }

    @Test
    public void freeLockFreedAgain() {
        Block block = createLocklessBlock();
        Block result = block.createNewForUpdatingLock(createLockOwner(), LockMode.free);
        assertSame(block, result);
    }

    @Test
    public void freeLockIsAcquired() {
        freeLock(LockMode.shared);
        freeLock(LockMode.exclusive);
    }

    void freeLock(LockMode newLockMode) {
        TransactionId lockOwner = createLockOwner();
        Block block = createLocklessBlock();
        Block newBlock = block.createNewForUpdatingLock(lockOwner, newLockMode);
        assertNotNull(newBlock);
        assertFalse(block == newBlock);
        assertEquals(lockOwner, newBlock.getLockOwner());
        assertEquals(newLockMode, newBlock.getLockMode());
    }

    //@Test
    public void holdLockGetsDifferentLockMode() {
        //holdLockGetsDifferentLockMode(LockMode.shared, LockMode.free);
        holdLockGetsDifferentLockMode(LockMode.shared, LockMode.shared);
        holdLockGetsDifferentLockMode(LockMode.shared, LockMode.exclusive);
        //holdLockGetsDifferentLockMode(LockMode.exclusive, LockMode.free);
        holdLockGetsDifferentLockMode(LockMode.exclusive, LockMode.shared);
        holdLockGetsDifferentLockMode(LockMode.exclusive, LockMode.exclusive);
    }

    void holdLockGetsDifferentLockMode(LockMode originalLockMode, LockMode newLockMode) {
        TransactionId lockOwner = createLockOwner();
        Block block = createBlock(createLockOwner(), originalLockMode);
        Block newBlock = block.createNewForUpdatingLock(lockOwner, newLockMode);

        assertNotNull(newBlock);
        assertFalse(block == newBlock);
        assertEquals(lockOwner, newBlock.getLockOwner());
        assertEquals(newLockMode, newBlock.getLockMode());
    }

    //@Test
    public void holdLockStaysAtLockMode() {
        holdLockStaysAtLockMode(LockMode.shared);
        holdLockStaysAtLockMode(LockMode.exclusive);
    }

    void holdLockStaysAtLockMode(LockMode originalLockMode) {
        TransactionId lockOwner = createLockOwner();
        Block block = createBlock(createLockOwner(), originalLockMode);
        Block newBlock = block.createNewForUpdatingLock(lockOwner, originalLockMode);

        assertSame(block, newBlock);
    }

    @Test
    public void otherTransactionAlreadyOwnsSharedLock() {
        otherTransactionAlreadyOwnsLock(LockMode.shared, LockMode.shared);
        otherTransactionAlreadyOwnsLock(LockMode.shared, LockMode.exclusive);
        otherTransactionAlreadyOwnsLock(LockMode.shared, LockMode.free);
    }

    @Test
    public void otherTransactionAlreadyOwnsExclusiveLock() {
        otherTransactionAlreadyOwnsLock(LockMode.exclusive, LockMode.shared);
        otherTransactionAlreadyOwnsLock(LockMode.exclusive, LockMode.exclusive);
        otherTransactionAlreadyOwnsLock(LockMode.exclusive, LockMode.free);
    }

    void otherTransactionAlreadyOwnsLock(LockMode lockModeOfOriginalLock, LockMode lockModeOfNewLock) {
        Block block = createBlock(createLockOwner(), lockModeOfOriginalLock);
        Block newBlock = block.createNewForUpdatingLock(createLockOwner(), lockModeOfNewLock);
        assertNull(newBlock);
    }
}
