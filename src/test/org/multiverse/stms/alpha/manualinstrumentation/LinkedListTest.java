package org.multiverse.stms.alpha.manualinstrumentation;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

import java.util.Iterator;

public class LinkedListTest {
    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
        setThreadLocalTransaction(null);
    }

    @Test
    public void testEmptyList() {
        LinkedList<Integer> list = new LinkedList<Integer>();
        assertTrue(list.isEmpty());
    }

    @Test
    public void testAdd() {
        LinkedList<String> list = new LinkedList<String>();
        list.add("a");
        list.add("b");
        list.add("c");

        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
    }

    @Test
    public void testAddInFront() {
        LinkedList<String> list = new LinkedList<String>();
        list.addInFront("a");
        list.addInFront("b");
        list.addInFront("c");

        assertEquals(3, list.size());
        assertEquals("c", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("a", list.get(2));
    }

    @Test
    public void testRemoval() {
        LinkedList<String> list = new LinkedList<String>();
        list.add("a");
        list.add("b");
        list.add("c");

        assertEquals("a", list.removeFirst());
        assertEquals(2, list.size());
        assertEquals("b", list.get(0));
        assertEquals("c", list.get(1));

        assertEquals("c", list.removeLast());
        assertEquals(1, list.size());
        assertEquals("b", list.get(0));

        assertEquals("b", list.removeLast());
        assertEquals(0, list.size());
    }

    @Test
    public void clearOnEmptyList() {
        LinkedList<String> list = new LinkedList<String>();
        list.clear();

        assertEquals(0, list.size());
    }

    @Test
    public void clearOnNonEmptyList() {
        LinkedList<String> list = new LinkedList<String>();
        list.add("a");
        list.add("b");
        list.add("c");
        list.clear();

        assertEquals(0, list.size());
    }

    @Test
    public void testLinkedNode() {
        LinkedNode node = new LinkedNode(10);
        assertNull(node.getNext());
        assertNull(node.getPrevious());
        assertEquals(10, node.getValue());
    }

    @Test
    public void toStringEmptyList() {
        LinkedList list = new LinkedList();
        assertEquals("[]", list.toString());
    }

    @Test
    public void toStringSingleton() {
        LinkedList<String> list = new LinkedList<String>();
        list.add("foo");
        assertEquals("[foo]", list.toString());
    }

    @Test
    public void toStringListWithMultipleItems() {
        LinkedList<String> list = new LinkedList<String>();
        list.add("foo");
        list.add("bar");
        list.add("java");
        assertEquals("[foo,bar,java]", list.toString());
    }

    @Test
    public void iteratorEmptyList() {
        LinkedList<String> list = new LinkedList<String>();
        Iterator<String> it = list.iterator();
        assertFalse(it.hasNext());
    }

    @Test
    public void iteratorSingletonList() {
        LinkedList<String> list = new LinkedList<String>();
        list.add("foo");

        Iterator<String> it = list.iterator();
        assertTrue(it.hasNext());
        assertEquals("foo", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void iteratorMultiItemList() {
        LinkedList<String> list = new LinkedList<String>();
        list.add("foo");
        list.add("bar");
        list.add("java");

        Iterator<String> it = list.iterator();
        assertTrue(it.hasNext());
        assertEquals("foo", it.next());
        assertTrue(it.hasNext());
        assertEquals("bar", it.next());
        assertTrue(it.hasNext());
        assertEquals("java", it.next());
        assertFalse(it.hasNext());
    }
}
