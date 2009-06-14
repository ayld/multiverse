package org.multiverse.multiversionedstm;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.NoCommittedDataFoundException;
import org.multiverse.multiversionedstm.examples.ExampleIntValue;

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
        Handle<ExampleIntValue> handle = commit(stm, new ExampleIntValue());

        long materializedCount = stm.getStatistics().getMaterializedCount();

        Transaction t = stm.startTransaction();
        ExampleIntValue v = t.read(handle);

        assertNotNull(v);
        assertEquals(0, v.get());
        assertIsActive(t);
        assertMaterializedCount(stm, materializedCount + 1);
    }

    @Test
    public void readAttached() {
        Transaction t = stm.startTransaction();
        ExampleIntValue original = new ExampleIntValue();
        Handle<ExampleIntValue> handle = t.attach(original);

        long materializedCount = stm.getStatistics().getMaterializedCount();
        ExampleIntValue found = t.read(handle);

        assertSame(original, found);
        assertMaterializedCount(stm, materializedCount);
        assertIsActive(t);
    }

    @Test
    public void rereadDoesntLeadToAnotherMaterialize() {
        Handle<ExampleIntValue> handle = commit(stm, new ExampleIntValue());

        long materializedCount = stm.getStatistics().getMaterializedCount();

        Transaction t = stm.startTransaction();
        ExampleIntValue v1 = t.read(handle);
        ExampleIntValue v2 = t.read(handle);

        assertNotNull(v1);
        assertEquals(0, v1.get());
        assertSame(v1, v2);
        assertIsActive(t);
        assertMaterializedCount(stm, materializedCount + 1);
    }

    @Test
    public void readFailsIThereIsNoCommittedData() {
        Transaction t1 = stm.startTransaction();
        Handle<ExampleIntValue> handle = t1.attach(new ExampleIntValue());

        long materializedCount = stm.getStatistics().getMaterializedCount();

        Transaction t2 = stm.startTransaction();
        try {
            t2.read(handle);
            fail();
        } catch (NoCommittedDataFoundException ex) {
        }

        assertIsActive(t2);
        assertMaterializedCount(stm, materializedCount);
    }

    //=================== other transaction states ===============

    @Test
    public void readFailsIfTransactionAlreadyIsAborted() {
        Handle<ExampleIntValue> handle = commit(stm, new ExampleIntValue());

        Transaction t = stm.startTransaction();
        t.abort();

        long materializedCount = stm.getStatistics().getMaterializedCount();

        try {
            t.read(handle);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertIsAborted(t);
        assertMaterializedCount(stm, materializedCount);
    }

    public void readFailsIfTransactionAlreadyIsCommitted() {
        Handle<ExampleIntValue> handle = commit(stm, new ExampleIntValue());

        Transaction t = stm.startTransaction();
        t.abort();

        long materializedCount = stm.getStatistics().getMaterializedCount();

        try {
            t.read(handle);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertIsCommitted(t);
        assertMaterializedCount(stm, materializedCount);
    }
}
