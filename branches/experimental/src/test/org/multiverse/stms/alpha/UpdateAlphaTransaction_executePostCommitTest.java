package org.multiverse.stms.alpha;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

/**
 * @author Peter Veentjer
 */
public class UpdateAlphaTransaction_executePostCommitTest {

    private Stm stm;

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
    public void onTransactionAbortPostCommitTasksAreNotExecuted() {
        TestTask task1 = new TestTask();
        TestTask task2 = new TestTask();

        Transaction t = stm.startUpdateTransaction(null);
        t.executePostCommit(task1);
        t.executePostCommit(task2);

        assertEquals(0, task1.executionCount);
        assertEquals(0, task2.executionCount);

        t.abort();

        assertEquals(0, task1.executionCount);
        assertEquals(0, task2.executionCount);
    }

    @Test
    public void executePostCommit() {
        TestTask task1 = new TestTask();
        TestTask task2 = new TestTask();

        Transaction t = stm.startUpdateTransaction(null);
        t.executePostCommit(task1);
        t.executePostCommit(task2);

        assertEquals(0, task1.executionCount);
        assertEquals(0, task2.executionCount);

        t.commit();

        assertEquals(1, task1.executionCount);
        assertEquals(1, task2.executionCount);
    }

    @Test
    public void executePostCommitFailsWithNullArgument() {
        Transaction t = stm.startUpdateTransaction(null);

        try {
            t.executePostCommit(null);
            fail();
        } catch (NullPointerException ex) {
        }

        assertIsActive(t);
    }

    @Test
    public void executePostCommitFailsOnCommittedTransaction() {
        Transaction t = stm.startUpdateTransaction(null);
        t.commit();

        TestTask task = new TestTask();
        try {
            t.executePostCommit(task);
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertIsCommitted(t);
        assertEquals(0, task.executionCount);
    }

    @Test
    public void executePostCommitFailsOnAbortedTransaction() {
        Transaction t = stm.startUpdateTransaction(null);
        t.abort();

        TestTask task = new TestTask();
        try {
            t.executePostCommit(task);
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertIsAborted(t);
        assertEquals(0, task.executionCount);
    }

    static class TestTask implements Runnable {
        int executionCount;

        @Override
        public void run() {
            executionCount++;
        }
    }
}
