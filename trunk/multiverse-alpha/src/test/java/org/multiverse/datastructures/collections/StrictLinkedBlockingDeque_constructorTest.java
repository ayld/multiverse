package org.multiverse.datastructures.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import org.multiverse.api.Stm;
import static org.multiverse.utils.ThreadLocalTransaction.clearThreadLocalTransaction;

public class StrictLinkedBlockingDeque_constructorTest {
    private Stm stm;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTransaction();
    }

    @Test
    public void testNoArgConstructor() {
        long version = stm.getClockVersion();
        StrictLinkedBlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();

        assertEquals(version+1, stm.getClockVersion());
        assertEquals(Integer.MAX_VALUE, deque.getMaxCapacity());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void constructorWithNegativeMaxCapacity() {
        long version = stm.getClockVersion();

        try {
            new StrictLinkedBlockingDeque(-1);
            fail();
        } catch (IllegalArgumentException ignore) {

        }

        assertEquals(version, stm.getClockVersion());
    }

    @Test
    public void constructorWithMaxCapacity() {
        long version = stm.getClockVersion();

        StrictLinkedBlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>(10);

        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(10, deque.getMaxCapacity());
        assertEquals("[]", deque.toString());
    }
}

