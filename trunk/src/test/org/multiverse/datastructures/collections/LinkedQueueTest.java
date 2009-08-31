package org.multiverse.datastructures.collections;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.utils.GlobalStmInstance;

/**
 * @author Peter Veentjer
 */
public class LinkedQueueTest {
    private Stm stm;

    @Before
    public void setUp() {
        stm = GlobalStmInstance.get();
    }

    @After
    public void tearDown() {
        //assertNoInstrumentationProblems();
    }

    @Test
    public void complexPushPopScenario() {
        LinkedQueue<String> queue = new LinkedQueue<String>();
        queue.push("1");
        queue.push("2");
        queue.push("3");
        assertEquals("1", queue.take());
        queue.push("4");
        assertEquals("2", queue.take());
        assertEquals("3", queue.take());
        queue.push("5");
        assertEquals("4", queue.take());
        assertEquals("5", queue.take());
        assertEquals(0, queue.size());
    }

    @Test
    public void clear() {
        LinkedQueue<String> queue = new LinkedQueue<String>();
        queue.push("foo");
        queue.push("bar");
        queue.take();

        queue.clear();
        assertEquals(0, queue.size());

        queue.clear();
        assertEquals(0, queue.size());
    }

    @Test
    public void isEmpty() {
        LinkedQueue<String> queue = new LinkedQueue<String>();
        assertTrue(queue.isEmpty());

        queue.push("foo");
        queue.push("bar");

        assertFalse(queue.isEmpty());
    }

    public void testToString() {
        LinkedQueue<String> queue = new LinkedQueue<String>();
        assertEquals("[]", queue.toString());
    }
}
