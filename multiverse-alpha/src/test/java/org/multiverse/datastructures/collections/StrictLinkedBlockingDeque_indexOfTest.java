package org.multiverse.datastructures.collections;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import org.multiverse.api.Stm;
import static org.multiverse.api.ThreadLocalTransaction.setThreadLocalTransaction;

import java.util.List;

public class StrictLinkedBlockingDeque_indexOfTest {

    private Stm stm;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        setThreadLocalTransaction(null);
    }

    @Test
    public void indexOfNullFails() {
        List<String> deque = new StrictLinkedBlockingDeque<String>();

        long version = stm.getClockVersion();
        try {
            deque.indexOf(null);
            fail();
        } catch (NullPointerException ex) {
        }

        assertEquals(version, stm.getClockVersion());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void indexOf() {
        List<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");
        deque.add("2");
        deque.add("3");
        deque.add("1");

        long version = stm.getClockVersion();
        assertEquals(0, deque.indexOf("1"));
        assertEquals(1, deque.indexOf("2"));
        assertEquals(2, deque.indexOf("3"));
        assertEquals(-1, deque.indexOf("a"));
        assertEquals(version, stm.getClockVersion());
        assertEquals("[1, 2, 3, 1]", deque.toString());
    }
}
