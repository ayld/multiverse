package org.multiverse.multiversionedstm;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestUtils;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Originator;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.NoProgressPossibleException;
import org.multiverse.examples.IntegerValue;
import org.multiverse.examples.Pair;

public class Transaction_ReadUnsafeTest {
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
    public void readUnsafeNull() {
        Transaction t = stm.startTransaction();
        Object result = t.readUnmanaged(null);
        assertNull(result);
        assertIsActive(t);
    }

    @Test
    public void readUnsafeDoesNotReturnSameValue() {
        Originator<IntegerValue> originator = commit(stm, new IntegerValue());

        long materializedCount = stm.getStatistics().getMaterializedCount();

        Transaction t = stm.startTransaction();
        IntegerValue v1 = t.readUnmanaged(originator);
        IntegerValue v2 = t.readUnmanaged(originator);

        assertNotNull(v1);
        assertNotNull(v2);
        assertFalse(v1 == v2);
        assertEquals(v1, v2);
        assertIsActive(t);
        assertMaterializedCount(stm, materializedCount + 2);
    }

    @Test
    public void readUnsafeIgnoresReadObject() {
        Originator<IntegerValue> originator = commit(stm, new IntegerValue());

        Transaction t = stm.startTransaction();
        IntegerValue v1 = t.read(originator);
        IntegerValue v2 = t.readUnmanaged(originator);

        assertNotNull(v1);
        assertNotNull(v2);
        assertFalse(v1 == v2);
        assertEquals(v1, v2);
        assertEquals(v1.getOriginator(), v2.getOriginator());
    }


    @Test
    public void readUnsafeIgnoresAttachedObject() {
        Originator<IntegerValue> originator = commit(stm, new IntegerValue());

        Transaction t = stm.startTransaction();
        IntegerValue v1 = t.read(originator);
        t.attach(v1);
        IntegerValue v2 = t.readUnmanaged(originator);

        assertNotNull(v1);
        assertNotNull(v2);
        assertFalse(v1 == v2);
        assertEquals(v1, v2);
        assertEquals(v1.getOriginator(), v2.getOriginator());
    }

    @Test
    public void changeOnReadUnsafeWontBeCommittedIfThereIsNoReferenceToIt() {
        IntegerValue original = new IntegerValue(29);
        Originator<IntegerValue> originator = commit(stm, original);

        long writes = stm.getStatistics().getWriteCount();

        Transaction t = stm.startTransaction();
        IntegerValue changed = t.readUnmanaged(originator);
        changed.inc();
        t.commit();

        TestUtils.assertIntegerValue(stm, originator, 29);
        assertWriteCount(stm, writes);
    }

    @Test
    public void changeOnreadUnsafeWillBeCommittedIfThereIsSomeReferenceToIt() {
        IntegerValue original = new IntegerValue(29);

        Originator<IntegerValue> originator = commit(stm, original);
        long writeCount = stm.getStatistics().getWriteCount();

        Transaction t = stm.startTransaction();
        IntegerValue changed = t.readUnmanaged(originator);
        Pair<IntegerValue, Object> pair = new Pair<IntegerValue, Object>(changed, null);
        t.attach(pair);
        changed.inc();
        t.commit();

        assertIntegerValue(stm, originator, 30);
        assertWriteCount(stm, writeCount + 2);
    }

    @Test
    public void readUnsafeIsNotUsedInListeningIfThereIsNoOtherReferenceToIt() throws InterruptedException {
        Originator<IntegerValue> originator = commit(stm, new IntegerValue());

        Transaction t = stm.startTransaction();
        IntegerValue found = t.readUnmanaged(originator);

        try {
            t.abortAndRetry();
            fail();
        } catch (NoProgressPossibleException ex) {
        }
    }

    @Test
    public void readUnsafeFailsIfTransactionAborted() {
        Originator<IntegerValue> originator = commit(stm, new IntegerValue());

        Transaction t = stm.startTransaction();
        t.abort();

        try {
            t.read(originator);
            fail();
        } catch (IllegalStateException ex) {

        }
        assertIsAborted(t);
    }

    @Test
    public void readUnsafeFailsIfTransactionCommitted() {
        Originator<IntegerValue> originator = commit(stm, new IntegerValue());

        Transaction t = stm.startTransaction();
        t.commit();

        try {
            t.read(originator);
            fail();
        } catch (IllegalStateException ex) {

        }
        assertIsCommitted(t);
    }


}
