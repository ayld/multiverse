package org.multiverse.datastructures.collections;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import org.multiverse.api.Stm;
import static org.multiverse.api.ThreadLocalTransaction.setThreadLocalTransaction;

import java.util.NoSuchElementException;
import java.util.concurrent.BlockingDeque;

public class StrictLinkedBlockingDeque_removeLastTest {

    private Stm stm;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        setThreadLocalTransaction(null);
    }

    @Test
    public void removeLastFromEmptyDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();

        long version = stm.getClockVersion();
        try {
            deque.removeLast();
            fail();
        } catch (NoSuchElementException expected) {
        }
        assertEquals(0, deque.size());
        assertEquals(version , stm.getClockVersion());
        assertEquals("[]", deque.toString());        
    }

    @Test
    public void removeLastFromDequeWithSingleItem() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");

        long version = stm.getClockVersion();
        String result = deque.removeLast();
        assertEquals("1", result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(0, deque.size());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void removeLastFromDequeWithMultipleItems() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");
        deque.add("2");

        long version = stm.getClockVersion();
        String result = deque.removeLast();
        assertEquals("2", result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(1, deque.size());
        assertEquals("[1]", deque.toString());
    }

}
