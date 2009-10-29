package org.multiverse.datastructures.collections;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import org.multiverse.api.Stm;
import static org.multiverse.utils.ThreadLocalTransaction.setThreadLocalTransaction;

import java.util.concurrent.BlockingDeque;

public class StrictLinkedBlockingDeque_offerLastTest {

    private Stm stm;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        setThreadLocalTransaction(null);
    }

    @Test
    public void offerLastNullFails() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();

        long version = stm.getClockVersion();

        try {
            deque.offerLast(null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertEquals(version, stm.getClockVersion());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void offerLastOnEmptyQueue() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        long version = stm.getClockVersion();

        boolean result = deque.offerLast("1");
        assertTrue(result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[1]", deque.toString());
    }

    @Test
    public void offerLastOnNonEmptyQueue() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.offerLast("1");
        long version = stm.getClockVersion();

        boolean result = deque.offerLast("2");

        assertTrue(result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[1, 2]", deque.toString());
    }

    @Test
    public void offerLastOnFullQueue() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>(1);
        deque.offerLast("1");
        long version = stm.getClockVersion();

        boolean result = deque.offerLast("2");
        assertFalse(result);
        assertEquals(version, stm.getClockVersion());
        assertEquals("[1]", deque.toString());
    }
}
