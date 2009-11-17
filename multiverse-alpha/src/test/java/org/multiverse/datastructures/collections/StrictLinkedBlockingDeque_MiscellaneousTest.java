package org.multiverse.datastructures.collections;

import static junit.framework.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import static org.multiverse.api.ThreadLocalTransaction.setThreadLocalTransaction;

public class StrictLinkedBlockingDeque_MiscellaneousTest {
    private Stm stm;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        setThreadLocalTransaction(null);
    }

    // ================== roll back ==========================

    @Test
    public void rollbackPuts() throws InterruptedException {
        StrictLinkedBlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.put("1");
        long version = stm.getClockVersion();

        Transaction t = stm.startUpdateTransaction("rollback");
        setThreadLocalTransaction(t);
        deque.put("2");
        deque.put("3");
        t.abort();

        assertEquals(version, stm.getClockVersion());
        assertEquals("[1]", deque.toString());
    }

    @Test
    public void rollbackTakes() throws InterruptedException {
        StrictLinkedBlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.put("1");
        deque.put("2");
        deque.put("3");
        long version = stm.getClockVersion();

        Transaction t = stm.startUpdateTransaction("rollback");
        setThreadLocalTransaction(t);
        deque.take();
        deque.take();
        t.abort();

        assertEquals(version, stm.getClockVersion());
        assertEquals("[1, 2, 3]", deque.toString());
    }

    // ========================================


    @Test
    public void testToString() {
        StrictLinkedBlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        assertEquals("[]", deque.toString());

        deque.add("1");
        assertEquals("[1]", deque.toString());

        deque.add("2");
        deque.add("3");
        assertEquals("[1, 2, 3]", deque.toString());
    }
}
