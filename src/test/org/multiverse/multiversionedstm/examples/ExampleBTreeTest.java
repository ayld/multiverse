package org.multiverse.multiversionedstm.examples;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;

public class ExampleBTreeTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void sizeEmptyBTree() {
        ExampleBTree tree = new ExampleBTree();
        assertEquals(0, tree.size());
    }

    //============== isEmpty =============

    @Test
    public void isEmptyOnEmptyBTree() {
        ExampleBTree tree = new ExampleBTree();
        assertTrue(tree.isEmpty());
    }

    @Test
    public void isEmptyOnNonEmptyBTree() {
        ExampleBTree<String, String> tree = new ExampleBTree<String, String>();
        tree.put("foo", "bar");
        assertFalse(tree.isEmpty());
    }


    // ============ height ==============

    @Test
    public void heightOnEmptyBTree() {
        ExampleBTree tree = new ExampleBTree();
        assertEquals(0, tree.height());
    }

    // ============ find =================

    @Test
    public void findOnEmptyBTree() {
        ExampleBTree<String, String> tree = new ExampleBTree<String, String>();
        String result = tree.find("foo");
        assertNull(result);
    }

    @Test
    public void findORootAndMatch() {
        ExampleBTree<String, String> tree = new ExampleBTree<String, String>();
        String key = "foo";
        String value = "bar";
        tree.put(key, value);
        String result = tree.find(key);
        assertEquals(value, result);
    }

    @Test
    public void findORootAndNoMatch() {
        ExampleBTree<String, String> tree = new ExampleBTree<String, String>();
        String key = "foo";
        String value = "bar";
        tree.put(key, value);
        String result = tree.find("non existing key");
        assertNull(result);
    }


    //================= remove =======================

    @Test
    public void removeOnEmptyBTree() {
        ExampleBTree<String, String> tree = new ExampleBTree<String, String>();
        String found = tree.remove("foo");
        assertNull(found);
        assertTrue(tree.isEmpty());
    }

    //================== put ================

    @Test
    public void putOnEmptyBTree() {
        ExampleBTree<String, String> tree = new ExampleBTree<String, String>();
        String key = "foo";
        String value = "bar";
        String replaced = tree.put(key, value);
        assertNull(replaced);
        assertEquals(1, tree.size());
    }

    @Test
    public void putReplaceRoot() {
        ExampleBTree<String, String> tree = new ExampleBTree<String, String>();
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
        ExampleBTree<String, String> tree = new ExampleBTree<String, String>();

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
        ExampleBTree<String, String> tree = new ExampleBTree<String, String>();

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
        ExampleBTree<String, String> tree = new ExampleBTree<String, String>();

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
        ExampleBTree<String, String> tree = new ExampleBTree<String, String>();

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
        ExampleBTree<String, String> tree = new ExampleBTree<String, String>();

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
        ExampleBTree<String, String> tree = new ExampleBTree<String, String>();
        tree.clear();
        assertTrue(tree.isEmpty());
    }

    @Test
    public void clearOnANonEmptyTree() {
        ExampleBTree<String, String> tree = new ExampleBTree<String, String>();
        tree.put("foo", "bar");
        tree.clear();
        assertTrue(tree.isEmpty());
    }

    // ============ persist =========================

    @Test
    public void persistEmptyTree() {
        ExampleBTree<String, String> tree = new ExampleBTree<String, String>();
        Handle<ExampleBTree<String, String>> handle = commit(stm, tree);

        Transaction t = stm.startTransaction();
        ExampleBTree<String, String> foundTree = t.read(handle);
        assertEquals(tree, foundTree);
    }

    //@Test
    public void persistNonEmptyTree() {
        ExampleBTree<String, String> tree = new ExampleBTree<String, String>();
        tree.put("1", "value1");
        tree.put("0", "value0");
        tree.put("2", "value2");

        Handle<ExampleBTree<String, String>> handle = commit(stm, tree);

        Transaction t = stm.startTransaction();
        ExampleBTree<String, String> foundTree = t.read(handle);
        assertEquals(tree, foundTree);
    }
}
