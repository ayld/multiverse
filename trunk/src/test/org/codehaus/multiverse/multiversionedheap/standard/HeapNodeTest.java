package org.codehaus.multiverse.multiversionedheap.standard;

import org.codehaus.multiverse.multiversionedheap.Deflated;
import org.codehaus.multiverse.multiversionedheap.DummyDeflated;
import static org.junit.Assert.*;
import org.junit.Test;

import static java.lang.StrictMath.max;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class HeapNodeTest {
    private HeapNode node;
    private static final int HANDLE_RANGE = 10000;
    private static final int CREATE_TREE_SANITY_CHECK = 1000;


    void assertSize(int expected) {
        assertEquals(expected, node.size());
    }

    void assertHeight(int depth) {
        assertEquals(depth, node.height());
    }

    void assertHandle(HeapNode node, long handle) {
        assertEquals(handle, node.getHandle());
    }

    void assertIsLeaf(HeapNode node) {
        assertNull(node.getLeft());
        assertNull(node.getRight());
    }

    public void assertNoLeft(HeapNode node) {
        assertNull(node.getLeft());
    }

    void assertLeft(HeapNode node, long value) {
        assertNotNull(node.getLeft());
        assertEquals(value, node.getLeft().getHandle());
    }

    public void assertNoRight(HeapNode node) {
        assertNull(node.getRight());
    }

    void assertRight(HeapNode node, long value) {
        assertNotNull(node.getRight());
        assertEquals(value, node.getRight().getHandle());
    }

    void assertIsBalanced() {
        int balanceFactor = node.balanceFactor();
        assertTrue("tree is not balanced, balanceFactor = " + balanceFactor, balanceFactor == 0 || balanceFactor == -1 || balanceFactor == 1);
    }

    HeapNode createLeaf(long handle) {
        return new HeapNode(new Block(new DummyDeflated(handle)), null, null);
    }

    public HeapNode createLeaf(long handle, long version) {
        return new HeapNode(new Block(new DummyDeflated(handle, version, "" + handle)), null, null);
    }


    HeapNode createBranch(long handle, long leftHandle, long rightHandle) {
        return new HeapNode(new Block(new DummyDeflated(handle)), createLeaf(leftHandle), createLeaf(rightHandle));
    }

    HeapNode createBranch(long handle, HeapNode left, HeapNode right) {
        return new HeapNode(new Block(new DummyDeflated(handle)), left, right);
    }

    HeapNode createLeftBranch(long handle, long leftHandle) {
        return new HeapNode(new Block(new DummyDeflated(handle)), createLeaf(leftHandle), null);
    }

    HeapNode createRightBranch(long handle, long rightHandle) {
        return new HeapNode(new Block(new DummyDeflated(handle)), null, createLeaf(rightHandle));
    }


    // ================== singleRotateLeft ===================

    @Test
    public void testSingleRotateLeft_noRight() {
        node = createLeftBranch(0, 1);

        try {
            node.singleRotateLeft();
            fail();
        } catch (IllegalStateException ex) {

        }
    }

    @Test
    public void testSingleRotateLeft() {
        HeapNode a = createLeaf(0);
        HeapNode b = createLeaf(2);
        HeapNode c = createLeaf(4);
        HeapNode q = createBranch(3, b, c);
        HeapNode p = createBranch(1, a, q);
        node = p;

        HeapNode result = p.singleRotateLeft();
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

    @Test
    public void testDoubleRotateLeft() {
        //todo
    }

    // ================== singleRotateRight ===================

    @Test
    public void testSingleRotateRight_noLeft() {
        node = createRightBranch(0, 1);

        try {
            node.singleRotateRight();
            fail();
        } catch (IllegalStateException ex) {

        }
    }

    @Test
    public void testSingleRotateRight() {
        HeapNode a = createLeaf(0);
        HeapNode b = createLeaf(2);
        HeapNode c = createLeaf(4);
        HeapNode p = createBranch(1, a, b);
        HeapNode q = createBranch(3, p, c);

        HeapNode result = q.singleRotateRight();
        assertHandle(result, 1);
        assertLeft(result, 0);
        assertIsLeaf(result.getLeft());
        assertRight(result, 3);
        assertLeft(result.getRight(), 2);
        assertIsLeaf(result.getRight().getLeft());
        assertRight(result.getRight(), 4);
        assertIsLeaf(result.getRight().getRight());
    }

    @Test
    public void testSingleRotatesAreSymetric() {
        HeapNode a = createLeaf(0);
        HeapNode b = createLeaf(2);
        HeapNode c = createLeaf(4);
        HeapNode p = createBranch(1, a, b);
        HeapNode q = createBranch(3, p, c);

        HeapNode result = q.singleRotateRight();
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

    @Test
    public void testDoubleRotateRight() {
        //todo
    }

    // ================== size =========================

    @Test
    public void testSizeLeaf() {
        node = createLeaf(1);
        assertSize(1);
    }

    @Test
    public void testSize_withLeftBranch() {
        node = createBranch(2, createLeaf(1), null);
        assertSize(2);
    }

    @Test
    public void testSize_withRightBranch() {
        node = createBranch(1, null, createLeaf(3));
        assertSize(2);
    }

    @Test
    public void testSize_withBranches() {
        node = createBranch(3, createBranch(0, 1, 2), createLeaf(4));
        assertSize(5);
    }

    //===================== height =================

    @Test
    public void testHeight_Leaf() {
        node = createLeaf(1);
        assertHeight(1);
    }

    @Test
    public void testHeight_withLeftBranch() {
        node = createBranch(2, createLeaf(1), null);
        assertHeight(2);
    }

    @Test
    public void testHeight_withRightBranch() {
        node = createBranch(1, null, createLeaf(3));
        assertHeight(2);
    }

    @Test
    public void testHeight_withBranches() {
        node = createBranch(3, createBranch(0, 1, 2), createLeaf(4));
        assertHeight(3);
    }

    // =================== find ===========================

    @Test
    public void testFind_foundInLeaf() {
        node = createLeaf(0);

        HeapNode found = node.find(0);
        assertSame(node, found);
    }

    @Test
    public void testFind_notFoundInLeaf() {
        node = createLeaf(0);

        HeapNode found = node.find(1);
        assertNull(found);
    }

    @Test
    public void testFind_inLeftBranch() {
        node = createBranch(2, createLeaf(1), createLeaf(3));

        HeapNode found = node.find(1);
        assertSame(node.getLeft(), found);
    }

    @Test
    public void testFind_inRightBranch() {
        node = createBranch(2, createLeaf(1), createLeaf(3));

        HeapNode found = node.find(3);
        assertSame(node.getRight(), found);
    }

    //================== balance factor ====================

    @Test
    public void testLeaf() {
        node = createLeaf(1);
        int balanceFactor = node.balanceFactor();
        assertEquals(0, balanceFactor);
    }

    @Test
    public void testLeftHeavy() {
        node = createLeftBranch(1, 2);
        int balanceFactor = node.balanceFactor();
        assertEquals(-1, balanceFactor);
    }

    @Test
    public void testRightHeavy() {
        node = createRightBranch(1, 2);
        int balanceFactor = node.balanceFactor();
        assertEquals(1, balanceFactor);
    }

    @Test
    public void testWithLeftRightLeaf() {
        node = createBranch(1, 0, 2);
        int balanceFactor = node.balanceFactor();
        assertEquals(0, balanceFactor);
    }

    // =================== some integration tests that test balancing, size etc ===============

    private Set<Long> handles = new HashSet<Long>();

    long createRandomHandle() {
        long handle = Math.round(Math.random() * HANDLE_RANGE);
        handles.add(handle);
        return handle;
    }

    void createTree(int count) {
        handles.clear();

        int checkMod = max(count / CREATE_TREE_SANITY_CHECK, 1);

        for (int k = 0; k < count; k++) {
            long handle = createRandomHandle();

            if (node == null) {
                node = new HeapNode(new Block(new DummyDeflated(handle)), null, null);
            } else {
                Deflated deflated = new DummyDeflated(handle, 1, "foo");
                node = node.createNewForWrite(deflated, 1, new Stack());
            }

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
    }

    @Test
    public void testCreate_2() {
        testCreate(2);
    }

    @Test
    public void testCreate_3() {
        testCreate(3);
    }

    @Test
    public void testCreate_10() {
        testCreate(10);
    }

    @Test
    public void testCreate_100() {
        testCreate(100);
    }

    @Test
    public void testCreate_1000() {
        testCreate(1000);
    }

    @Test
    public void testCreate_10000() {
        testCreate(10000);
    }

    @Test
    public void testCreate_100000() {
        testCreate(100000);
    }

    /*
    @Test
    public void testWriteConflict_olderVersion() {
        long handle = createHandle();
        long version = 10;
        long newCommitVersion = version + 1;
        long maximumVersion = newCommitVersion - 2;

        HeapNode oldNode = createLeaf(handle, version);
        HeapNode result = oldNode.createNewForWrite(createBlock(handle, newCommitVersion), maximumVersion);
        assertNull(result);
    }

    Block createBlock(long handle, long commitVersion){
        return new Block(new StringDeflatable(handle).___deflate(commitVersion)) ;
    }

    @Test
    public void testWriteConflict_noConflictDetected() {
        long handle = createHandle();
        long firstCommitVersion = 10;
        long newCommitVersion = firstCommitVersion + 1;
        long maximumVersion = newCommitVersion - 1;

        HeapNode oldNode = createLeaf(handle, firstCommitVersion);
        HeapNode result = oldNode.createNewForWrite(createBlock(handle, newCommitVersion), maximumVersion);
        assertNotNull(result);
    }

    @Test
    public void testWriteConflict_noConflictDetectedOnLegacyData() {
        long handle = createHandle();
        long firstCommitVersion = 10;
        long newCommitVersion = firstCommitVersion + 10;
        long maximumVersion = newCommitVersion - 1;

        HeapNode oldNode = createLeaf(handle, firstCommitVersion);
        HeapNode result = oldNode.createNewForWrite(createBlock(handle, newCommitVersion), maximumVersion);
        assertNotNull(result);
    } */
}
