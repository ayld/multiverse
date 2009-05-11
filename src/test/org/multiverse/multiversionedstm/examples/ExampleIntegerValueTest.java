package org.multiverse.multiversionedstm.examples;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;

public class ExampleIntegerValueTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void testFreshObjectIsDirty() {
        ExampleIntegerValue value = new ExampleIntegerValue();
        assertTrue(value.isDirty());
    }

    @Test
    public void testFreshCommitedObjectIsNotDirty() {
        Transaction t = stm.startTransaction();
        ExampleIntegerValue value = new ExampleIntegerValue(10);
        t.attach(value);
        t.commit();

        assertFalse(value.isDirty());
    }

    @Test
    public void testLoadedObjectIsNotDirty() {
        Handle<ExampleIntegerValue> handle = commit(stm, new ExampleIntegerValue(10));

        Transaction t = stm.startTransaction();
        ExampleIntegerValue value = t.read(handle);
        assertFalse(value.isDirty());
    }

    @Test
    public void testLoadedObjectThatIsUpdatedIsDirty() {
        Handle<ExampleIntegerValue> handle = commit(stm, new ExampleIntegerValue(10));

        Transaction t = stm.startTransaction();
        ExampleIntegerValue value = t.read(handle);
        value.inc();
        assertTrue(value.isDirty());
    }
}
