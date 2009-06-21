package org.multiverse.multiversionedstm;

import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.WriteConflictException;
import org.multiverse.multiversionedstm.manualinstrumented.ManualIntValue;

public class Transaction_CommitTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void writeConflict() {
        ManualIntValue intValue = new ManualIntValue(10);

        Transaction t1 = stm.startTransaction();
        Handle<ManualIntValue> handle = t1.attach(intValue);
        t1.commit();

        Transaction t2 = stm.startTransaction();
        ManualIntValue v2 = t2.readLazy(handle).get();

        Transaction t3 = stm.startTransaction();
        ManualIntValue v3 = t3.readLazy(handle).get();
        v3.inc();
        t3.commit();

        v2.inc();

        long globalVersion = stm.getGlobalVersion();
        long commitCount = stm.getStatistics().getTransactionCommittedCount();
        long abortedCount = stm.getStatistics().getTransactionAbortedCount();

        try {
            t2.commit();
            fail();
        } catch (WriteConflictException er) {
        }

        assertGlobalVersion(stm, globalVersion);
        assertTransactionCommittedCount(stm, commitCount);
        assertTransactionAbortedCount(stm, abortedCount + 1);
    }

    @Test
    public void freshObject() {
        ManualIntValue intValue = new ManualIntValue(10);

        long globalVersion = stm.getGlobalVersion();
        long commitCount = stm.getStatistics().getTransactionCommittedCount();
        long readonlyCount = stm.getStatistics().getTransactionReadonlyCount();

        Transaction t = stm.startTransaction();
        Handle<ManualIntValue> handle = t.attach(intValue);
        t.commit();

        assertIsCommitted(t);
        assertGlobalVersion(stm, globalVersion + 1);
        assertTransactionCommittedCount(stm, commitCount + 1);
        assertTransactionReadonlyCount(stm, readonlyCount);
        assertIntegerValue(stm, handle, 10);
    }

    @Test
    public void multipleAttachedFreshObjects() {
        ManualIntValue item1 = new ManualIntValue(1);
        ManualIntValue item2 = new ManualIntValue(2);
        ManualIntValue item3 = new ManualIntValue(3);

        long globalVersion = stm.getGlobalVersion();
        long commitCount = stm.getStatistics().getTransactionCommittedCount();
        long readonlyCount = stm.getStatistics().getTransactionReadonlyCount();

        Transaction t = stm.startTransaction();
        Handle<ManualIntValue> handle1 = t.attach(item1);
        Handle<ManualIntValue> handle2 = t.attach(item2);
        Handle<ManualIntValue> handle3 = t.attach(item3);
        t.commit();

        assertIsCommitted(t);
        assertTransactionCommittedCount(stm, commitCount + 1);
        assertTransactionReadonlyCount(stm, readonlyCount);
        assertGlobalVersion(stm, globalVersion + 1);
        assertIntegerValue(stm, handle1, 1);
        assertIntegerValue(stm, handle2, 2);
        assertIntegerValue(stm, handle3, 3);
    }

    @Test
    public void multipleDirtyObjects() {
        ManualIntValue item1 = new ManualIntValue(1);
        ManualIntValue item2 = new ManualIntValue(1);
        ManualIntValue item3 = new ManualIntValue(1);

        Transaction t = stm.startTransaction();
        Handle<ManualIntValue> handle1 = t.attach(item1);
        Handle<ManualIntValue> handle2 = t.attach(item2);
        Handle<ManualIntValue> handle3 = t.attach(item3);
        t.commit();

        long globalVersion = stm.getGlobalVersion();
        long commitCount = stm.getStatistics().getTransactionCommittedCount();

        Transaction t2 = stm.startTransaction();
        t2.read(handle1).inc();
        t2.read(handle2).inc();
        t2.read(handle3).inc();
        t2.commit();

        assertGlobalVersion(stm, globalVersion + 1);
        assertTransactionCommittedCount(stm, commitCount + 1);
        assertIntegerValue(stm, handle1, 2);
        assertIntegerValue(stm, handle2, 2);
        assertIntegerValue(stm, handle3, 2);
    }

    @Test
    public void commitOfReadonlyTransactionDoesntLeadToChanges() {
        Handle<ManualIntValue> handle = commit(stm, new ManualIntValue());

        long globalVersion = stm.getGlobalVersion();
        long commitCount = stm.getStatistics().getTransactionCommittedCount();
        long readonlyCount = stm.getStatistics().getTransactionReadonlyCount();

        Transaction t = stm.startTransaction();
        ManualIntValue intValue = t.read(handle);
        t.commit();

        assertGlobalVersion(stm, globalVersion);
        assertTransactionCommittedCount(stm, commitCount + 1);
        assertTransactionReadonlyCount(stm, readonlyCount + 1);
    }

    @Test
    public void commitSucceedsIfTransactionIsUnused() {
        Transaction t = stm.startTransaction();

        long transactionCommittedCount = stm.getStatistics().getTransactionCommittedCount();
        long globalVersion = stm.getGlobalVersion();

        t.commit();
        assertIsCommitted(t);
        assertTransactionCommittedCount(stm, transactionCommittedCount + 1);
        assertGlobalVersion(stm, globalVersion);
    }

    @Test
    public void commitIsIgnoredIfTransactionAlreadyIsCommitted() {
        Transaction t = stm.startTransaction();
        t.commit();

        long transactionCommittedCount = stm.getStatistics().getTransactionCommittedCount();
        long globalVersion = stm.getGlobalVersion();

        t.commit();
        assertIsCommitted(t);
        assertTransactionCommittedCount(stm, transactionCommittedCount);
        assertGlobalVersion(stm, globalVersion);
    }

    @Test
    public void commitFailsIfTransactionAlreadyIsAborted() {
        Transaction t = stm.startTransaction();
        t.abort();

        long transactionCommittedCount = stm.getStatistics().getTransactionCommittedCount();
        long globalVersion = stm.getGlobalVersion();

        try {
            t.commit();
            fail();
        } catch (IllegalStateException ex) {

        }
        assertIsAborted(t);
        assertTransactionCommittedCount(stm, transactionCommittedCount);
        assertGlobalVersion(stm, globalVersion);
    }
}
