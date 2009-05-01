package org.multiverse.multiversionedstm;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.examples.IntegerValue;
import org.multiverse.multiversionedstm.examples.Stack;

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
        Handle<IntegerValue> handle = commit(stm, new IntegerValue());

        Transaction t = stm.startTransaction();
        IntegerValue foundValue = t.read(handle);
        Handle<IntegerValue> foundHandle = t.attach(foundValue);
        assertSame(handle, foundHandle);
    }

    @Test
    public void attachAlreadyAttachedObjectReturnsTheSameHandle() {
        IntegerValue value = new IntegerValue(1);

        Transaction t = stm.startTransaction();
        Handle<IntegerValue> handle1 = t.attach(value);
        Handle<IntegerValue> handle2 = t.attach(value);
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
            t.attach(new Stack());
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
            t.attach(new Stack());
            fail();
        } catch (IllegalStateException ex) {
        }

        assertIsCommitted(t);
    }
}
