package org.codehaus.multiverse.multiversionedheap.standard;

import org.codehaus.multiverse.multiversionedheap.DummyDeflated;
import org.codehaus.multiverse.util.latches.Latch;
import org.codehaus.multiverse.util.latches.StandardLatch;
import static org.junit.Assert.*;
import org.junit.Test;

public class HeapNodeTest_createNewForAddingListenerTest {

    public Block createBlock(long handle) {
        return new Block(new DummyDeflated(handle));
    }

    public HeapNode createLeaf(Block block) {
        return new HeapNode(block);
    }

    public HeapNode createLeaf(int handle) {
        return createLeaf(createBlock(handle));
    }

    @Test
    public void inLeaf() {
        long handle = 10;
        HeapNode node = new HeapNode(new Block(new DummyDeflated(handle)));

        Latch latch = new StandardLatch();
        HeapNode newNode = node.createNewForAddingListener(10, latch);
        assertNotNull(newNode);
        assertFalse(node == newNode);
        assertFalse(latch.isOpen());
        //todo: content chechen
    }

    @Test
    public void notInLeaf() {
        long handle = 10;
        HeapNode node = new HeapNode(new Block(new DummyDeflated(handle + 1)));

        Latch latch = new StandardLatch();
        HeapNode newNode = node.createNewForAddingListener(handle, latch);
        assertNull(newNode);
        assertFalse(latch.isOpen());
    }

    @Test
    public void inLeftLeaf() {
        //todo er moet rijker informatie op de left gezet worden
        HeapNode root = new HeapNode(createBlock(10), createLeaf(5), createLeaf(15));

        Latch latch = new StandardLatch();
        HeapNode newRoot = root.createNewForAddingListener(5, latch);
        assertNotNull(newRoot);
        assertSame(root.getRight(), newRoot.getRight());
        assertSame(root.getBlock(), newRoot.getBlock());
        //todo block in left  moet nog gecontroleerd worden
        assertFalse(latch.isOpen());
    }

    @Test
    public void notInLeftLeaf() {
        HeapNode root = new HeapNode(createBlock(10), createLeaf(5), createLeaf(15));

        Latch latch = new StandardLatch();
        HeapNode newRoot = root.createNewForAddingListener(4, latch);
        assertNull(newRoot);
        assertFalse(latch.isOpen());
    }

    @Test
    public void inRightLeaf() {
        //todo er moet rijker informatie op de rechterleaf gezet worden
        HeapNode root = new HeapNode(createBlock(10), createLeaf(5), createLeaf(15));

        Latch latch = new StandardLatch();
        HeapNode newRoot = root.createNewForAddingListener(15, latch);
        assertNotNull(newRoot);
        assertSame(root.getLeft(), newRoot.getLeft());
        assertSame(root.getBlock(), newRoot.getBlock());
        //todo block in right root moet nog gecontroleerd worden
        assertFalse(latch.isOpen());
    }

    @Test
    public void notInRightLeaf() {
        HeapNode root = new HeapNode(createBlock(10), createLeaf(5), createLeaf(15));

        Latch latch = new StandardLatch();
        HeapNode newRoot = root.createNewForAddingListener(20, latch);
        assertNull(newRoot);
        assertFalse(latch.isOpen());
    }
}
