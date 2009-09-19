package org.multiverse.templates;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.assertIsActive;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.api.exceptions.TooManyRetriesException;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.stms.alpha.manualinstrumentation.IntRef;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.getThreadLocalTransaction;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

import java.io.IOException;

public class AtomicTemplateTest {
    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
        setThreadLocalTransaction(null);
    }

    @After
    public void tearDown() {
        setThreadLocalTransaction(null);
    }

    public Transaction startUpdateTransaction() {
        Transaction t = stm.startUpdateTransaction(null);
        setThreadLocalTransaction(t);
        return t;
    }

    @Test
    public void testEmptyTemplate() {
        long version = stm.getClockVersion();

        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                return null;
            }
        }.execute();

        assertEquals(version, stm.getClockVersion());
        assertNull(getThreadLocalTransaction());
    }

    @Test
    public void testSelfCreatedTransaction() {
        final IntRef value = new IntRef(0);

        long version = stm.getClockVersion();

        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                value.inc();
                return null;
            }
        }.execute();

        assertEquals(version + 1, stm.getClockVersion());
        assertNull(getThreadLocalTransaction());
        assertEquals(1, value.get());
    }

    @Test
    public void testLiftingOnExistingTransaction() {
        final IntRef value = new IntRef(0);

        Transaction t = startUpdateTransaction();

        long startVersion = stm.getClockVersion();
        long startedCount = stm.getProfiler().sumKey1("updatetransaction.started.count");

        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                value.inc();
                return null;
            }
        }.execute();

        assertSame(t, getThreadLocalTransaction());
        assertIsActive(t);
        assertEquals(startVersion, stm.getClockVersion());
        assertEquals(startedCount, stm.getProfiler().sumKey1("updatetransaction.started.count"));

        t.commit();
        assertEquals(startVersion + 1, stm.getClockVersion());
        assertSame(t, getThreadLocalTransaction());
        setThreadLocalTransaction(null);
        assertEquals(1, value.get());
    }

    @Test
    public void testExplicitAbort() {
        final IntRef value = new IntRef(0);

        long version = stm.getClockVersion();

        try {
            new AtomicTemplate() {
                @Override
                public Object execute(Transaction t) throws Exception {
                    value.inc();
                    t.abort();
                    return null;
                }
            }.execute();

            fail();
        } catch (DeadTransactionException ex) {

        }

        assertEquals(1, stm.getProfiler().sumKey1("updatetransaction.aborted.count"));
        assertNull(getThreadLocalTransaction());
        assertEquals(version, stm.getClockVersion());
        assertEquals(0, value.get());
    }

    @Test
    public void testExplicitCommit() {
        final IntRef value = new IntRef(0);

        long version = stm.getClockVersion();

        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                value.inc();
                t.commit();
                return null;
            }
        }.execute();

        assertEquals(version + 1, stm.getClockVersion());
        assertNull(getThreadLocalTransaction());
        assertEquals(1, value.get());
    }

    @Test
    public void testRuntimeExceptionDoesNotCommitChanges() {
        final IntRef value = new IntRef(0);

        final Exception ex = new RuntimeException();

        try {
            new AtomicTemplate() {
                @Override
                public Object execute(Transaction t) throws Exception {
                    value.inc();
                    throw ex;
                }
            }.execute();
        } catch (Exception found) {
            assertSame(ex, found);
        }

        assertEquals(0, value.get());
    }

    @Test
    public void testCheckedException() {
        final IntRef value = new IntRef(0);

        final Exception ex = new IOException();

        try {
            new AtomicTemplate() {
                @Override
                public Object execute(Transaction t) throws Exception {
                    value.inc();
                    throw ex;
                }
            }.execute();
        } catch (AtomicTemplate.InvisibleCheckedException found) {
            assertSame(ex, found.getCause());
        }

        assertEquals(0, value.get());
    }

    @Test
    public void testRecursionDoesntCallProblems() {
        long version = stm.getClockVersion();
        long startedCount = stm.getProfiler().sumKey1("updatetransaction.started.count");

        run(100);

        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(startedCount + 1, stm.getProfiler().sumKey1("updatetransaction.started.count"));
    }


    public void run(final int depth) {
        if (depth == 0) {
            new IntRef();
        } else {
            new AtomicTemplate() {
                @Override
                public Object execute(Transaction t) throws Exception {
                    run(depth - 1);
                    return null;
                }
            }.execute();
        }
    }

    // =========  retry count ===============================

    @Test
    public void tooManyRetries() {
        final IntRef ref = IntRef.createUncommitted();
        final IntHolder counter = new IntHolder();

        long version = stm.getClockVersion();

        try {
            new AtomicTemplate(stm, null, false, false, 10) {
                @Override
                public Object execute(Transaction t) throws Exception {
                    counter.value++;
                    assertEquals(counter.value, getAttemptCount());
                    ref.get();
                    return null;
                }
            }.execute();
            fail();
        } catch (TooManyRetriesException ex) {
        }

        assertEquals(version, stm.getClockVersion());
    }

    private static class IntHolder {
        int value;
    }

    // =============== readonly support ===================

    @Test
    public void readonly() {
        final IntRef ref = new IntRef(0);

        long version = stm.getClockVersion();

        try {
            new AtomicTemplate(stm, null, false, true, Integer.MAX_VALUE) {
                @Override
                public Object execute(Transaction t) throws Exception {
                    ref.inc();
                    return null;
                }
            }.execute();
        } catch (ReadonlyException ex) {
        }

        assertEquals(version, stm.getClockVersion());
        assertEquals(0, ref.get());
    }
}
