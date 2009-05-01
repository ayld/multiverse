package org.multiverse.multiversionedstm;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Handle;
import org.multiverse.api.LazyReference;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.examples.IntegerValue;

public class Transaction_ReadLazyAndUnmanagedTest {
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
    public void readLazyAndUnmanagedNull() {
        Transaction t = stm.startTransaction();
        long materializedCount = stm.getStatistics().getMaterializedCount();
        LazyReference ref = t.readLazyAndUnmanaged(null);

        assertNull(ref);
        assertIsActive(t);
        assertMaterializedCount(stm, materializedCount);
    }

    @Test
    public void readLazyAndUnmanaged() {
        IntegerValue original = new IntegerValue();
        Handle<IntegerValue> handle = commit(stm, original);

        long materializedCount = stm.getStatistics().getMaterializedCount();
        Transaction t = stm.startTransaction();
        LazyReference<IntegerValue> ref = t.readLazyAndUnmanaged(handle);

        assertFalse(ref.isLoaded());
        IntegerValue value = ref.get();

        assertSame(handle, ref.getHandle());
        assertEquals(original, value);
        assertMaterializedCount(stm, materializedCount + 1);
    }

    @Test
    public void readLazyAndUnmanagedDoesntSeeNormalReads() {
        IntegerValue original = new IntegerValue();
        Handle<IntegerValue> handle = commit(stm, original);

        long materializedCount = stm.getStatistics().getMaterializedCount();
        Transaction t = stm.startTransaction();
        IntegerValue found = t.read(handle);
        LazyReference<IntegerValue> ref = t.readLazyAndUnmanaged(handle);

        assertFalse(ref.isLoaded());
        IntegerValue value = ref.get();

        assertEquals(handle, ref.getHandle());
        assertEquals(original, value);
        assertFalse(found == value);
        assertMaterializedCount(stm, materializedCount + 2);
    }

    @Test
    public void readLazyAndUnmanagedDoesNoSaveChanges() {
        IntegerValue original = new IntegerValue();
        Handle<IntegerValue> handle = commit(stm, original);

        long writeCount = stm.getStatistics().getWriteCount();

        Transaction t = stm.startTransaction();
        LazyReference<IntegerValue> ref = t.readLazyAndUnmanaged(handle);
        ref.get().inc();
        t.commit();

        assertIsCommitted(t);
        assertWriteCount(stm, writeCount);
    }

    @Test
    public void readLazyFailsIfGetIsDoneAfterAbort() {
        Handle<IntegerValue> handle = commit(stm, new IntegerValue());

        long materializedCount = stm.getStatistics().getMaterializedCount();
        Transaction t = stm.startTransaction();
        LazyReference<IntegerValue> ref = t.readLazyAndUnmanaged(handle);
        t.abort();

        try {
            ref.get();
            fail();
        } catch (IllegalStateException ex) {
        }

        assertMaterializedCount(stm, materializedCount);
    }

    @Test
    public void readLazyFailsIfGetIsDoneAfterCommit() {
        Handle<IntegerValue> handle = commit(stm, new IntegerValue());

        long materializedCount = stm.getStatistics().getMaterializedCount();
        Transaction t = stm.startTransaction();
        LazyReference<IntegerValue> ref = t.readLazyAndUnmanaged(handle);
        t.commit();

        try {
            ref.get();
            fail();
        } catch (IllegalStateException ex) {
        }

        assertMaterializedCount(stm, materializedCount);
    }

    @Test
    public void readLazyAndUnmanagedFailsIfTransactionIsAborted() {
        Handle<IntegerValue> handle = commit(stm, new IntegerValue());

        Transaction t = stm.startTransaction();
        t.abort();

        try {
            t.readLazyAndUnmanaged(handle);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertIsAborted(t);
    }

    @Test
    public void readLazyAndUnmanagedFailsIfTransactionIsCommitted() {
        Handle<IntegerValue> handle = commit(stm, new IntegerValue());

        Transaction t = stm.startTransaction();
        t.commit();

        try {
            t.readLazyAndUnmanaged(handle);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertIsCommitted(t);
    }
}
