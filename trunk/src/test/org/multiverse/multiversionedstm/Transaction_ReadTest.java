package org.multiverse.multiversionedstm;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.NoCommittedDataFoundException;
import org.multiverse.multiversionedstm.examples.ExampleIntegerValue;

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
        Handle<ExampleIntegerValue> handle = commit(stm, new ExampleIntegerValue());

        long materializedCount = stm.getStatistics().getMaterializedCount();

        Transaction t = stm.startTransaction();
        ExampleIntegerValue v = t.read(handle);

        assertNotNull(v);
        assertEquals(0, v.get());
        assertIsActive(t);
        assertMaterializedCount(stm, materializedCount + 1);
    }

    @Test
    public void readAttached() {
        Transaction t = stm.startTransaction();
        ExampleIntegerValue original = new ExampleIntegerValue();
        Handle<ExampleIntegerValue> handle = t.attach(original);

        long materializedCount = stm.getStatistics().getMaterializedCount();
        ExampleIntegerValue found = t.read(handle);

        assertSame(original, found);
        assertMaterializedCount(stm, materializedCount);
        assertIsActive(t);
    }

    @Test
    public void rereadDoesntLeadToAnotherMaterialize() {
        Handle<ExampleIntegerValue> handle = commit(stm, new ExampleIntegerValue());

        long materializedCount = stm.getStatistics().getMaterializedCount();

        Transaction t = stm.startTransaction();
        ExampleIntegerValue v1 = t.read(handle);
        ExampleIntegerValue v2 = t.read(handle);

        assertNotNull(v1);
        assertEquals(0, v1.get());
        assertSame(v1, v2);
        assertIsActive(t);
        assertMaterializedCount(stm, materializedCount + 1);
    }

    @Test
    public void readFailsIThereIsNoCommittedData() {
        Transaction t1 = stm.startTransaction();
        Handle<ExampleIntegerValue> handle = t1.attach(new ExampleIntegerValue());

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
        Handle<ExampleIntegerValue> handle = commit(stm, new ExampleIntegerValue());

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
        Handle<ExampleIntegerValue> handle = commit(stm, new ExampleIntegerValue());

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
