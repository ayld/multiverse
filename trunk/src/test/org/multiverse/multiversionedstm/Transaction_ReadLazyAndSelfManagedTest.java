package org.multiverse.multiversionedstm;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Handle;
import org.multiverse.api.LazyReference;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.manualinstrumented.ManualIntValue;

public class Transaction_ReadLazyAndSelfManagedTest {
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
        LazyReference ref = t.readLazyAndSelfManaged(null);

        assertNull(ref);
        assertIsActive(t);
        assertMaterializedCount(stm, materializedCount);
    }

    @Test
    public void readLazyAndUnmanaged() {
        ManualIntValue original = new ManualIntValue();
        Handle<ManualIntValue> handle = commit(stm, original);

        long materializedCount = stm.getStatistics().getMaterializedCount();
        Transaction t = stm.startTransaction();
        LazyReference<ManualIntValue> ref = t.readLazyAndSelfManaged(handle);

        assertFalse(ref.isLoaded());
        ManualIntValue value = ref.get();

        assertSame(handle, ref.getHandle());
        assertEquals(original, value);
        assertMaterializedCount(stm, materializedCount + 1);
    }

    @Test
    public void readLazyAndUnmanagedDoesntSeeNormalReads() {
        ManualIntValue original = new ManualIntValue();
        Handle<ManualIntValue> handle = commit(stm, original);

        long materializedCount = stm.getStatistics().getMaterializedCount();
        Transaction t = stm.startTransaction();
        ManualIntValue found = t.read(handle);
        LazyReference<ManualIntValue> ref = t.readLazyAndSelfManaged(handle);

        assertFalse(ref.isLoaded());
        ManualIntValue value = ref.get();

        assertEquals(handle, ref.getHandle());
        assertEquals(original, value);
        assertFalse(found == value);
        assertMaterializedCount(stm, materializedCount + 2);
    }

    @Test
    public void readLazyAndUnmanagedDoesNoSaveChanges() {
        ManualIntValue original = new ManualIntValue();
        Handle<ManualIntValue> handle = commit(stm, original);

        long writeCount = stm.getStatistics().getWriteCount();

        Transaction t = stm.startTransaction();
        LazyReference<ManualIntValue> ref = t.readLazyAndSelfManaged(handle);
        ref.get().inc();
        t.commit();

        assertIsCommitted(t);
        assertWriteCount(stm, writeCount);
    }

    @Test
    public void readLazyFailsIfGetIsDoneAfterAbort() {
        Handle<ManualIntValue> handle = commit(stm, new ManualIntValue());

        long materializedCount = stm.getStatistics().getMaterializedCount();
        Transaction t = stm.startTransaction();
        LazyReference<ManualIntValue> ref = t.readLazyAndSelfManaged(handle);
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
        Handle<ManualIntValue> handle = commit(stm, new ManualIntValue());

        long materializedCount = stm.getStatistics().getMaterializedCount();
        Transaction t = stm.startTransaction();
        LazyReference<ManualIntValue> ref = t.readLazyAndSelfManaged(handle);
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
        Handle<ManualIntValue> handle = commit(stm, new ManualIntValue());

        Transaction t = stm.startTransaction();
        t.abort();

        try {
            t.readLazyAndSelfManaged(handle);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertIsAborted(t);
    }

    @Test
    public void readLazyAndUnmanagedFailsIfTransactionIsCommitted() {
        Handle<ManualIntValue> handle = commit(stm, new ManualIntValue());

        Transaction t = stm.startTransaction();
        t.commit();

        try {
            t.readLazyAndSelfManaged(handle);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertIsCommitted(t);
    }
}
