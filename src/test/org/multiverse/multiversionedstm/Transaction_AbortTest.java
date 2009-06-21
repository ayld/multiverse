package org.multiverse.multiversionedstm;

import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.manualinstrumented.ManualIntValue;

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
        ManualIntValue intValue = new ManualIntValue();
        Handle<ManualIntValue> handle = t.attach(intValue);
        t.abort();

        assertIsAborted(t);
        assertNoCommits(stm, handle);
        assertGlobalVersion(stm, globalVersion);
    }

    @Test
    public void abortDoesNotCommitChangesOnRematerializedObject() {
        int oldValue = 0;
        Handle<ManualIntValue> handle = commit(stm, new ManualIntValue(oldValue));

        long globalVersion = stm.getGlobalVersion();
        Transaction t = stm.startTransaction();
        ManualIntValue intValue = t.read(handle);
        intValue.inc();
        t.abort();

        assertIsAborted(t);
        assertIntegerValue(stm, handle, oldValue);
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
