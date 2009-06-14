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
import org.multiverse.multiversionedstm.examples.ExampleIntValue;
import org.multiverse.multiversionedstm.examples.ExamplePair;

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
        Handle<ExampleIntValue> handle = commit(stm, new ExampleIntValue());

        long materializedCount = stm.getStatistics().getMaterializedCount();

        Transaction t = stm.startTransaction();
        ExampleIntValue v1 = t.readSelfManaged(handle);
        ExampleIntValue v2 = t.readSelfManaged(handle);

        assertNotNull(v1);
        assertNotNull(v2);
        assertFalse(v1 == v2);
        assertEquals(v1, v2);
        assertIsActive(t);
        assertMaterializedCount(stm, materializedCount + 2);
    }

    @Test
    public void readSelfManagedIgnoresPreviousReadObject() {
        Handle<ExampleIntValue> handle = commit(stm, new ExampleIntValue());

        Transaction t = stm.startTransaction();
        ExampleIntValue v1 = t.read(handle);
        ExampleIntValue v2 = t.readSelfManaged(handle);

        assertNotNull(v1);
        assertNotNull(v2);
        assertFalse(v1 == v2);
        assertEquals(v1, v2);
        assertEquals(v1.getHandle(), v2.getHandle());
    }

    @Test
    public void readSelfManagedIgnoresAttachedObject() {
        Handle<ExampleIntValue> handle = commit(stm, new ExampleIntValue());

        Transaction t = stm.startTransaction();
        ExampleIntValue v1 = t.read(handle);
        t.attach(v1);
        ExampleIntValue v2 = t.readSelfManaged(handle);

        assertNotNull(v1);
        assertNotNull(v2);
        assertFalse(v1 == v2);
        assertEquals(v1, v2);
        assertEquals(v1.getHandle(), v2.getHandle());
    }

    @Test
    public void changeOnReadSelfManagedWontBeCommittedIfThereIsNoReferenceToIt() {
        ExampleIntValue original = new ExampleIntValue(29);
        Handle<ExampleIntValue> handle = commit(stm, original);

        long writes = stm.getStatistics().getWriteCount();

        Transaction t = stm.startTransaction();
        ExampleIntValue changed = t.readSelfManaged(handle);
        changed.inc();
        t.commit();

        TestUtils.assertIntegerValue(stm, handle, 29);
        assertWriteCount(stm, writes);
    }

    @Test
    public void changeOnReadSelfManagedWillBeCommittedIfThereIsSomeReferenceToIt() {
        ExampleIntValue original = new ExampleIntValue(29);

        Handle<ExampleIntValue> handle = commit(stm, original);
        long writeCount = stm.getStatistics().getWriteCount();

        Transaction t = stm.startTransaction();
        ExampleIntValue changed = t.readSelfManaged(handle);
        ExamplePair<ExampleIntValue, Object> pair = new ExamplePair<ExampleIntValue, Object>(changed, null);
        t.attach(pair);
        changed.inc();
        t.commit();

        assertIntegerValue(stm, handle, 30);
        assertWriteCount(stm, writeCount + 2);
    }

    @Test
    public void readSelfManagedNotUsedInListeningIfThereIsNoOtherReferenceToIt() throws InterruptedException {
        Handle<ExampleIntValue> handle = commit(stm, new ExampleIntValue());

        Transaction t = stm.startTransaction();
        ExampleIntValue found = t.readSelfManaged(handle);

        try {
            t.abortAndRetry();
            fail();
        } catch (NoProgressPossibleException ex) {
        }
    }

    // ================= the other states ====================

    @Test
    public void readSelfManagedFailsIfTransactionAborted() {
        Handle<ExampleIntValue> handle = commit(stm, new ExampleIntValue());

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
        Handle<ExampleIntValue> handle = commit(stm, new ExampleIntValue());

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
