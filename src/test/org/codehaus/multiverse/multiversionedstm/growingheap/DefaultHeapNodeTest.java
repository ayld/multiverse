package org.codehaus.multiverse.multiversionedstm.growingheap;

import junit.framework.TestCase;
import org.codehaus.multiverse.multiversionedstm.DummyDehydratedStmObject;
import org.codehaus.multiverse.multiversionedstm.growingheap.heapnodes.HeapNode;

import static java.lang.Math.max;
import java.util.HashSet;
import java.util.Set;

public class DefaultHeapNodeTest extends TestCase {
    private DefaultHeapNode node;
    private static final int HANDLE_RANGE = 10000;
    private static final int CREATE_TREE_SANITY_CHECK = 1000;

    public void assertSize(int expected) {
        assertEquals(expected, node.size());
    }

    public void assertHeight(int depth) {
        assertEquals(depth, node.height());
    }

    public void assertHandle(HeapNode node, long handle) {
        assertEquals(handle, node.getHandle());
    }

    public void assertIsLeaf(DefaultHeapNode node) {
        assertNull(node.getLeft());
        assertNull(node.getRight());
    }

    public void assertNoLeft(DefaultHeapNode node) {
        assertNull(node.getLeft());
    }

    public void assertLeft(DefaultHeapNode node, long value) {
        assertNotNull(node.getLeft());
        assertEquals(value, node.getLeft().getHandle());
    }

    public void assertNoRight(DefaultHeapNode node) {
        assertNull(node.getRight());
    }

    public void assertRight(DefaultHeapNode node, long value) {
        assertNotNull(node.getRight());
        assertEquals(value, node.getRight().getHandle());
    }

    public void assertIsBalanced() {
        int balanceFactor = node.balanceFactor();
        assertTrue("tree is not balanced, balanceFactor = " + balanceFactor, balanceFactor == 0 || balanceFactor == -1 || balanceFactor == 1);
    }

    public DefaultHeapNode createLeaf(long handle) {
        return new DefaultHeapNode(new DummyDehydratedStmObject(handle), 1, null, null);
    }

    public DefaultHeapNode createBranch(long handle, long leftHandle, long rightHandle) {
        return new DefaultHeapNode(new DummyDehydratedStmObject(handle), 1, createLeaf(leftHandle), createLeaf(rightHandle));
    }

    public DefaultHeapNode createBranch(long handle, DefaultHeapNode left, DefaultHeapNode right) {
        return new DefaultHeapNode(new DummyDehydratedStmObject(handle), 1, left, right);
    }

    public DefaultHeapNode createLeftBranch(long handle, long leftHandle) {
        return new DefaultHeapNode(new DummyDehydratedStmObject(handle), 1, createLeaf(leftHandle), null);
    }

    public DefaultHeapNode createRightBranch(long handle, long rightHandle) {
        return new DefaultHeapNode(new DummyDehydratedStmObject(handle), 1, null, createLeaf(rightHandle));
    }


    // ================== singleRotateLeft ===================

    public void testSingleRotateLeft_noRight() {
        node = createLeftBranch(0, 1);

        try {
            node.singleRotateLeft();
            fail();
        } catch (IllegalStateException ex) {

        }
    }

    public void testSingleRotateLeft() {
        DefaultHeapNode a = createLeaf(0);
        DefaultHeapNode b = createLeaf(2);
        DefaultHeapNode c = createLeaf(4);
        DefaultHeapNode q = createBranch(3, b, c);
        DefaultHeapNode p = createBranch(1, a, q);
        node = p;

        DefaultHeapNode result = p.singleRotateLeft();
        assertHandle(result, 3);
        assertLeft(result, 1);
        assertLeft(result.getLeft(), 0);
        assertIsLeaf(result.getLeft().getLeft());
        assertRight(result.getLeft(), 2);
        assertIsLeaf(result.getLeft().getRight());
        assertRight(result, 4);
        assertIsLeaf(result.getRight());
    }

    // ================== doubleRotateLeft ===================

    public void testDoubleRotateLeft() {
        //todo
    }

    // ================== singleRotateRight ===================

    public void testSingleRotateRight_noLeft() {
        node = createRightBranch(0, 1);

        try {
            node.singleRotateRight();
            fail();
        } catch (IllegalStateException ex) {

        }
    }

    public void testSingleRotateRight() {
        DefaultHeapNode a = createLeaf(0);
        DefaultHeapNode b = createLeaf(2);
        DefaultHeapNode c = createLeaf(4);
        DefaultHeapNode p = createBranch(1, a, b);
        DefaultHeapNode q = createBranch(3, p, c);

        DefaultHeapNode result = q.singleRotateRight();
        assertHandle(result, 1);
        assertLeft(result, 0);
        assertIsLeaf(result.getLeft());
        assertRight(result, 3);
        assertLeft(result.getRight(), 2);
        assertIsLeaf(result.getRight().getLeft());
        assertRight(result.getRight(), 4);
        assertIsLeaf(result.getRight().getRight());
    }

