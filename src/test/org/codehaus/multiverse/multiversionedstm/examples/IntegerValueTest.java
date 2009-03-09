package org.codehaus.multiverse.multiversionedstm.examples;

import static org.codehaus.multiverse.TestUtils.commit;
import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class IntegerValueTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void testFreshObjectIsDirty() {
        IntegerValue value = new IntegerValue();
        assertTrue(value.___isDirtyIgnoringStmMembers());
    }

    @Test
    public void testFreshCommitedObjectIsNotDirty() {
        Transaction t = stm.startTransaction();
        IntegerValue value = new IntegerValue(10);
        t.attachAsRoot(value);
        t.commit();

        assertFalse(value.___isDirtyIgnoringStmMembers());
    }

    @Test
    public void testLoadedObjectIsNotDirty() {
        long handle = commit(stm, new IntegerValue(10));

        Transaction t = stm.startTransaction();
        IntegerValue value = (IntegerValue) t.read(handle);
        assertFalse(value.___isDirtyIgnoringStmMembers());
    }

    @Test
    public void testLoadedObjectThatIsUpdatedIsDirty() {
        long handle = commit(stm, new IntegerValue(10));

        Transaction t = stm.startTransaction();
        IntegerValue value = (IntegerValue) t.read(handle);
        value.inc();
        assertTrue(value.___isDirtyIgnoringStmMembers());
    }
}
