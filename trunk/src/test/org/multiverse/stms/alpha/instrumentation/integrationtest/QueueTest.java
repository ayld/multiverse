package org.multiverse.stms.alpha.instrumentation.integrationtest;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.stms.alpha.instrumentation.asm.MetadataService;
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
    public void testConstruction() {
        MetadataService s = MetadataService.INSTANCE;
        Queue queue = new Queue(100);
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
