package org.multiverse.multiversionedstm.examples;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;

public class ExampleIntValueTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void testFreshObjectIsDirty() {
        ExampleIntValue value = new ExampleIntValue();
        assertTrue(value.isDirty());
    }

    @Test
    public void testFreshCommitedObjectIsNotDirty() {
        Transaction t = stm.startTransaction();
        ExampleIntValue value = new ExampleIntValue(10);
        t.attach(value);
        t.commit();

        assertFalse(value.isDirty());
    }

    @Test
    public void testLoadedObjectIsNotDirty() {
        Handle<ExampleIntValue> handle = commit(stm, new ExampleIntValue(10));

        Transaction t = stm.startTransaction();
        ExampleIntValue value = t.read(handle);
        assertFalse(value.isDirty());
    }

    @Test
    public void testLoadedObjectThatIsUpdatedIsDirty() {
        Handle<ExampleIntValue> handle = commit(stm, new ExampleIntValue(10));

        Transaction t = stm.startTransaction();
        ExampleIntValue value = t.read(handle);
        value.inc();
        assertTrue(value.isDirty());
    }
}
