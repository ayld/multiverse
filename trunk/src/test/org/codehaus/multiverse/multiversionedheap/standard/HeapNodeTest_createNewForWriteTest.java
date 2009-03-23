package org.codehaus.multiverse.multiversionedheap.standard;

import org.codehaus.multiverse.multiversionedheap.Deflated;
import org.codehaus.multiverse.multiversionedheap.DummyDeflated;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.Stack;

public class HeapNodeTest_createNewForWriteTest {

    @Test
    public void updateLeaf() {
        Deflated version1Deflated = new DummyDeflated(1, 1, "foo");
        Deflated version2Deflated = new DummyDeflated(1, 2, "bar");
        HeapNode version1Leaf = new HeapNode(new Block(version1Deflated));

        Stack listenersToWakeup = new Stack();
        HeapNode version2Leaf = version1Leaf.createNewForWrite(version2Deflated, version1Deflated.___getVersion(), listenersToWakeup);

        assertNotNull(version2Leaf);
        assertTrue(listenersToWakeup.isEmpty());
    }

    @Test
    public void updateLeafWithListeners() {

    }

    @Test
    public void updateLeafWithWriteConflict() {

    }

    @Test
    public void updateLeftWithWriteConflict() {
    }

    @Test
    public void updateRightWithWriteConflict() {
    }

    @Test
    public void updateLeft() {
    }

    @Test
    public void updateRight() {
    }

    @Test
    public void insertLeftOfLeaf() {
    }

    @Test
    public void insertRightOfLeaf() {
    }

    @Test
    public void updateReleasesLocks() {

    }
}
