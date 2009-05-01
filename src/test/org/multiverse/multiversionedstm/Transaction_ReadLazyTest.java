package org.multiverse.multiversionedstm;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.LazyReference;
import org.multiverse.api.Originator;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.NoCommittedDataFoundException;
import org.multiverse.api.exceptions.SnapshotTooOldException;
import org.multiverse.multiversionedstm.examples.IntegerValue;

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
        IntegerValue value = new IntegerValue(10);
        Originator<IntegerValue> originator = commit(stm, value);
        long rematerializedCount = stm.getStatistics().getMaterializedCount();

        Transaction t = stm.startTransaction();
        LazyReference<IntegerValue> lazyReference = t.readLazy(originator);

        assertSame(value.getOriginator(), lazyReference.getOriginator());
        assertFalse(lazyReference.isLoaded());
        assertMaterializedCount(stm, rematerializedCount);

        IntegerValue found = lazyReference.get();
        assertEquals(value, lazyReference.get());
        assertMaterializedCount(stm, rematerializedCount + 1);
        assertIsActive(t);
    }

    @Test
    public void readLazyAttachedObject() {
        IntegerValue value = new IntegerValue(1);
        Transaction t1 = stm.startTransaction();
        Originator<IntegerValue> originator = t1.attach(value);

        IntegerValue foundValue = t1.read(originator);
        assertSame(value, foundValue);
    }

    @Test
    public void readLazyAlreadyRematerializedStmObject() {
        IntegerValue value = new IntegerValue(10);

        Transaction t1 = stm.startTransaction();
        Originator<IntegerValue> originator = t1.attach(value);
        t1.commit();

        Transaction t2 = stm.startTransaction();
        LazyReference<IntegerValue> found1 = t2.readLazy(originator);
        assertFalse(found1.isLoaded());

        LazyReference<IntegerValue> found2 = t2.readLazy(originator);
        assertFalse(found2.isLoaded());

        assertSame(found1, found2);
        assertEquals(value, found1.get());
        assertEquals(value, found1.get());
    }

    @Test
    public void readTooOldVersion() {
        IntegerValue value = new IntegerValue(10);

        Transaction t1 = stm.startTransaction();
        Originator<IntegerValue> originator = t1.attach(value);
        t1.commit();

        Transaction t2 = stm.startTransaction();
        Transaction t3 = stm.startTransaction();
        IntegerValue value3 = t3.readLazy(originator).get();
        value3.set(value3.get() + 1);
        t3.commit();

        long rematerializedCount = stm.getStatistics().getMaterializedCount();
        try {
            t2.readLazy(originator).get();
            fail();
        } catch (SnapshotTooOldException ex) {

        }

        assertIsActive(t2);
        assertMaterializedCount(stm, rematerializedCount);
    }

    @Test
    public void readLazyAttachedStmObject() {
        Transaction t = stm.startTransaction();
        IntegerValue newValue = new IntegerValue();
        Originator<IntegerValue> originator = t.attach(newValue);

        long rematerializedCount = stm.getStatistics().getMaterializedCount();

        LazyReference lazyReference = t.readLazy(originator);

        assertNotNull(lazyReference);
        assertTrue(lazyReference.isLoaded());
        assertEquals(originator, lazyReference.getOriginator());
        assertSame(newValue, lazyReference.get());
        assertMaterializedCount(stm, rematerializedCount);
        assertIsActive(t);
    }

    @Test
    public void readLazyNonExitsting() {
        Originator originator = new DefaultOriginator();
        Transaction t = stm.startTransaction();

        LazyReference lazyReference = t.readLazy(originator);

        long rematerializedCount = stm.getStatistics().getMaterializedCount();

        assertSame(originator, lazyReference.getOriginator());
        assertFalse(lazyReference.isLoaded());

        try {
            lazyReference.get();
            fail();
        } catch (NoCommittedDataFoundException ex) {
        }

        assertFalse(lazyReference.isLoaded());
        assertMaterializedCount(stm, rematerializedCount);
    }

    @Test
    public void getFailsIfTransactionIsCommitted() {
        Originator<IntegerValue> originator = commit(stm, new IntegerValue());

        Transaction t = stm.startTransaction();
        LazyReference<IntegerValue> found = t.readLazy(originator);
        t.abort();

        try {
            found.get();
            fail();
        } catch (IllegalStateException ex) {

        }

        assertIsAborted(t);
    }

    @Test
    public void getFailsIfTransactionIsAborted() {
        Originator<IntegerValue> originator = commit(stm, new IntegerValue());

        Transaction t = stm.startTransaction();
        LazyReference<IntegerValue> found = t.readLazy(originator);
        t.commit();

        try {
            found.get();
            fail();
        } catch (IllegalStateException ex) {

        }

        assertIsCommitted(t);
    }

    @Test
    public void readLazyFailsIfCommitted() {
        Originator<IntegerValue> originator = commit(stm, new IntegerValue());

        Transaction t = stm.startTransaction();
        t.commit();

        try {
            t.readLazy(originator);
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
            t.readLazy(new DefaultOriginator());
            fail();
        } catch (IllegalStateException ex) {
        }

        assertIsAborted(t);
    }
}
