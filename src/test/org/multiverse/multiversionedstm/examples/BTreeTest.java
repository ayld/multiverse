package org.multiverse.multiversionedstm.examples;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Originator;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;

public class BTreeTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void sizeEmptyBTree() {
        BTree tree = new BTree();
        assertEquals(0, tree.size());
    }

    //============== isEmpty =============

    @Test
    public void isEmptyOnEmptyBTree() {
        BTree tree = new BTree();
        assertTrue(tree.isEmpty());
    }

    @Test
    public void isEmptyOnNonEmptyBTree() {
        BTree<String, String> tree = new BTree<String, String>();
        tree.put("foo", "bar");
        assertFalse(tree.isEmpty());
    }


    // ============ height ==============

    @Test
    public void heightOnEmptyBTree() {
        BTree tree = new BTree();
        assertEquals(0, tree.height());
    }

    // ============ find =================

    @Test
    public void findOnEmptyBTree() {
        BTree<String, String> tree = new BTree<String, String>();
        String result = tree.find("foo");
        assertNull(result);
    }

    @Test
    public void findORootAndMatch() {
        BTree<String, String> tree = new BTree<String, String>();
        String key = "foo";
        String value = "bar";
        tree.put(key, value);
        String result = tree.find(key);
        assertEquals(value, result);
    }

    @Test
    public void findORootAndNoMatch() {
        BTree<String, String> tree = new BTree<String, String>();
        String key = "foo";
        String value = "bar";
        tree.put(key, value);
        String result = tree.find("non existing key");
        assertNull(result);
    }


    //================= remove =======================

    @Test
    public void removeOnEmptyBTree() {
        BTree<String, String> tree = new BTree<String, String>();
        String found = tree.remove("foo");
        assertNull(found);
        assertTrue(tree.isEmpty());
    }

    //================== put ================

    @Test
    public void putOnEmptyBTree() {
        BTree<String, String> tree = new BTree<String, String>();
        String key = "foo";
        String value = "bar";
        String replaced = tree.put(key, value);
        assertNull(replaced);
        assertEquals(1, tree.size());
    }

    @Test
    public void putReplaceRoot() {
        BTree<String, String> tree = new BTree<String, String>();
        String key = "foo";
        String oldValue = "bar";
        tree.put(key, oldValue);

        String newValue = "banana";
        String replaced = tree.put(key, newValue);
        assertSame(oldValue, replaced);
        assertEquals(1, tree.size());
        assertEquals(newValue, tree.find(key));
    }

    @Test
    public void putReplaceNonEmptyRoot() {
        BTree<String, String> tree = new BTree<String, String>();

        tree.put("1", "oldValue");
        tree.put("0", "banana");
        tree.put("2", "appel");

        String replaced = tree.put("1", "newValue");
        assertEquals("oldValue", replaced);
        assertEquals(3, tree.size());
        assertEquals("newValue", tree.find("1"));
        assertEquals("banana", tree.find("0"));
        assertEquals("appel", tree.find("2"));
    }


    @Test
    public void putAddToLeft() {
        BTree<String, String> tree = new BTree<String, String>();

        tree.put("1", "root");

        String newValue = "banana";
        String replaced = tree.put("0", newValue);
        assertNull(replaced);
        assertEquals(2, tree.size());
        assertEquals("root", tree.find("1"));
        assertEquals(newValue, tree.find("0"));
    }

    @Test
    public void putReplaceLeft() {
        BTree<String, String> tree = new BTree<String, String>();

        tree.put("1", "root");
        tree.put("0", "oldValue");

        String replaced = tree.put("0", "newValue");
        assertEquals("oldValue", replaced);
        assertEquals(2, tree.size());
        assertEquals("root", tree.find("1"));
        assertEquals("newValue", tree.find("0"));
    }

    @Test
    public void putAddToRight() {
        BTree<String, String> tree = new BTree<String, String>();

        tree.put("1", "root");

        String newValue = "banana";
        String replaced = tree.put("2", newValue);
        assertNull(replaced);
        assertEquals(2, tree.size());
        assertEquals("root", tree.find("1"));
        assertEquals(newValue, tree.find("2"));
    }

    @Test
    public void putReplaceRight() {
        BTree<String, String> tree = new BTree<String, String>();

        tree.put("1", "root");
        tree.put("2", "oldValue");

        String replaced = tree.put("2", "newValue");
        assertEquals("oldValue", replaced);
        assertEquals(2, tree.size());
        assertEquals("root", tree.find("1"));
        assertEquals("newValue", tree.find("2"));
    }


    // ================== clear =====================

    @Test
    public void clearOnEmptyBTreeIsIgnored() {
        BTree<String, String> tree = new BTree<String, String>();
        tree.clear();
        assertTrue(tree.isEmpty());
    }

    @Test
    public void clearOnANonEmptyTree() {
        BTree<String, String> tree = new BTree<String, String>();
        tree.put("foo", "bar");
        tree.clear();
        assertTrue(tree.isEmpty());
    }

    // ============ persist =========================

    @Test
    public void persistEmptyTree() {
        BTree<String, String> tree = new BTree<String, String>();
        Originator<BTree<String, String>> originator = commit(stm, tree);

        Transaction t = stm.startTransaction();
        BTree<String, String> foundTree = t.read(originator);
        assertEquals(tree, foundTree);
    }

    @Test
    public void persistNonEmptyTree() {
        BTree<String, String> tree = new BTree<String, String>();
        tree.put("1", "value1");
        tree.put("0", "value0");
        tree.put("2", "value2");

        Originator<BTree<String, String>> originator = commit(stm, tree);

        Transaction t = stm.startTransaction();
        BTree<String, String> foundTree = t.read(originator);
        assertEquals(tree, foundTree);
    }
}