    public void testSingleRotatesAreSymetric() {
        DefaultHeapNode a = createLeaf(0);
        DefaultHeapNode b = createLeaf(2);
        DefaultHeapNode c = createLeaf(4);
        DefaultHeapNode p = createBranch(1, a, b);
        DefaultHeapNode q = createBranch(3, p, c);

        DefaultHeapNode result = q.singleRotateRight();
        result = result.singleRotateLeft();

        assertHandle(result, 3);
        assertLeft(result, 1);
        assertLeft(result.getLeft(), 0);
        assertIsLeaf(result.getLeft().getLeft());
        assertRight(result.getLeft(), 2);
        assertIsLeaf(result.getLeft().getRight());
        assertRight(result, 4);
        assertIsLeaf(result.getRight());
    }

    // ================== doubleRotateRight =========================

    public void testDoubleRotateRight() {
        //todo
    }

    // ================== size =========================

    public void testSizeLeaf() {
        node = createLeaf(1);
        assertSize(1);
    }

    public void testSize_withLeftBranch() {
        node = new DefaultHeapNode(new DummyDehydratedStmObject(2), 0, createLeaf(1), null);
        assertSize(2);
    }

    public void testSize_withRightBranch() {
        node = new DefaultHeapNode(new DummyDehydratedStmObject(1), 0, null, createLeaf(3));
        assertSize(2);
    }

    public void testSize_withBranches() {
        node = new DefaultHeapNode(new DummyDehydratedStmObject(3), 0, createBranch(0, 1, 2), createLeaf(4));
        assertSize(5);
    }

    //===================== height =================

    public void testHeight_Leaf() {
        node = createLeaf(1);
        assertHeight(1);
    }

    public void testHeight_withLeftBranch() {
        node = new DefaultHeapNode(new DummyDehydratedStmObject(2), 0, createLeaf(1), null);
        assertHeight(2);
    }

    public void testHeight_withRightBranch() {
        node = new DefaultHeapNode(new DummyDehydratedStmObject(1), 0, null, createLeaf(3));
        assertHeight(2);
    }

    public void testHeight_withBranches() {
        node = new DefaultHeapNode(new DummyDehydratedStmObject(3), 0, createBranch(0, 1, 2), createLeaf(4));
        assertHeight(3);
    }

    // =================== find ===========================

    public void testFind_foundInLeaf() {
        node = createLeaf(0);

        HeapNode found = node.find(0);
        assertSame(node, found);
    }

    public void testFind_notFoundInLeaf() {
        node = createLeaf(0);

        HeapNode found = node.find(1);
        assertNull(found);
    }

    public void testFind_inLeftBranch() {
        node = new DefaultHeapNode(new DummyDehydratedStmObject(2), 1, createLeaf(1), createLeaf(3));

        HeapNode found = node.find(1);
        assertSame(node.getLeft(), found);
    }

    public void testFind_inRightBranch() {
        node = new DefaultHeapNode(new DummyDehydratedStmObject(2), 1, createLeaf(1), createLeaf(3));

        HeapNode found = node.find(3);
        assertSame(node.getRight(), found);
    }

    //================== balance factor ====================

    public void testLeaf() {
        node = createLeaf(1);
        int balanceFactor = node.balanceFactor();
        assertEquals(0, balanceFactor);
    }

    public void testLeftHeavy() {
        node = createLeftBranch(1, 2);
        int balanceFactor = node.balanceFactor();
        assertEquals(-1, balanceFactor);
    }

    public void testRightHeavy() {
        node = createRightBranch(1, 2);
        int balanceFactor = node.balanceFactor();
        assertEquals(1, balanceFactor);
    }

    public void testWithLeftRightLeaf() {
        node = createBranch(1, 0, 2);
        int balanceFactor = node.balanceFactor();
        assertEquals(0, balanceFactor);
    }

    // =================== some integration tests that test balancing, size etc ===============

    private Set<Long> handles = new HashSet<Long>();

    public long createRandomHandle() {
        long handle = Math.round(Math.random() * HANDLE_RANGE);
        handles.add(handle);
        return handle;
    }

    public void createTree(int count) {
        handles.clear();

        int checkMod = max(count / CREATE_TREE_SANITY_CHECK, 1);

        for (int k = 0; k < count; k++) {
            long handle = createRandomHandle();

            if (node == null)
                node = new DefaultHeapNode(new DummyDehydratedStmObject(handle), 0, null, null);
            else
                node = node.createNew(new DummyDehydratedStmObject(handle), 0);

            if (k % checkMod == 0) {
                assertIsBalanced();
                assertSize(handles.size());

                for (long h : handles) {
                    HeapNode found = node.find(h);
                    assertNotNull(found);
                }
            }
        }

        assertIsBalanced();
    }

    public void testCreate(int count) {
        createTree(count);

        System.out.println("number of cores: " + Runtime.getRuntime().availableProcessors());
    }

    public void testCreate_2() {
        testCreate(2);
    }

    public void testCreate_3() {
        testCreate(3);
    }

    public void testCreate_10() {
        testCreate(10);
    }

    public void testCreate_100() {
        testCreate(100);
    }

    public void testCreate_1000() {
        testCreate(1000);
    }

    public void testCreate_10000() {
        testCreate(10000);
    }

    public void testCreate_100000() {
        testCreate(100000);
    }
}
