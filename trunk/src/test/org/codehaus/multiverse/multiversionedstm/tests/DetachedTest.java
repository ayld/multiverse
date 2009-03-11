package org.codehaus.multiverse.multiversionedstm.tests;

import static org.codehaus.multiverse.TestUtils.commit;
import org.codehaus.multiverse.api.Transaction;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import org.codehaus.multiverse.multiversionedstm.examples.IntegerValue;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class DetachedTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void test_Detach_Update_Reattach() {
        IntegerValue original = new IntegerValue(10);
        long handle = commit(stm, original);

        original.inc();

        Transaction t = stm.startTransaction();
        t.attachAsRoot(original);
        t.commit();

        assertValue(handle, 11);
    }

    @Test
    public void test_Detach_Reattach_Update() {
        IntegerValue original = new IntegerValue(10);
        long handle = commit(stm, original);

        Transaction t = stm.startTransaction();
        t.attachAsRoot(original);
        original.inc();
        t.commit();

        assertValue(handle, 11);
    }

    public void assertValue(long handle, int expectedValue) {
        Transaction t = stm.startTransaction();
        IntegerValue integerValue = (IntegerValue) t.read(handle);
        assertEquals(expectedValue, integerValue.get());
        t.commit();
    }
}
