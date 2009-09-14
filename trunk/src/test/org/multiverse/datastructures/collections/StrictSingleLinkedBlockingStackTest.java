package org.multiverse.datastructures.collections;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

import java.util.Iterator;

/**
 * @author Peter Veentjer
 */
public class StrictSingleLinkedBlockingStackTest {
    private Stm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
        setThreadLocalTransaction(null);
    }

    @After
    public void tearDown() {
        //assertNoInstrumentationProblems();
    }

    @Test
    public void constructionNoArg() {
        StrictSingleLinkedBlockingStack<String> s = new StrictSingleLinkedBlockingStack<String>();
        assertEquals(0, s.size());
        assertEquals(Integer.MAX_VALUE, s.getMaximumCapacity());
    }

    @Test
    public void constructionWithMaximumCapacity() {
        int maxCapacity = 10;
        StrictSingleLinkedBlockingStack<String> s = new StrictSingleLinkedBlockingStack<String>(maxCapacity);
        assertEquals(0, s.size());
        assertEquals(maxCapacity, s.getMaximumCapacity());
    }

    @Test
    public void testScenario() {
        StrictSingleLinkedBlockingStack<String> s = new StrictSingleLinkedBlockingStack<String>();
        s.push("foo");
        s.push("bar");
        assertEquals(2, s.size());
        assertEquals("bar", s.pop());
        assertEquals(1, s.size());

        s.push("bla");
        assertEquals(2, s.size());
        assertEquals("bla", s.pop());
        assertEquals("foo", s.pop());
        assertEquals(0, s.size());
    }

    @Test
    public void clear() {
        StrictSingleLinkedBlockingStack<String> s = new StrictSingleLinkedBlockingStack<String>();
        s.push("foo");
        s.push("bar");
        s.clear();
        assertEquals(0, s.size());

        s.clear();
        assertEquals(0, s.size());
    }

    @Test
    public void isEmpty() {
        StrictSingleLinkedBlockingStack<String> s = new StrictSingleLinkedBlockingStack<String>();
        assertTrue(s.isEmpty());

        s.push("foo");
        s.push("bar");
        assertFalse(s.isEmpty());
    }

    @Test
    public void testToString() {
        StrictSingleLinkedBlockingStack<String> s = new StrictSingleLinkedBlockingStack<String>();
        assertEquals("[]", s.toString());

        s.push("1");
        assertEquals("[1]", s.toString());
        s.push("2");
        s.push("3");
        assertEquals("[3, 2, 1]", s.toString());
    }

    @Test
    public void peek() {
        StrictSingleLinkedBlockingStack<String> s = new StrictSingleLinkedBlockingStack<String>();
        assertNull(s.peek());

        s.push("1");
        assertEquals("1", s.peek());

        s.push("2");
        assertEquals("2", s.peek());
    }

    @Test
    public void testEmptyIterator() {
        StrictSingleLinkedBlockingStack<String> s = new StrictSingleLinkedBlockingStack<String>();
        assertFalse(s.iterator().hasNext());
    }

    @Test
    public void testSingleItemIterator() {
        StrictSingleLinkedBlockingStack<String> s = new StrictSingleLinkedBlockingStack<String>();
        s.push("1");

        Iterator<String> it = s.iterator();
        assertTrue(it.hasNext());
        assertEquals("1", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testMultipleItemsIterator() {
        StrictSingleLinkedBlockingStack<String> s = new StrictSingleLinkedBlockingStack<String>();
        s.push("1");
        s.push("2");
        s.push("3");

        Iterator<String> it = s.iterator();
        assertTrue(it.hasNext());
        assertEquals("3", it.next());
        assertTrue(it.hasNext());
        assertEquals("2", it.next());
        assertTrue(it.hasNext());
        assertEquals("1", it.next());
        assertFalse(it.hasNext());
    }


    @Test
    public void equals() {

    }

    @Test
    public void testHash() {

    }
}
