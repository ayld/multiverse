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
public class AbstractTransaction_compensatingExecuteTest {

    @Test
    public void compensatingExecuteFailsWithNullTask() {
        Transaction t = new AbstractTransactionImpl();

        try {
            t.compensatingExecute(null);
            fail();
        } catch (NullPointerException ex) {
        }

        assertIsActive(t);
    }

    @Test
    public void compensatingTaskIsNotExecutedWhenTransactionIsCommitted() {
        Transaction t = new AbstractTransactionImpl();

        CountingTask task = new CountingTask();
        t.compensatingExecute(task);
        t.commit();

        assertIsCommitted(t);
        assertEquals(0, task.executionCount);
    }

    @Test
    public void compensatingTaskIsNotExecutedWhenTransactionIsStillActive() {
        Transaction t = new AbstractTransactionImpl();

        CountingTask task = new CountingTask();
        t.compensatingExecute(task);
        assertEquals(0, task.executionCount);
        assertIsActive(t);
    }

    @Test
    public void compensatingTaskIsExecutedWhenTransactionIsAborted() {
        Transaction t = new AbstractTransactionImpl();

        CountingTask task = new CountingTask();
        t.compensatingExecute(task);
        t.abort();

        assertEquals(1, task.executionCount);
    }

    @Test
    public void multipleCompensatingTasks() {
        Transaction t = new AbstractTransactionImpl();

        CountingTask task1 = new CountingTask();
        CountingTask task2 = new CountingTask();
        CountingTask task3 = new CountingTask();
        t.compensatingExecute(task1);
        t.compensatingExecute(task2);
        t.compensatingExecute(task3);
        t.abort();

        assertEquals(1, task1.executionCount);
        assertEquals(1, task2.executionCount);
        assertEquals(1, task3.executionCount);
    }

    @Test
    public void compensatingExecuteFailsOnCommittedTransaction() {
        Transaction t = new AbstractTransactionImpl();
        t.commit();

        CountingTask task = new CountingTask();

        try {
            t.compensatingExecute(task);
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertIsCommitted(t);
        assertEquals(0, task.executionCount);
    }

    @Test
    public void compensatingExecuteFailsOnAbortedTransaction() {
        Transaction t = new AbstractTransactionImpl();
        t.abort();

        CountingTask task = new CountingTask();

        try {
            t.compensatingExecute(task);
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertIsAborted(t);
        assertEquals(0, task.executionCount);
    }

    static class CountingTask implements Runnable {
        int executionCount;

        @Override
        public void run() {
            executionCount++;
        }
    }
}
