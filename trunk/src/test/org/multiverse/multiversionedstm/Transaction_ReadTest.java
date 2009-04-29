package org.multiverse.multiversionedstm;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Originator;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.NoCommittedDataFoundException;
import org.multiverse.examples.IntegerValue;

public class Transaction_ReadTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void readNull() {
        long materializedCount = stm.getStatistics().getMaterializedCount();

        Transaction t = stm.startTransaction();
        Object result = t.read(null);

        assertNull(result);
        assertIsActive(t);
        assertMaterializedCount(stm, materializedCount);
    }

    @Test
    public void read() {
        Originator<IntegerValue> originator = commit(stm, new IntegerValue());

        long materializedCount = stm.getStatistics().getMaterializedCount();

        Transaction t = stm.startTransaction();
        IntegerValue v = t.read(originator);

        assertNotNull(v);
        assertEquals(0, v.get());
        assertIsActive(t);
        assertMaterializedCount(stm, materializedCount + 1);
    }

    @Test
    public void readAttached() {
        Transaction t = stm.startTransaction();
        IntegerValue original = new IntegerValue();
        Originator<IntegerValue> originator = t.attach(original);

        long materializedCount = stm.getStatistics().getMaterializedCount();
        IntegerValue found = t.read(originator);

        assertSame(original, found);
        assertMaterializedCount(stm, materializedCount);
        assertIsActive(t);
    }

    @Test
    public void rereadDoesntLeadToAnotherMaterialize() {
        Originator<IntegerValue> originator = commit(stm, new IntegerValue());

        long materializedCount = stm.getStatistics().getMaterializedCount();

        Transaction t = stm.startTransaction();
        IntegerValue v1 = t.read(originator);
        IntegerValue v2 = t.read(originator);

        assertNotNull(v1);
        assertEquals(0, v1.get());
        assertSame(v1, v2);
        assertIsActive(t);
        assertMaterializedCount(stm, materializedCount + 1);
    }

    @Test
    public void readFailsIThereIsNoCommittedData() {
        Transaction t1 = stm.startTransaction();
        Originator<IntegerValue> originator = t1.attach(new IntegerValue());

        long materializedCount = stm.getStatistics().getMaterializedCount();

        Transaction t2 = stm.startTransaction();
        try {
            t2.read(originator);
            fail();
        } catch (NoCommittedDataFoundException ex) {
        }

        assertIsActive(t2);
        assertMaterializedCount(stm, materializedCount);
    }


    @Test
    public void readFailsIfTransactionAlreadyIsAborted() {
        Originator<IntegerValue> originator = commit(stm, new IntegerValue());

        Transaction t = stm.startTransaction();
        t.abort();

        long materializedCount = stm.getStatistics().getMaterializedCount();

        try {
            t.read(originator);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertIsAborted(t);
        assertMaterializedCount(stm, materializedCount);
    }

    public void readFailsIfTransactionAlreadyIsCommitted() {
        Originator<IntegerValue> originator = commit(stm, new IntegerValue());

        Transaction t = stm.startTransaction();
        t.abort();

        long materializedCount = stm.getStatistics().getMaterializedCount();

        try {
            t.read(originator);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertIsCommitted(t);
        assertMaterializedCount(stm, materializedCount);
    }
}
