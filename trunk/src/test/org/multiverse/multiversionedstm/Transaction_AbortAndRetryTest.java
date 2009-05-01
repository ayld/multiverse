package org.multiverse.multiversionedstm;

import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Originator;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.NoProgressPossibleException;
import org.multiverse.multiversionedstm.examples.IntegerValue;
import org.multiverse.multiversionedstm.examples.Queue;
import org.multiverse.multiversionedstm.examples.Stack;

public class Transaction_AbortAndRetryTest {

    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }


    @Test
    public void abortAndRetryWithSingleRead() throws InterruptedException {
        final Originator<Queue<IntegerValue>> originator = commit(stm, new Queue<IntegerValue>());

        TestThread waiter = new TestThread() {
            public void run() {
                Transaction t = stm.startTransaction();
                t.read(originator);
                try {
                    t.abortAndRetry();
                } catch (InterruptedException e) {
                    fail();
                }
            }
        };

        waiter.start();
        sleep(1000);

        TestThread thread = new TestThread() {
            public void run() {
                Transaction t = stm.startTransaction();
                Queue<IntegerValue> stack = t.read(originator);
                stack.push(new IntegerValue());
                t.commit();
            }
        };

        thread.start();
    }

    @Test
    public void abortAndRetryWithMultipleReads() throws InterruptedException {
        //fail();
    }

    //@Test
    public void abortAndRetryFailsOnTransactionWithFreshAttach() throws InterruptedException {
        Transaction t = stm.startTransaction();
        Originator<Stack> originator = t.attach(new Stack());
        Stack stack = t.read(originator);

        long globalVersion = stm.getGlobalVersion();
        long abortedCount = stm.getStatistics().getTransactionAbortedCount();
        long retryCount = stm.getStatistics().getTransactionRetriedCount();

        try {
            t.abortAndRetry();
            fail();
        } catch (NoProgressPossibleException e) {
        }


        assertIsAborted(t);
        assertGlobalVersion(stm, globalVersion);
        assertTransactionAbortedCount(stm, abortedCount + 1);
        assertTransactionRetriedCount(stm, retryCount);
    }

    @Test
    public void abortAndRetryFailsOnTransactionWithoutReads() throws InterruptedException {
        Transaction t = stm.startTransaction();

        long globalVersion = stm.getGlobalVersion();
        long abortedCount = stm.getStatistics().getTransactionAbortedCount();
        long retryCount = stm.getStatistics().getTransactionRetriedCount();

        try {
            t.abortAndRetry();
            fail();
        } catch (NoProgressPossibleException e) {

        }

        assertIsAborted(t);
        assertGlobalVersion(stm, globalVersion);
        assertTransactionAbortedCount(stm, abortedCount + 1);
        assertTransactionRetriedCount(stm, retryCount);
    }

    @Test
    public void abortAndRetryFailsIfTransactionAlreadyIsCommitted() throws InterruptedException {
        Transaction t = stm.startTransaction();
        t.commit();

        long globalVersion = stm.getGlobalVersion();
        long abortedCount = stm.getStatistics().getTransactionAbortedCount();
        long retryCount = stm.getStatistics().getTransactionRetriedCount();

        try {
            t.abortAndRetry();
            fail();
        } catch (IllegalStateException ex) {
        }

        assertIsCommitted(t);
        assertGlobalVersion(stm, globalVersion);
        assertTransactionAbortedCount(stm, abortedCount);
        assertTransactionRetriedCount(stm, retryCount);
    }

    @Test
    public void abortAndRetryFailsIfTransactionAlreadyIsAborted() throws InterruptedException {
        Transaction t = stm.startTransaction();
        t.abort();

        long globalVersion = stm.getGlobalVersion();
        long abortedCount = stm.getStatistics().getTransactionAbortedCount();
        long retryCount = stm.getStatistics().getTransactionRetriedCount();

        try {
            t.abortAndRetry();
            fail();
        } catch (IllegalStateException ex) {
        }

        assertIsAborted(t);
        assertGlobalVersion(stm, globalVersion);
        assertTransactionAbortedCount(stm, abortedCount);
        assertTransactionRetriedCount(stm, retryCount);
    }
}
