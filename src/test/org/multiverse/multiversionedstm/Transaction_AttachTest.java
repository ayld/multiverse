package org.multiverse.multiversionedstm;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.examples.ExampleIntegerValue;
import org.multiverse.multiversionedstm.examples.ExampleStack;

public class Transaction_AttachTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void attachFailsIfArgumentIsNull() {
        Transaction t = stm.startTransaction();

        try {
            t.attach(null);
            fail();
        } catch (NullPointerException ex) {
        }

        assertIsActive(t);
    }

    @Test
    public void attachOfReadObject() {
        Handle<ExampleIntegerValue> handle = commit(stm, new ExampleIntegerValue());

        Transaction t = stm.startTransaction();
        ExampleIntegerValue foundValue = t.read(handle);
        Handle<ExampleIntegerValue> foundHandle = t.attach(foundValue);
        assertSame(handle, foundHandle);
    }

    @Test
    public void attachAlreadyAttachedObjectReturnsTheSameHandle() {
        ExampleIntegerValue value = new ExampleIntegerValue(1);

        Transaction t = stm.startTransaction();
        Handle<ExampleIntegerValue> handle1 = t.attach(value);
        Handle<ExampleIntegerValue> handle2 = t.attach(value);
        assertSame(handle1, handle2);
    }

    @Test
    public void attachFailsIfObjectIsNotInstanceOfMaterializedObject() {
        Transaction t = stm.startTransaction();

        try {
            t.attach("");
            fail();
        } catch (IllegalArgumentException ex) {
        }

        assertIsActive(t);
    }

    // =============== other states ==============

    @Test
    public void attachFailsIfTransactionIsAborted() {
        Transaction t = stm.startTransaction();
        t.abort();

        try {
            t.attach(new ExampleStack());
            fail();
        } catch (IllegalStateException ex) {
        }

        assertIsAborted(t);
    }

    @Test
    public void attachFailsIfTransactionIsCommitted() {
        Transaction t = stm.startTransaction();
        t.commit();

        try {
            t.attach(new ExampleStack());
            fail();
        } catch (IllegalStateException ex) {
        }

        assertIsCommitted(t);
    }
}
