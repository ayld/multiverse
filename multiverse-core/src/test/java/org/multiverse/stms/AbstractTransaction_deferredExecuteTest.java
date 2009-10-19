package org.multiverse.stms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.DeadTransactionException;

/**
 * @author Peter Veentjer
 */
public class AbstractTransaction_deferredExecuteTest {

    @Test
    public void onTransactionAbortDeferredTasksAreNotExecuted() {
        TestTask task1 = new TestTask();
        TestTask task2 = new TestTask();

        Transaction t = new AbstractTransactionImpl();
        t.deferredExecute(task1);
        t.deferredExecute(task2);

        assertEquals(0, task1.executionCount);
        assertEquals(0, task2.executionCount);

        t.abort();

        assertEquals(0, task1.executionCount);
        assertEquals(0, task2.executionCount);
    }

    @Test
    public void deferredTasksAreExecutedWhenTransactionCommits() {
        TestTask task1 = new TestTask();
        TestTask task2 = new TestTask();

        Transaction t = new AbstractTransactionImpl();
        t.deferredExecute(task1);
        t.deferredExecute(task2);

        assertEquals(0, task1.executionCount);
        assertEquals(0, task2.executionCount);

        t.commit();

        assertEquals(1, task1.executionCount);
        assertEquals(1, task2.executionCount);
    }

    @Test
    public void deferredExecuteFailsWithNullTask() {
        Transaction t = new AbstractTransactionImpl();

        try {
            t.deferredExecute(null);
            fail();
        } catch (NullPointerException ex) {
        }

        assertIsActive(t);
    }

    @Test
    public void deferredExecuteFailsOnCommittedTransaction() {
        Transaction t = new AbstractTransactionImpl();
        t.commit();

        TestTask task = new TestTask();
        try {
            t.deferredExecute(task);
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertIsCommitted(t);
        assertEquals(0, task.executionCount);
    }

    @Test
    public void deferredExecuteFailsOnAbortedTransaction() {
        Transaction t = new AbstractTransactionImpl();
        t.abort();

        TestTask task = new TestTask();
        try {
            t.deferredExecute(task);
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
