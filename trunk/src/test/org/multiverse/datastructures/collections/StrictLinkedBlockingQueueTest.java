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
public class StrictLinkedBlockingQueueTest {
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
    public void complexPushPopScenario() throws InterruptedException {
        StrictLinkedBlockingQueue<String> queue = new StrictLinkedBlockingQueue<String>();
        queue.put("1");
        queue.put("2");
        queue.put("3");
        assertEquals("1", queue.take());
        queue.put("4");
        assertEquals("2", queue.take());
        assertEquals("3", queue.take());
        queue.put("5");
        assertEquals("4", queue.take());
        assertEquals("5", queue.take());
        assertEquals(0, queue.size());
    }

    @Test
    public void clear() throws InterruptedException {
        StrictLinkedBlockingQueue<String> queue = new StrictLinkedBlockingQueue<String>();
        queue.put("foo");
        queue.put("bar");
        queue.take();

        queue.clear();
        assertEquals(0, queue.size());

        queue.clear();
        assertEquals(0, queue.size());
    }

    @Test
    public void isEmpty() throws InterruptedException {
        StrictLinkedBlockingQueue<String> queue = new StrictLinkedBlockingQueue<String>();
        assertTrue(queue.isEmpty());

        queue.put("foo");
        queue.put("bar");

        assertFalse(queue.isEmpty());
    }

    public void testToString() {
        StrictLinkedBlockingQueue<String> queue = new StrictLinkedBlockingQueue<String>();
        assertEquals("[]", queue.toString());
    }
}
