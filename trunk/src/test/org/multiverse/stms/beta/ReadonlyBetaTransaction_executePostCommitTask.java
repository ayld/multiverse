package org.multiverse.stms.beta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.utils.GlobalStmInstance;

public class ReadonlyBetaTransaction_executePostCommitTask {

    private BetaStm stm;

    @Before
    public void setUp() {
        stm = new BetaStm();
        GlobalStmInstance.set(stm);
    }

    private BetaTransaction startTransaction() {
        return (BetaTransaction) stm.startReadOnlyTransaction(null);
    }

    @Test
    public void executePostCommitFailsWithNullArgument() {
        BetaTransaction t = startTransaction();
        try {
            t.executePostCommit(null);
            fail();
        } catch (NullPointerException ex) {
        }

        assertIsActive(t);
    }

    @Test
    public void executePostCommit() {
        Task task = new Task();
        BetaTransaction t = startTransaction();
        t.executePostCommit(task);
        assertEquals(0, task.executionCount);
        t.commit();

        assertEquals(1, task.executionCount);
    }

    @Test
    public void failingTaskDoesntCauseTransactionProblems() {
        Runnable failingTask = new FailingTask();

        long version = stm.getClockVersion();

        BetaTransaction t = startTransaction();
        t.executePostCommit(failingTask);

        try {
            t.commit();
            fail();
        } catch (FailException ex) {
        }

        assertIsCommitted(t);
        assertEquals(version, stm.getClockVersion());
    }

    @Test
    public void executePostCommitMultipleTasks() {
        Task task1 = new Task();
        Task task2 = new Task();
        BetaTransaction t = startTransaction();
        t.executePostCommit(task1);
        t.executePostCommit(task2);
        t.commit();

        assertEquals(1, task1.executionCount);
        assertEquals(1, task2.executionCount);
    }

    @Test
    public void executePostCommitIsExecutedEvenWithUnusedTransaction() {
        BetaTransaction t = startTransaction();
        Task task = new Task();
        t.executePostCommit(task);
        t.commit();

        assertEquals(1, task.executionCount);
    }

    @Test
    public void abortDoesNotExecutePostCommitTasks() {
        Task task1 = new Task();
        Task task2 = new Task();

        BetaTransaction t = startTransaction();
        t.executePostCommit(task1);
        t.executePostCommit(task2);
        t.abort();

        assertIsAborted(t);
        assertEquals(0, task1.executionCount);
        assertEquals(0, task2.executionCount);
    }

    @Test
    public void executePostCommitFailOnAbortedTransaction() {
        BetaTransaction t = startTransaction();
        t.abort();

        Task task = new Task();
        try {
            t.executePostCommit(task);
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertEquals(0, task.executionCount);
    }

    @Test
    public void executePostCommitFailsOnCommittedTransaction() {
        BetaTransaction t = startTransaction();
        t.commit();

        Task task = new Task();
        try {
            t.executePostCommit(task);
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertEquals(0, task.executionCount);
    }

    static class Task implements Runnable {
        int executionCount;

        @Override
        public void run() {
            executionCount++;
        }
    }

    static class FailingTask implements Runnable {
        @Override
        public void run() {
            throw new FailException();
        }
    }

    static class FailException extends RuntimeException {
    }
}
