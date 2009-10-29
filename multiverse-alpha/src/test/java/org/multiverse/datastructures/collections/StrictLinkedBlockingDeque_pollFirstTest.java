package org.multiverse.datastructures.collections;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import org.multiverse.api.Stm;
import static org.multiverse.utils.ThreadLocalTransaction.setThreadLocalTransaction;

import java.util.concurrent.BlockingDeque;

public class StrictLinkedBlockingDeque_pollFirstTest {

     private Stm stm;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        setThreadLocalTransaction(null);
    }

      @Test
    public void pollFirstFromEmptyDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();

        long version = stm.getClockVersion();
        String result = deque.pollFirst();
        assertEquals(version, stm.getClockVersion());
        assertNull(result);
        assertEquals(0, deque.size());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void pollFirstFromDequeWithSingleItem() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");

        long version = stm.getClockVersion();
        String result = deque.pollFirst();
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("1", result);
        assertEquals(0, deque.size());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void pollFirstFromDequeWithMultipleItems() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");
        deque.add("2");

        long version = stm.getClockVersion();
        String result = deque.pollFirst();
        assertEquals("1", result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(1, deque.size());
        assertEquals("[2]", deque.toString());
    }
}
