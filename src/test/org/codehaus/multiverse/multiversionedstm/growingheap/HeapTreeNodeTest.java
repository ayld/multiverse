package org.codehaus.multiverse.multiversionedstm.growingheap;

import junit.framework.TestCase;
import org.codehaus.multiverse.multiversionedstm.DummyDehydratedStmObject;

public class HeapTreeNodeTest extends TestCase {
    private HeapTreeNode node;

    public void assertSize(int expected) {
        assertEquals(expected, node.size());
    }

    public HeapTreeNode createLeaf(long handle) {
        return new HeapTreeNode(new DummyDehydratedStmObject(handle),1, null, null);
    }

    // ================== size =========================

    public void testSizeLeaf() {
        node = createLeaf(1);
        assertSize(1);
    }

    public void testSize_withLeftBranch() {
        node = new HeapTreeNode(new DummyDehydratedStmObject(2), 0, createLeaf(1), null);
        assertSize(2);
    }

    public void testSize_withRightBranch() {
        node = new HeapTreeNode(new DummyDehydratedStmObject(1), 0, null, createLeaf(3));
        assertSize(2);
    }

    public void testSize_withBranches() {
        node = new HeapTreeNode(new DummyDehydratedStmObject(2), 0, createLeaf(1), createLeaf(3));
        assertSize(3);
    }

    // =================== find ===========================

    public void testFind_foundInLeaf() {
        node = createLeaf(0);

        HeapTreeNode found = node.find(0);
        assertSame(node, found);
    }

    public void testFind_notFoundInLeaf() {
        node = createLeaf(0);

        HeapTreeNode found = node.find(1);
        assertNull(found);
    }

    public void testFind_inLeftBranch() {
        node = new HeapTreeNode(new DummyDehydratedStmObject(2), 1, createLeaf(1), createLeaf(3));

        HeapTreeNode found = node.find(1);
        assertSame(node.getLeft(), found);
    }

    public void testFind_inRighBranch() {
        node = new HeapTreeNode(new DummyDehydratedStmObject(2), 1, createLeaf(1), createLeaf(3));

        HeapTreeNode found = node.find(3);
        assertSame(node.getRight(), found);
    }
}
