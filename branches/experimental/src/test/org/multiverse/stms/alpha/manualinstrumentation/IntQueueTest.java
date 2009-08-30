package org.multiverse.stms.alpha.manualinstrumentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

public class IntQueueTest {
    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
        setThreadLocalTransaction(null);
    }

    @Test
    public void test() {
        IntQueue queue = new IntQueue();

        queue.push(1);

        queue.push(2);

        assertEquals(1, queue.pop());
        assertEquals(2, queue.pop());
        assertTrue(queue.isEmpty());
    }
}
