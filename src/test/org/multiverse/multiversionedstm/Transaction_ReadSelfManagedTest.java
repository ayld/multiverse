package org.multiverse.multiversionedstm;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestUtils;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.NoProgressPossibleException;
import org.multiverse.multiversionedstm.manualinstrumented.ManualIntValue;
import org.multiverse.multiversionedstm.manualinstrumented.ManualPair;

public class Transaction_ReadSelfManagedTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @After
    public void tearDown() {
        System.out.println(stm.getStatistics());
    }

    @Test
    public void readSelfManagedNull() {
        Transaction t = stm.startTransaction();
        Object result = t.readSelfManaged(null);
        assertNull(result);
        assertIsActive(t);
    }

    @Test
    public void repeatedReadSelfManagedDoesNotReturnSameValue() {
        Handle<ManualIntValue> handle = commit(stm, new ManualIntValue());

        long materializedCount = stm.getStatistics().getMaterializedCount();

        Transaction t = stm.startTransaction();
        ManualIntValue v1 = t.readSelfManaged(handle);
        ManualIntValue v2 = t.readSelfManaged(handle);

        assertNotNull(v1);
        assertNotNull(v2);
        assertFalse(v1 == v2);
        assertEquals(v1, v2);
        assertIsActive(t);
        assertMaterializedCount(stm, materializedCount + 2);
    }

    @Test
    public void readSelfManagedIgnoresPreviousReadObject() {
        Handle<ManualIntValue> handle = commit(stm, new ManualIntValue());

        Transaction t = stm.startTransaction();
        ManualIntValue v1 = t.read(handle);
        ManualIntValue v2 = t.readSelfManaged(handle);

        assertNotNull(v1);
        assertNotNull(v2);
        assertFalse(v1 == v2);
        assertEquals(v1, v2);
        assertEquals(v1.getHandle(), v2.getHandle());
    }

    @Test
    public void readSelfManagedIgnoresAttachedObject() {
        Handle<ManualIntValue> handle = commit(stm, new ManualIntValue());

        Transaction t = stm.startTransaction();
        ManualIntValue v1 = t.read(handle);
        t.attach(v1);
        ManualIntValue v2 = t.readSelfManaged(handle);

        assertNotNull(v1);
        assertNotNull(v2);
        assertFalse(v1 == v2);
        assertEquals(v1, v2);
        assertEquals(v1.getHandle(), v2.getHandle());
    }

    @Test
    public void changeOnReadSelfManagedWontBeCommittedIfThereIsNoReferenceToIt() {
        ManualIntValue original = new ManualIntValue(29);
        Handle<ManualIntValue> handle = commit(stm, original);

        long writes = stm.getStatistics().getWriteCount();

        Transaction t = stm.startTransaction();
        ManualIntValue changed = t.readSelfManaged(handle);
        changed.inc();
        t.commit();

        TestUtils.assertIntegerValue(stm, handle, 29);
        assertWriteCount(stm, writes);
    }

    @Test
    public void changeOnReadSelfManagedWillBeCommittedIfThereIsSomeReferenceToIt() {
        ManualIntValue original = new ManualIntValue(29);

        Handle<ManualIntValue> handle = commit(stm, original);
        long writeCount = stm.getStatistics().getWriteCount();

        Transaction t = stm.startTransaction();
        ManualIntValue changed = t.readSelfManaged(handle);
        ManualPair<ManualIntValue, Object> pair = new ManualPair<ManualIntValue, Object>(changed, null);
        t.attach(pair);
        changed.inc();
        t.commit();

        assertIntegerValue(stm, handle, 30);
        assertWriteCount(stm, writeCount + 2);
    }

    @Test
    public void readSelfManagedNotUsedInListeningIfThereIsNoOtherReferenceToIt() throws InterruptedException {
        Handle<ManualIntValue> handle = commit(stm, new ManualIntValue());

        Transaction t = stm.startTransaction();
        ManualIntValue found = t.readSelfManaged(handle);

        try {
            t.abortAndRetry();
            fail();
        } catch (NoProgressPossibleException ex) {
        }
    }

    // ================= the other states ====================

    @Test
    public void readSelfManagedFailsIfTransactionAborted() {
        Handle<ManualIntValue> handle = commit(stm, new ManualIntValue());

        Transaction t = stm.startTransaction();
        t.abort();

        try {
            t.read(handle);
            fail();
        } catch (IllegalStateException ex) {

        }
        assertIsAborted(t);
    }

    @Test
    public void readSelfManagedFailsIfTransactionCommitted() {
        Handle<ManualIntValue> handle = commit(stm, new ManualIntValue());

        Transaction t = stm.startTransaction();
        t.commit();

        try {
            t.read(handle);
            fail();
        } catch (IllegalStateException ex) {

        }
        assertIsCommitted(t);
    }
}
