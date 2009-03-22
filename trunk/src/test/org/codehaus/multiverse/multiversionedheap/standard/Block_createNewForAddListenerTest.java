package org.codehaus.multiverse.multiversionedheap.standard;

import org.codehaus.multiverse.api.LockMode;
import org.codehaus.multiverse.api.TransactionId;
import org.codehaus.multiverse.multiversionedheap.DummyDeflated;
import org.codehaus.multiverse.util.latches.Latch;
import org.codehaus.multiverse.util.latches.StandardLatch;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Block_createNewForAddListenerTest {

    private void assertIsClosed(Latch latch) {
        assertFalse(latch.isOpen());
    }

    private void assertHasListeners(Block block, Latch... listeners) {
        List<Latch> found = getListeners(block);
        assertEquals(Arrays.asList(listeners), found);
    }

    private List<Latch> getListeners(Block block) {
        List<Latch> result = new LinkedList<Latch>();
        ListenerNode node = block.getListenerRoot();
        while (node != null) {
            result.add(node.getListener());
            node = node.getPrevious();
        }
        return result;
    }

    @Test
    public void firstListener() {
        Block originalBlock = new Block(new DummyDeflated(1, 1, "foo"));

        Latch listener = new StandardLatch();
        Block newBlock = originalBlock.createNewForAddingListener(listener);

        assertSame(originalBlock.getDeflated(), newBlock.getDeflated());
        assertHasListeners(newBlock, listener);
        assertIsClosed(listener);
    }

    @Test
    public void notFirstListener() {
        Block originalBlock = new Block(new DummyDeflated(1, 1, "foo"));

        Latch listener1 = new StandardLatch();
        Latch listener2 = new StandardLatch();
        originalBlock = originalBlock.createNewForAddingListener(listener1);
        Block newBlock = originalBlock.createNewForAddingListener(listener2);

        assertSame(originalBlock.getDeflated(), newBlock.getDeflated());
        assertHasListeners(newBlock, listener2, listener1);
        assertIsClosed(listener1);
        assertIsClosed(listener2);
    }

    public void testLockInformationRemains() {
        TransactionId lockOwner = new TransactionId("t1");
        LockMode lockMode = LockMode.exclusive;

        Block originalBlock = new Block(new DummyDeflated(1, 1, "foo"));
        originalBlock.createNewForUpdatingLock(lockOwner, lockMode);

        Latch listener = new StandardLatch();
        Block newBlock = originalBlock.createNewForAddingListener(listener);

        assertSame(originalBlock.getDeflated(), newBlock.getDeflated());
        assertSame(lockOwner, newBlock.getLockOwner());
        assertSame(lockMode, newBlock.getLockMode());
        assertIsClosed(listener);
    }

}
