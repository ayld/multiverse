package org.multiverse.stms.alpha;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.StmUtils.retry;
import org.multiverse.api.Transaction;
import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.NoProgressPossibleException;
import org.multiverse.stms.alpha.manualinstrumentation.IntRef;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

public class UpdateTransaction_abortAndRetryTest {

    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
        setThreadLocalTransaction(null);
    }

    public Transaction startUpdateTransaction() {
        Transaction t = stm.startUpdateTransaction();
        setThreadLocalTransaction(t);
        return t;
    }

    @Test
    public void abortAndRetryFailsIfNothingHasBeenAttached() {
        Transaction t = startUpdateTransaction();

        try {
            t.abortAndRetry();
            fail();
        } catch (NoProgressPossibleException ex) {
        }

        assertIsAborted(t);
    }

    @Test
    public void abortAndRetryFailsIfOnlyNewObjects() {
        Transaction t = startUpdateTransaction();
        IntRef intValue = new IntRef(0);

        try {
            t.abortAndRetry();
            fail();
        } catch (NoProgressPossibleException ex) {

        }

        assertIsAborted(t);
    }

    @Test
    public void abortAndRetryOnSingleObject() {
        final IntRef value = new IntRef(0);

        TestThread t = new TestThread() {
            @AtomicMethod
            public void run() {
                if (value.get() == 0) {
                    retry();
                }
            }
        };

        t.start();
        sleepMs(300);
        assertTrue(t.isAlive());

        value.set(1);
        sleepMs(300);

        joinAll(t);
    }

    @Test
    public void abortAndRetryOnMultipleObjects() {
        final IntRef value1 = new IntRef(0);
        final IntRef value2 = new IntRef(0);
        final IntRef value3 = new IntRef(0);

        TestThread t = new TestThread() {
            @AtomicMethod
            public void run() {
                if (value1.get() == 0 && value2.get() == 0 && value3.get() == 0) {
                    retry();
                }
            }
        };

        t.start();
        sleepMs(300);
        assertTrue(t.isAlive());

        value2.set(1);
        sleepMs(300);

        joinAll(t);
    }

    @Test
    public void abortAndRetryFailsIfTransactionIsCommitted() {
        Transaction t = startUpdateTransaction();
        t.commit();

        long expectedVersion = stm.getClockVersion();

        try {
            t.abortAndRetry();
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertIsCommitted(t);
        assertEquals(expectedVersion, stm.getClockVersion());
    }

    @Test
    public void abortAndRetryFailsIfTransactionIsAborted() {
        Transaction t = startUpdateTransaction();
        t.abort();

        long expectedVersion = stm.getClockVersion();

        try {
            t.abortAndRetry();
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertIsAborted(t);
        assertEquals(expectedVersion, stm.getClockVersion());
    }
}
