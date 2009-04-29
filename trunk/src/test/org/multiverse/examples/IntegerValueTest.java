package org.multiverse.examples;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Originator;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;

public class IntegerValueTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void testFreshObjectIsDirty() {
        IntegerValue value = new IntegerValue();
        assertTrue(value.isDirty());
    }

    @Test
    public void testFreshCommitedObjectIsNotDirty() {
        Transaction t = stm.startTransaction();
        IntegerValue value = new IntegerValue(10);
        t.attach(value);
        t.commit();

        assertFalse(value.isDirty());
    }

    @Test
    public void testLoadedObjectIsNotDirty() {
        Originator<IntegerValue> originator = commit(stm, new IntegerValue(10));

        Transaction t = stm.startTransaction();
        IntegerValue value = t.read(originator);
        assertFalse(value.isDirty());
    }

    @Test
    public void testLoadedObjectThatIsUpdatedIsDirty() {
        Originator<IntegerValue> originator = commit(stm, new IntegerValue(10));

        Transaction t = stm.startTransaction();
        IntegerValue value = t.read(originator);
        value.inc();
        assertTrue(value.isDirty());
    }
}
