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
import org.multiverse.multiversionedstm.examples.ExampleIntegerValue;
import org.multiverse.multiversionedstm.examples.ExamplePair;

public class Transaction_ReadUnmanagedTest {
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
    public void readUnmanagedNull() {
        Transaction t = stm.startTransaction();
        Object result = t.readUnmanaged(null);
        assertNull(result);
        assertIsActive(t);
    }

    @Test
    public void readUnmanagedDoesNotReturnSameValue() {
        Handle<ExampleIntegerValue> handle = commit(stm, new ExampleIntegerValue());

        long materializedCount = stm.getStatistics().getMaterializedCount();

        Transaction t = stm.startTransaction();
        ExampleIntegerValue v1 = t.readUnmanaged(handle);
        ExampleIntegerValue v2 = t.readUnmanaged(handle);

        assertNotNull(v1);
        assertNotNull(v2);
        assertFalse(v1 == v2);
        assertEquals(v1, v2);
        assertIsActive(t);
        assertMaterializedCount(stm, materializedCount + 2);
    }

    @Test
    public void readUnmanagedIgnoresReadObject() {
        Handle<ExampleIntegerValue> handle = commit(stm, new ExampleIntegerValue());

        Transaction t = stm.startTransaction();
        ExampleIntegerValue v1 = t.read(handle);
        ExampleIntegerValue v2 = t.readUnmanaged(handle);

        assertNotNull(v1);
        assertNotNull(v2);
        assertFalse(v1 == v2);
        assertEquals(v1, v2);
        assertEquals(v1.getHandle(), v2.getHandle());
    }

    @Test
    public void readUnmanagedIgnoresAttachedObject() {
        Handle<ExampleIntegerValue> handle = commit(stm, new ExampleIntegerValue());

        Transaction t = stm.startTransaction();
        ExampleIntegerValue v1 = t.read(handle);
        t.attach(v1);
        ExampleIntegerValue v2 = t.readUnmanaged(handle);

        assertNotNull(v1);
        assertNotNull(v2);
        assertFalse(v1 == v2);
        assertEquals(v1, v2);
        assertEquals(v1.getHandle(), v2.getHandle());
    }

    @Test
    public void changeOnReadUnmanagedWontBeCommittedIfThereIsNoReferenceToIt() {
        ExampleIntegerValue original = new ExampleIntegerValue(29);
        Handle<ExampleIntegerValue> handle = commit(stm, original);

        long writes = stm.getStatistics().getWriteCount();

        Transaction t = stm.startTransaction();
        ExampleIntegerValue changed = t.readUnmanaged(handle);
        changed.inc();
        t.commit();

        TestUtils.assertIntegerValue(stm, handle, 29);
        assertWriteCount(stm, writes);
    }

    @Test
    public void changeOnreadUnmanagedWillBeCommittedIfThereIsSomeReferenceToIt() {
        ExampleIntegerValue original = new ExampleIntegerValue(29);

        Handle<ExampleIntegerValue> handle = commit(stm, original);
        long writeCount = stm.getStatistics().getWriteCount();

        Transaction t = stm.startTransaction();
        ExampleIntegerValue changed = t.readUnmanaged(handle);
        ExamplePair<ExampleIntegerValue, Object> pair = new ExamplePair<ExampleIntegerValue, Object>(changed, null);
        t.attach(pair);
        changed.inc();
        t.commit();

        assertIntegerValue(stm, handle, 30);
        assertWriteCount(stm, writeCount + 2);
    }

    @Test
    public void readUnmanagedNotUsedInListeningIfThereIsNoOtherReferenceToIt() throws InterruptedException {
        Handle<ExampleIntegerValue> handle = commit(stm, new ExampleIntegerValue());

        Transaction t = stm.startTransaction();
        ExampleIntegerValue found = t.readUnmanaged(handle);

        try {
            t.abortAndRetry();
            fail();
        } catch (NoProgressPossibleException ex) {
        }
    }

    @Test
    public void readUnmanagedFailsIfTransactionAborted() {
        Handle<ExampleIntegerValue> handle = commit(stm, new ExampleIntegerValue());

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
    public void readUnmanagedFailsIfTransactionCommitted() {
        Handle<ExampleIntegerValue> handle = commit(stm, new ExampleIntegerValue());

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
