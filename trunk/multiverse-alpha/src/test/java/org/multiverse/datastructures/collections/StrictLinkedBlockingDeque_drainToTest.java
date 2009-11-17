package org.multiverse.datastructures.collections;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.multiverse.TestUtils.testIncomplete;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import org.multiverse.api.Stm;
import static org.multiverse.api.ThreadLocalTransaction.setThreadLocalTransaction;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.BlockingDeque;

public class StrictLinkedBlockingDeque_drainToTest {

    private Stm stm;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        setThreadLocalTransaction(null);
    }

    @Test
    @Ignore
    public void drainToWithEmptyDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();

        Collection<String> c = new LinkedList<String>();
        long version = stm.getClockVersion();
        int result = deque.drainTo(c);
        assertEquals(0, result);

        assertEquals(version, stm.getClockVersion());
        assertTrue(c.isEmpty());
        assertEquals(0, deque.size());
        testIncomplete();
    }

    @Ignore
    @Test
    public void drainToWithNonEmptyDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");
        deque.add("2");
        deque.add("3");

        Collection<String> c = new LinkedList<String>();
        long version = stm.getClockVersion();
        int result = deque.drainTo(c);
        assertEquals(3, result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[1, 2, 3]", c.toString());
        assertEquals(0, deque.size());
        testIncomplete();
    }
}
