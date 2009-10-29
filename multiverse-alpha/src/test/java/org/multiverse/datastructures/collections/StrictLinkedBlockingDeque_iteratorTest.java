package org.multiverse.datastructures.collections;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import org.multiverse.api.Stm;
import static org.multiverse.utils.ThreadLocalTransaction.setThreadLocalTransaction;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class StrictLinkedBlockingDeque_iteratorTest {

    private Stm stm;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        setThreadLocalTransaction(null);
    }

    @Test(expected = NoSuchElementException.class)
    public void emptyDeqeue() {
        StrictLinkedBlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        Iterator<String> it = deque.iterator();
        assertFalse(it.hasNext());
        it.next();
    }

    @Test
    public void emptyNonEmpty() {
        StrictLinkedBlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");
        deque.add("2");

        Iterator<String> it = deque.iterator();
        assertTrue(it.hasNext());
        assertEquals("1", it.next());

        assertTrue(it.hasNext());
        assertEquals("2", it.next());

        assertFalse(it.hasNext());
    }

    @Test
    public void removeFromIterator() {
        StrictLinkedBlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");
        deque.add("2");

        Iterator<String> it = deque.iterator();
        it.next();
        long version = stm.getClockVersion();
        it.remove();

        assertEquals(version+1, stm.getClockVersion());
        assertEquals("[2]", deque.toString());
    }

    @Test(expected = NoSuchElementException.class)
    public void removeWithoutNextCalled(){
        StrictLinkedBlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");

        Iterator<String> it = deque.iterator();
        it.remove();
    }
}
