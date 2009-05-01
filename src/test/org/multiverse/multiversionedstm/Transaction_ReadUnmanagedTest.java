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
import org.multiverse.multiversionedstm.examples.IntegerValue;
import org.multiverse.multiversionedstm.examples.Pair;

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
        Handle<IntegerValue> handle = commit(stm, new IntegerValue());

        long materializedCount = stm.getStatistics().getMaterializedCount();

        Transaction t = stm.startTransaction();
        IntegerValue v1 = t.readUnmanaged(handle);
        IntegerValue v2 = t.readUnmanaged(handle);

        assertNotNull(v1);
        assertNotNull(v2);
        assertFalse(v1 == v2);
        assertEquals(v1, v2);
        assertIsActive(t);
        assertMaterializedCount(stm, materializedCount + 2);
    }

    @Test
    public void readUnmanagedIgnoresReadObject() {
        Handle<IntegerValue> handle = commit(stm, new IntegerValue());

        Transaction t = stm.startTransaction();
        IntegerValue v1 = t.read(handle);
        IntegerValue v2 = t.readUnmanaged(handle);

        assertNotNull(v1);
        assertNotNull(v2);
        assertFalse(v1 == v2);
        assertEquals(v1, v2);
        assertEquals(v1.getHandle(), v2.getHandle());
    }

    @Test
    public void readUnmanagedIgnoresAttachedObject() {
        Handle<IntegerValue> handle = commit(stm, new IntegerValue());

        Transaction t = stm.startTransaction();
        IntegerValue v1 = t.read(handle);
        t.attach(v1);
        IntegerValue v2 = t.readUnmanaged(handle);

        assertNotNull(v1);
        assertNotNull(v2);
        assertFalse(v1 == v2);
        assertEquals(v1, v2);
        assertEquals(v1.getHandle(), v2.getHandle());
    }

    @Test
    public void changeOnReadUnmanagedWontBeCommittedIfThereIsNoReferenceToIt() {
        IntegerValue original = new IntegerValue(29);
        Handle<IntegerValue> handle = commit(stm, original);

        long writes = stm.getStatistics().getWriteCount();

        Transaction t = stm.startTransaction();
        IntegerValue changed = t.readUnmanaged(handle);
        changed.inc();
        t.commit();

        TestUtils.assertIntegerValue(stm, handle, 29);
        assertWriteCount(stm, writes);
    }

    @Test
    public void changeOnreadUnmanagedWillBeCommittedIfThereIsSomeReferenceToIt() {
        IntegerValue original = new IntegerValue(29);

        Handle<IntegerValue> handle = commit(stm, original);
        long writeCount = stm.getStatistics().getWriteCount();

        Transaction t = stm.startTransaction();
        IntegerValue changed = t.readUnmanaged(handle);
        Pair<IntegerValue, Object> pair = new Pair<IntegerValue, Object>(changed, null);
        t.attach(pair);
        changed.inc();
        t.commit();

        assertIntegerValue(stm, handle, 30);
        assertWriteCount(stm, writeCount + 2);
    }

    @Test
    public void readUnmanagedNotUsedInListeningIfThereIsNoOtherReferenceToIt() throws InterruptedException {
        Handle<IntegerValue> handle = commit(stm, new IntegerValue());

        Transaction t = stm.startTransaction();
        IntegerValue found = t.readUnmanaged(handle);

        try {
            t.abortAndRetry();
            fail();
        } catch (NoProgressPossibleException ex) {
        }
    }

    @Test
    public void readUnmanagedFailsIfTransactionAborted() {
        Handle<IntegerValue> handle = commit(stm, new IntegerValue());

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
        Handle<IntegerValue> handle = commit(stm, new IntegerValue());

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
