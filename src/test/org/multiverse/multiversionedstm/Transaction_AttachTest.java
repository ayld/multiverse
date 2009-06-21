package org.multiverse.multiversionedstm;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.manualinstrumented.ManualIntValue;
import org.multiverse.multiversionedstm.manualinstrumented.ManualStack;

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
    public void attachFailsIfObjectIsNotInstanceOfMaterializedObject() {
        Transaction t = stm.startTransaction();

        try {
            t.attach("");
            fail();
        } catch (IllegalArgumentException ex) {
        }

        assertIsActive(t);
    }

    @Test
    public void attachOfFreshEntity() {
        ManualIntValue value = new ManualIntValue(1);

        Transaction t = stm.startTransaction();
        Handle<ManualIntValue> handle = t.attach(value);
        assertHasHandle(value, handle);
    }

    @Test
    public void attachOfPreviouslyReadEntity() {
        Handle<ManualIntValue> handle = commit(stm, new ManualIntValue());

        Transaction t = stm.startTransaction();
        ManualIntValue found = t.read(handle);
        Handle<ManualIntValue> foundHandle = t.attach(found);
        assertSame(handle, foundHandle);
    }

    @Test
    public void multipleAttachOfFreshEntity() {
        ManualIntValue value = new ManualIntValue(1);

        Transaction t = stm.startTransaction();
        Handle<ManualIntValue> handle1 = t.attach(value);
        Handle<ManualIntValue> handle2 = t.attach(value);
        assertSame(handle1, handle2);
    }

    @Test
    public void multipleAttachOfPreviouslyReadEntity() {
        Handle<ManualIntValue> handle = commit(stm, new ManualIntValue());

        Transaction t = stm.startTransaction();
        ManualIntValue foundValue = t.read(handle);
        Handle<ManualIntValue> handle1 = t.attach(foundValue);
        Handle<ManualIntValue> handle2 = t.attach(foundValue);
        assertSame(handle1, handle2);
    }


    // =============== other states ==============

    @Test
    public void attachFailsIfTransactionIsAborted() {
        Transaction t = stm.startTransaction();
        t.abort();

        try {
            t.attach(new ManualStack());
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
            t.attach(new ManualStack());
            fail();
        } catch (IllegalStateException ex) {
        }

        assertIsCommitted(t);
    }
}
