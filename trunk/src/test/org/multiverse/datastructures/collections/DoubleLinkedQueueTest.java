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
public class DoubleLinkedQueueTest {
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
        DoubleLinkedQueue<String> queue = new DoubleLinkedQueue<String>();
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
        DoubleLinkedQueue<String> queue = new DoubleLinkedQueue<String>();
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
        DoubleLinkedQueue<String> queue = new DoubleLinkedQueue<String>();
        assertTrue(queue.isEmpty());

        queue.push("foo");
        queue.push("bar");

        assertFalse(queue.isEmpty());
    }
}
