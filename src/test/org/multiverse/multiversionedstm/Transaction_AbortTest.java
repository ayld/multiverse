package org.multiverse.multiversionedstm;

import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Originator;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.examples.IntegerValue;

public class Transaction_AbortTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void abortUnusedTransaction() {
        long globalVersion = stm.getGlobalVersion();
        Transaction t = stm.startTransaction();
        t.abort();

        assertIsAborted(t);
        assertGlobalVersion(stm, globalVersion);
    }

    @Test
    public void abortDoesNotCommitChangesFreshAttachedObject() {
        long globalVersion = stm.getGlobalVersion();

        Transaction t = stm.startTransaction();
        IntegerValue integerValue = new IntegerValue();
        Originator<IntegerValue> originator = t.attach(integerValue);
        t.abort();

        assertIsAborted(t);
        assertNoCommits(stm, originator);
        assertGlobalVersion(stm, globalVersion);
    }

    @Test
    public void abortDoesNotCommitChangesOnRematerializedObject() {
        int oldValue = 0;
        Originator<IntegerValue> originator = commit(stm, new IntegerValue(oldValue));

        long globalVersion = stm.getGlobalVersion();
        Transaction t = stm.startTransaction();
        IntegerValue integerValue = t.read(originator);
        integerValue.inc();
        t.abort();

        assertIsAborted(t);
        assertIntegerValue(stm, originator, oldValue);
        assertGlobalVersion(stm, globalVersion);
    }

    @Test
    public void multipleAbortsAreIgnored() {
        Transaction t = stm.startTransaction();
        t.abort();

        long globalVersion = stm.getGlobalVersion();

        t.abort();
        assertIsAborted(t);
        assertGlobalVersion(stm, globalVersion);
    }

    @Test
    public void abortFailsIfTransactionAlreadyIsCommitted() {
        Transaction t = stm.startTransaction();
        t.commit();

        long globalVersion = stm.getGlobalVersion();

        try {
            t.abort();
            fail();
        } catch (IllegalStateException ex) {

        }
        assertIsCommitted(t);
        assertGlobalVersion(stm, globalVersion);
    }
}
