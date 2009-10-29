package org.multiverse.datastructures.collections;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.RetryError;
import static org.multiverse.utils.ThreadLocalTransaction.setThreadLocalTransaction;

import java.util.concurrent.BlockingDeque;

public class StrictLinkedBlockingDeque_putFirstTest {

    private Stm stm;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        setThreadLocalTransaction(null);
    }

    @Test
    public void putFirstNullFails() throws InterruptedException {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();

        long version = stm.getClockVersion();

        try {
            deque.putFirst(null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertEquals(version, stm.getClockVersion());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void putFirstOnEmptyDeque() throws InterruptedException {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        long version = stm.getClockVersion();

        deque.putFirst("1");
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[1]", deque.toString());
    }

    @Test
    public void putFirstOnNonEmptyDeque() throws InterruptedException {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");
        long version = stm.getClockVersion();

        deque.putFirst("2");

        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[2, 1]", deque.toString());
    }

    @Test
    public void putFirstOnFullDeque() throws InterruptedException {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>(2);
        deque.put("1");
        deque.put("2");

        long version = stm.getClockVersion();

        Transaction t = stm.startUpdateTransaction("");
        setThreadLocalTransaction(t);

        try {
            deque.putFirst("3");
            fail();
        } catch (RetryError expected) {
        }
        t.abort();

        assertEquals(version, stm.getClockVersion());
        assertEquals("[1, 2]", deque.toString());
    }
}
