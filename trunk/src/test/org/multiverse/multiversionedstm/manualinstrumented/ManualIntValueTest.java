package org.multiverse.multiversionedstm.manualinstrumented;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;

public class ManualIntValueTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void testFreshObjectIsDirty() {
        ManualIntValue value = new ManualIntValue();
        assertTrue(value.isDirty());
    }

    @Test
    public void testFreshCommitedObjectIsNotDirty() {
        Transaction t = stm.startTransaction();
        ManualIntValue value = new ManualIntValue(10);
        t.attach(value);
        t.commit();

        assertFalse(value.isDirty());
    }

    @Test
    public void testLoadedObjectIsNotDirty() {
        Handle<ManualIntValue> handle = commit(stm, new ManualIntValue(10));

        Transaction t = stm.startTransaction();
        ManualIntValue value = t.read(handle);
        assertFalse(value.isDirty());
    }

    @Test
    public void testLoadedObjectThatIsUpdatedIsDirty() {
        Handle<ManualIntValue> handle = commit(stm, new ManualIntValue(10));

        Transaction t = stm.startTransaction();
        ManualIntValue value = t.read(handle);
        value.inc();
        assertTrue(value.isDirty());
    }
}
