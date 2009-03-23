package org.codehaus.multiverse.multiversionedheap.standard;

import org.codehaus.multiverse.api.LockMode;
import org.codehaus.multiverse.api.TransactionId;
import org.codehaus.multiverse.multiversionedheap.Deflated;
import org.codehaus.multiverse.multiversionedheap.DummyDeflated;
import org.codehaus.multiverse.utils.latches.Latch;
import org.codehaus.multiverse.utils.latches.StandardLatch;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Stack;

public class Block_createNewForUpdateTest {

    void assertNoListeners(Block block) {
        assertNull(block.getListenerRoot());
    }

    void assertLockIsFree(Block block) {
        assertNull(block.getLockOwner());
        assertEquals(LockMode.free, block.getLockMode());
    }

    void assertHasDeflated(Block block, Deflated expected) {
        assertSame(expected, block.getDeflated());
    }

    @Test
    public void updateWithoutInterferingTransaction() {
        long handle = 10;
        long firstVersion = 5;
        long newVersion = 5 + 1;

        Block originalBlock = new Block(new DummyDeflated(handle, firstVersion, "foo"));
        DummyDeflated newDeflated = new DummyDeflated(handle, newVersion, "bar");
        Block newBlock = originalBlock.createNewForUpdate(newDeflated, firstVersion, new Stack());

        assertNotNull(newBlock);
        assertFalse(newBlock == originalBlock);
        assertHasDeflated(newBlock, newDeflated);
        assertNoListeners(newBlock);
        assertLockIsFree(newBlock);
    }

    @Test
    public void writeConflictIsDetected() {
        long handle = 10;
        long version = 5;

        Block originalBlock = new Block(new DummyDeflated(handle, version, "foo"));
        DummyDeflated newDeflated = new DummyDeflated(handle, version, "bar");
        Block newBlock = originalBlock.createNewForUpdate(newDeflated, version - 1, new Stack());
        assertNull(newBlock);
    }

    @Test
    public void listenersAreRemoved() {
        long handle = 10;
        long version = 5;

        //create a block with some listeners
        Latch listener1 = new StandardLatch();
        Latch listener2 = new StandardLatch();
        Block originalBlock = new Block(new DummyDeflated(handle, version, "foo"));
        originalBlock = originalBlock.createNewForAddingListener(listener1);
        originalBlock = originalBlock.createNewForAddingListener(listener2);

        //do the update
        DummyDeflated newDeflated = new DummyDeflated(handle, version + 1, "bar");
        Block newBlock = originalBlock.createNewForUpdate(newDeflated, version, new Stack());

        assertNotNull(newBlock);
        //the listeners should be removed but the latches should still be closed
        //opening of the latches should only happen when the snapshot is activated, not before
        assertHasDeflated(newBlock, newDeflated);
        assertLockIsFree(newBlock);
        assertNoListeners(newBlock);
        assertFalse(listener1.isOpen());
        assertFalse(listener2.isOpen());
    }

    @Test
    public void lockIsReleased() {
        long handle = 10;
        long version = 5;

        //create a block with a lock.
        TransactionId lockOwner = new TransactionId("foobar");
        LockMode lockMode = LockMode.exclusive;
        Block originalBlock = new Block(new DummyDeflated(handle, version, "foo"));
        originalBlock = originalBlock.createNewForUpdatingLock(lockOwner, lockMode);

        //do the update
        DummyDeflated newDeflated = new DummyDeflated(handle, version + 1, "bar");
        Block newBlock = originalBlock.createNewForUpdate(newDeflated, version, new Stack());

        //check the locks are not there anymore.
        assertNotNull(newBlock);
        assertLockIsFree(newBlock);
        assertHasDeflated(newBlock, newDeflated);
        assertNoListeners(newBlock);
    }
}
