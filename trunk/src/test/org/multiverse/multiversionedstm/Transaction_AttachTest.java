package org.multiverse.multiversionedstm;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Originator;
import org.multiverse.api.Transaction;
import org.multiverse.examples.IntegerValue;
import org.multiverse.examples.Stack;

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
        Originator<IntegerValue> originator = commit(stm, new IntegerValue());

        Transaction t = stm.startTransaction();
        IntegerValue foundValue = t.read(originator);
        Originator<IntegerValue> foundOriginator = t.attach(foundValue);
        assertSame(originator, foundOriginator);
    }

    @Test
    public void attachAlreadyAttachedObjectReturnsTheSameOriginator() {
        IntegerValue value = new IntegerValue(1);

        Transaction t = stm.startTransaction();
        Originator<IntegerValue> originator1 = t.attach(value);
        Originator<IntegerValue> originator2 = t.attach(value);
        assertSame(originator1, originator2);
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
