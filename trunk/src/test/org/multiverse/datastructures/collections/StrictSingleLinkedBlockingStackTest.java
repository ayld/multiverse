package org.multiverse.datastructures.collections;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.RetryError;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

import static java.util.Arrays.asList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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

    // ===================== constructor =============================================

    @Test
    public void constructionNoArg() {
        long version = stm.getClockVersion();

        StrictSingleLinkedBlockingStack<String> stack = new StrictSingleLinkedBlockingStack<String>();

        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(0, stack.size());
        assertEquals(Integer.MAX_VALUE, stack.getMaximumCapacity());
    }

    @Test
    public void constructionWithMaximumCapacity() {
        long version = stm.getClockVersion();

        int maxCapacity = 10;
        StrictSingleLinkedBlockingStack<String> stack = new StrictSingleLinkedBlockingStack<String>(maxCapacity);

        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(0, stack.size());
        assertEquals(maxCapacity, stack.getMaximumCapacity());
    }

    // ==================================================================

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
    public void isEmpty() {
        StrictSingleLinkedBlockingStack<String> s = new StrictSingleLinkedBlockingStack<String>();
        assertTrue(s.isEmpty());

        s.push("foo");
        s.push("bar");
        assertFalse(s.isEmpty());
    }


    // ======================== iterator ===========================================================

    @Test
    public void iteratorWithoutItems() {
        StrictSingleLinkedBlockingStack<String> s = new StrictSingleLinkedBlockingStack<String>();
        assertFalse(s.iterator().hasNext());
    }

    @Test
    public void iteratorWithSingleItem() {
        StrictSingleLinkedBlockingStack<String> s = new StrictSingleLinkedBlockingStack<String>();
        s.push("1");

        Iterator<String> it = s.iterator();
        assertTrue(it.hasNext());
        assertEquals("1", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void iteratorWithMultipleItems() {
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
    public void iteratorRemove() {
        //todo
    }

    // ========================== peek ==================================

    @Test
    public void peek() {
        StrictSingleLinkedBlockingStack<String> s = new StrictSingleLinkedBlockingStack<String>();
        assertNull(s.peek());

        s.push("1");
        assertEquals("1", s.peek());

        s.push("2");
        assertEquals("2", s.peek());
    }

    // =========================== offer =================================

    @Test
    public void offerNullFails() throws InterruptedException {
        BlockingStack<String> stack = new StrictSingleLinkedBlockingStack<String>();

        long version = stm.getClockVersion();

        try {
            stack.offer(null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertEquals(version, stm.getClockVersion());
        assertEquals("[]", stack.toString());
    }

    @Test
    public void offerOnEmptyStack() throws InterruptedException {
        BlockingStack<String> stack = new StrictSingleLinkedBlockingStack<String>();

        long version = stm.getClockVersion();

        boolean result = stack.offer("foo");
        assertTrue(result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[foo]", stack.toString());
    }

    @Test
    public void offerOnNonEmptyStack() throws InterruptedException {
        BlockingStack<String> stack = new StrictSingleLinkedBlockingStack<String>();
        stack.push("1");

        long version = stm.getClockVersion();

        boolean result = stack.offer("2");
        assertTrue(result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[2, 1]", stack.toString());
    }

    @Test
    public void offerOnFullStack() throws InterruptedException {
        BlockingStack<String> stack = new StrictSingleLinkedBlockingStack<String>(1);
        stack.push("1");

        long version = stm.getClockVersion();

        boolean result = stack.offer("2");
        assertFalse(result);
        assertEquals(version, stm.getClockVersion());
        assertEquals("[1]", stack.toString());
    }

    // ========================== put =====================================

    @Test
    public void putNullFails() throws InterruptedException {
        BlockingStack<String> stack = new StrictSingleLinkedBlockingStack<String>();

        long version = stm.getClockVersion();

        try {
            stack.put(null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertEquals(version, stm.getClockVersion());
        assertEquals("[]", stack.toString());
    }

    @Test
    public void putOnEmptyStack() throws InterruptedException {
        BlockingStack<String> stack = new StrictSingleLinkedBlockingStack<String>();

        long version = stm.getClockVersion();

        stack.put("foo");
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[foo]", stack.toString());
    }

    @Test
    public void putOnNonEmptyStack() throws InterruptedException {
        BlockingStack<String> stack = new StrictSingleLinkedBlockingStack<String>();
        stack.push("1");

        long version = stm.getClockVersion();

        stack.put("2");
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[2, 1]", stack.toString());
    }

    @Test
    public void putOnFullStack() throws InterruptedException {
        BlockingStack<String> stack = new StrictSingleLinkedBlockingStack<String>(1);
        stack.push("1");

        long version = stm.getClockVersion();

        Transaction t = stm.startUpdateTransaction("foo");
        setThreadLocalTransaction(t);
        try {
            stack.put("2");
            fail();
        } catch (RetryError ex) {
        }


        assertEquals(version, stm.getClockVersion());
        assertEquals("[1]", stack.toString());
    }

    // ========================== push =====================================

    @Test
    public void pushNullFails() {
        BlockingStack<String> stack = new StrictSingleLinkedBlockingStack<String>();

        long version = stm.getClockVersion();

        try {
            stack.push(null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertEquals(version, stm.getClockVersion());
        assertEquals("[]", stack.toString());
    }

    @Test
    public void pushOnEmptyStack() {
        BlockingStack<String> stack = new StrictSingleLinkedBlockingStack<String>();

        long version = stm.getClockVersion();

        stack.push("foo");
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[foo]", stack.toString());
    }

    @Test
    public void pushOnNonEmptyStack() {
        BlockingStack<String> stack = new StrictSingleLinkedBlockingStack<String>();
        stack.push("1");

        long version = stm.getClockVersion();

        stack.push("2");
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[2, 1]", stack.toString());
    }

    @Test
    public void pushOnFullStack() {
        BlockingStack<String> stack = new StrictSingleLinkedBlockingStack<String>(1);
        stack.push("1");

        long version = stm.getClockVersion();
        try {
            stack.push("2");
            fail();
        } catch (IllegalStateException expected) {
        }

        assertEquals(version, stm.getClockVersion());
        assertEquals("[1]", stack.toString());
    }

    // ============================ pop =================================

    @Test
    public void popFromStackWithMultipleItems() {
        BlockingStack<String> stack = new StrictSingleLinkedBlockingStack<String>();
        stack.push("1");
        stack.push("2");

        long version = stm.getClockVersion();
        String result = stack.pop();
        assertEquals("2", result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[1]", stack.toString());
    }


    @Test
    public void popFromSingletonStack() {
        BlockingStack<String> stack = new StrictSingleLinkedBlockingStack<String>();
        stack.push("1");

        long version = stm.getClockVersion();
        String result = stack.pop();
        assertEquals("1", result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[]", stack.toString());
    }


    @Test
    public void popFromEmptyStack() {
        BlockingStack<String> stack = new StrictSingleLinkedBlockingStack<String>();

        long version = stm.getClockVersion();

        Transaction t = stm.startUpdateTransaction("foo");
        setThreadLocalTransaction(t);
        try {
            stack.pop();
            fail();
        } catch (RetryError ex) {
        }

        assertEquals(version, stm.getClockVersion());
        assertEquals("[]", stack.toString());
    }

    // ============================ poll =================================

    @Test
    public void pollFromStackWithMultipleItems() {
        BlockingStack<String> stack = new StrictSingleLinkedBlockingStack<String>();
        stack.push("1");
        stack.push("2");

        long version = stm.getClockVersion();
        String result = stack.poll();
        assertEquals("2", result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[1]", stack.toString());
    }


    @Test
    public void pollFromSingletonStack() {
        BlockingStack<String> stack = new StrictSingleLinkedBlockingStack<String>();
        stack.push("1");

        long version = stm.getClockVersion();
        String result = stack.poll();
        assertEquals("1", result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[]", stack.toString());
    }

    @Test
    public void pollFromEmptyStack() {
        BlockingStack<String> stack = new StrictSingleLinkedBlockingStack<String>();

        long version = stm.getClockVersion();

        String result = stack.poll();
        assertNull(result);
        assertEquals(version, stm.getClockVersion());
        assertEquals("[]", stack.toString());
    }

    // ============================ poll(long,TimeUnit) =================================


    // ============================= clear ==============================================

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

    // ============================ drainTo (collection)=================================

    @Test
    public void drainToEmty() {
        BlockingStack<String> s = new StrictSingleLinkedBlockingStack<String>();
        List<String> drain = new LinkedList<String>();
        int result = s.drainTo(drain);
        assertEquals(0, result);
        assertTrue(drain.isEmpty());
    }

    @Test
    public void drainToNonEmty() {
        BlockingStack<String> s = new StrictSingleLinkedBlockingStack<String>();
        s.push("1");
        s.push("2");
        s.push("3");
        List<String> drain = new LinkedList<String>();
        int result = s.drainTo(drain);
        assertEquals(3, result);
        assertEquals(asList("3", "2", "1"), drain);
    }

    // ============================ drainTo (collection, int)=================================

    // ===========================================================================


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
    public void equals() {
        //BlockingStack<String> stack = new StrictSingleLinkedBlockingStack<String>();
        //assertFalse(stack.equals(null));
        //assertTrue(stack.equals(stack));
        //assertFalse(stack.equals(asList("foo")));
        //assertEquals(stack, asList());
    }

    @Test
    public void testHash() {

    }
}
