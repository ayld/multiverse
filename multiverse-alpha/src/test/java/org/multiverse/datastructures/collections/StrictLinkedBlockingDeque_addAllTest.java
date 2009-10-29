package org.multiverse.datastructures.collections;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.testIncomplete;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import org.multiverse.api.Stm;
import static org.multiverse.utils.ThreadLocalTransaction.clearThreadLocalTransaction;

import static java.util.Arrays.asList;
import java.util.LinkedList;
import java.util.List;

public class StrictLinkedBlockingDeque_addAllTest {

    private Stm stm;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTransaction();
    }

    @Test
    public void addAllWithNullCollectionFails() {
        StrictLinkedBlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");
        deque.add("2");

        long version = stm.getClockVersion();
        try {
            deque.addAll(null);
            fail();
        } catch (NullPointerException ignore) {
        }

        assertEquals(version, stm.getClockVersion());
        assertEquals("[1, 2]", deque.toString());
    }

    @Test
    public void test() {
        List<String> l = asList("1", "2", "3");

        StrictLinkedBlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("a");
        deque.add("b");

        long version = stm.getClockVersion();

        boolean result = deque.addAll(l);
        assertTrue(result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[a, b, 1, 2, 3]", deque.toString());
    }

    @Test
    public void addEmptyList() {
        StrictLinkedBlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("a");
        deque.add("b");

        long version = stm.getClockVersion();

        boolean result = deque.addAll(new LinkedList<String>());
        assertFalse(result);
        assertEquals(version, stm.getClockVersion());
        assertEquals("[a, b]", deque.toString());
    }

    @Test
    public void addToEmptyDeque() {
        List<String> l = asList("1", "2", "3");

        StrictLinkedBlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();

        long version = stm.getClockVersion();

        boolean result = deque.addAll(l);
        assertTrue(result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[1, 2, 3]", deque.toString());
    }

    @Test
    public void testAddAllWithIndex(){
        testIncomplete();
    }
}
