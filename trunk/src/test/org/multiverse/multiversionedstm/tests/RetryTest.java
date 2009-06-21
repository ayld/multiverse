package org.multiverse.multiversionedstm.tests;

import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.assertIsActive;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.RetryError;
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.multiversionedstm.manualinstrumented.ManualStack;

public class RetryTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void testPopFromEmptyStack() {
        Handle<ManualStack> handle = commit(stm, new ManualStack());

        Transaction t = stm.startTransaction();
        ManualStack stack = t.read(handle);

        try {
            stack.pop();
            fail();
        } catch (RetryError ex) {
        }

        assertIsActive(t);
    }
}
