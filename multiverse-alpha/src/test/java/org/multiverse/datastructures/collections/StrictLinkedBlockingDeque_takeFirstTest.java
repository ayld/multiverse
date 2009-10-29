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

public class StrictLinkedBlockingDeque_takeFirstTest {


    private Stm stm;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        setThreadLocalTransaction(null);
    }

    @Test
    public void takeFirstFromEmptyDeque() throws InterruptedException {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();

        long version = stm.getClockVersion();
        Transaction t = stm.startUpdateTransaction("");
        setThreadLocalTransaction(t);

        try {
            deque.take();
            fail();
        } catch (RetryError expected) {
        }
        t.abort();
        assertEquals(version, stm.getClockVersion());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void takeFirstFromDequeWithSingleItem() throws InterruptedException {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");

        long version = stm.getClockVersion();
        String result = deque.takeFirst();
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("1", result);
        assertEquals(0, deque.size());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void takeFirstFromDequeWithMultipleItems() throws InterruptedException {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");
        deque.add("2");

        long version = stm.getClockVersion();
        String result = deque.takeFirst();
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("1", result);
        assertEquals(1, deque.size());
        assertEquals("[2]", deque.toString());
    }

}
