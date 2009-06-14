package org.multiverse.multiversionedstm;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Handle;
import org.multiverse.api.LazyReference;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.NoCommittedDataFoundException;
import org.multiverse.api.exceptions.SnapshotTooOldException;
import org.multiverse.multiversionedstm.examples.ExampleIntValue;

public class Transaction_ReadLazyTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void readLazyNull() {
        Transaction t = stm.startTransaction();
        LazyReference result = t.readLazy(null);
        assertNull(result);
    }

    @Test
    public void readLazyLoaded() {
        ExampleIntValue value = new ExampleIntValue(10);
        Handle<ExampleIntValue> handle = commit(stm, value);
        long rematerializedCount = stm.getStatistics().getMaterializedCount();

        Transaction t = stm.startTransaction();
        LazyReference<ExampleIntValue> lazyReference = t.readLazy(handle);

        assertSame(value.getHandle(), lazyReference.getHandle());
        assertFalse(lazyReference.isLoaded());
        assertMaterializedCount(stm, rematerializedCount);

        ExampleIntValue found = lazyReference.get();
        assertEquals(value, lazyReference.get());
        assertMaterializedCount(stm, rematerializedCount + 1);
        assertIsActive(t);
    }

    @Test
    public void readLazyAttachedObject() {
        ExampleIntValue value = new ExampleIntValue(1);

        Transaction t1 = stm.startTransaction();
        Handle<ExampleIntValue> handle = t1.attach(value);

        ExampleIntValue foundValue = t1.read(handle);
        assertSame(value, foundValue);
    }

    @Test
    public void readLazyAlreadyRematerializedObject() {
        ExampleIntValue value = new ExampleIntValue(10);

        Transaction t1 = stm.startTransaction();
        Handle<ExampleIntValue> handle = t1.attach(value);
        t1.commit();

        Transaction t2 = stm.startTransaction();
        LazyReference<ExampleIntValue> found1 = t2.readLazy(handle);
        assertFalse(found1.isLoaded());

        LazyReference<ExampleIntValue> found2 = t2.readLazy(handle);
        assertFalse(found2.isLoaded());

        assertSame(found1, found2);
        assertEquals(value, found1.get());
        assertEquals(value, found1.get());
    }

    @Test
    public void readTooOldVersion() {
        ExampleIntValue value = new ExampleIntValue(10);

        Transaction t1 = stm.startTransaction();
        Handle<ExampleIntValue> handle = t1.attach(value);
        t1.commit();

        Transaction t2 = stm.startTransaction();
        Transaction t3 = stm.startTransaction();
        ExampleIntValue value3 = t3.readLazy(handle).get();
        value3.set(value3.get() + 1);
        t3.commit();

        long rematerializedCount = stm.getStatistics().getMaterializedCount();
        try {
            t2.readLazy(handle).get();
            fail();
        } catch (SnapshotTooOldException ex) {

        }

        assertIsActive(t2);
        assertMaterializedCount(stm, rematerializedCount);
    }

    @Test
    public void readLazyAttachedStmObject() {
        Transaction t = stm.startTransaction();
        ExampleIntValue newValue = new ExampleIntValue();
        Handle<ExampleIntValue> handle = t.attach(newValue);

        long rematerializedCount = stm.getStatistics().getMaterializedCount();

        LazyReference lazyReference = t.readLazy(handle);

        assertNotNull(lazyReference);
        assertTrue(lazyReference.isLoaded());
        assertEquals(handle, lazyReference.getHandle());
        assertSame(newValue, lazyReference.get());
        assertMaterializedCount(stm, rematerializedCount);
        assertIsActive(t);
    }

    @Test
    public void readLazyNonExitsting() {
        Handle handle = new DefaultMultiversionedHandle();
        Transaction t = stm.startTransaction();

        LazyReference lazyReference = t.readLazy(handle);

        long rematerializedCount = stm.getStatistics().getMaterializedCount();

        assertSame(handle, lazyReference.getHandle());
        assertFalse(lazyReference.isLoaded());

        try {
            lazyReference.get();
            fail();
        } catch (NoCommittedDataFoundException ex) {
        }

        assertFalse(lazyReference.isLoaded());
        assertMaterializedCount(stm, rematerializedCount);
    }


    // ================== other states ===================================

    @Test
    public void getFailsIfTransactionIsAbortedAfterTheReadLazy() {
        Handle<ExampleIntValue> handle = commit(stm, new ExampleIntValue());

        Transaction t = stm.startTransaction();
        LazyReference<ExampleIntValue> found = t.readLazy(handle);
        t.commit();

        try {
            found.get();
            fail();
        } catch (IllegalStateException ex) {

        }

        assertIsCommitted(t);
    }


    @Test
    public void getFailsIfTransactionIsCommittedAfterTheReadLazy() {
        Handle<ExampleIntValue> handle = commit(stm, new ExampleIntValue());

        Transaction t = stm.startTransaction();
        LazyReference<ExampleIntValue> found = t.readLazy(handle);
        t.abort();

        try {
            found.get();
            fail();
        } catch (IllegalStateException ex) {

        }

        assertIsAborted(t);
    }


    @Test
    public void readLazyFailsIfCommitted() {
        Handle<ExampleIntValue> handle = commit(stm, new ExampleIntValue());

        Transaction t = stm.startTransaction();
        t.commit();

        try {
            t.readLazy(handle);
            fail();
        } catch (IllegalStateException ex) {

        }

        assertIsCommitted(t);
    }


    @Test
    public void readLazyFailsIfAborted() {
        Transaction t = stm.startTransaction();
        t.abort();

        try {
            t.readLazy(new DefaultMultiversionedHandle());
            fail();
        } catch (IllegalStateException ex) {
        }

        assertIsAborted(t);
    }
}
