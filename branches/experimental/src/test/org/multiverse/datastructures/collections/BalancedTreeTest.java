package org.multiverse.datastructures.collections;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.assertNoInstrumentationProblems;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

public class BalancedTreeTest {

    @Before
    public void setUp() {
        setThreadLocalTransaction(null);
    }

    @After
    public void tearDown() {
        assertNoInstrumentationProblems();
    }

    @Test
    public void test() {
        BalancedTree tree = new BalancedTree();
        assertEquals(0, tree.size());
    }

    @Test
    public void testClearEmptyTree() {
        BalancedTree tree = new BalancedTree();
        tree.clear();
        assertEquals(0, tree.size());
        assertEquals(0, tree.height());
    }

    @Test
    public void testClearNonEmptyTree() {
        BalancedTree<String, String> tree = new BalancedTree<String, String>();
        tree.put("b", "2");
        tree.put("a", "1");
        tree.put("c", "3");

        tree.clear();
        assertEquals(0, tree.size());
        assertEquals(0, tree.height());
        assertNull(tree.get("a"));
        assertNull(tree.get("b"));
        assertNull(tree.get("c"));
    }

    @Test
    public void testPutFirst() {
        BalancedTree<String, String> tree = new BalancedTree<String, String>();
        tree.put("foo", "bar");
        assertEquals("bar", tree.get("foo"));
        assertEquals(null, tree.get("bar"));
    }

    @Test
    public void testComplex() {
        BalancedTree<String, String> tree = new BalancedTree<String, String>();
        tree.put("a", "1");
        tree.put("b", "2");
        tree.put("c", "3");
        assertEquals("1", tree.get("a"));
        assertEquals("2", tree.get("b"));
        assertEquals("3", tree.get("c"));
        assertEquals(3, tree.size());
        //assertEquals(3, tree.height());
        assertEquals(null, tree.get("bar"));
    }

    @Test
    public void toStringEmptyTree() {
        BalancedTree<String, String> tree = new BalancedTree<String, String>();
        assertEquals("[]", tree.toString());
    }

    @Test
    public void toStringSingletonTree() {
        //todo
    }

    @Test
    public void toStringFilledTree() {
        //todo
    }
}
