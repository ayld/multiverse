package org.multiverse.stms.alpha.instrumentation.integrationtest;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.alpha.AlphaAtomicObject;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.utils.GlobalStmInstance;

/**
 * @author Peter Veentjer
 */
public class QueueTest {

    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
    }

    @After
    public void tearDown() {
        //assertNoInstrumentationProblems();
    }

    @Test
    public void testIsNotTransformedToAlphaAtomicObject() {
        Queue queue = new Queue();

        assertFalse(((Object) queue) instanceof AlphaAtomicObject);
    }

    @Test
    public void testConstruction() {
        long version = stm.getClockVersion();
        Queue queue = new Queue(100);

        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
    }

    @Test
    public void complexScenario() {
        Queue<String> queue = new Queue<String>(100);
        queue.push("1");
        queue.push("2");

        assertEquals("1", queue.take());

        queue.push("3");

        assertEquals("2", queue.take());
        assertEquals("3", queue.take());
    }
}
