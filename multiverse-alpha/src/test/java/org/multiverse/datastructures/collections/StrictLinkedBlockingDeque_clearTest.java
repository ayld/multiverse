package org.multiverse.datastructures.collections;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import org.multiverse.api.Stm;
import static org.multiverse.utils.ThreadLocalTransaction.setThreadLocalTransaction;

public class StrictLinkedBlockingDeque_clearTest {

    private Stm stm;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        setThreadLocalTransaction(null);
    }

    @Test
    public void clearEmptyDeque() {
        StrictLinkedBlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        long version = stm.getClockVersion();

        deque.clear();
        assertEquals(version, stm.getClockVersion());
        assertTrue(deque.isEmpty());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void clearNonEmptyDeque() {
        StrictLinkedBlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");
        deque.add("2");
        deque.add("3");

        long version = stm.getClockVersion();
        deque.clear();
        assertEquals(version + 1, stm.getClockVersion());
        assertTrue(deque.isEmpty());
        assertEquals("[]", deque.toString());
    }
}
