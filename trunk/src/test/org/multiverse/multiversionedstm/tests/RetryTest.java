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
import org.multiverse.multiversionedstm.examples.Stack;

public class RetryTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void testPopFromEmptyStack() {
        Handle<Stack> handle = commit(stm, new Stack());

        Transaction t = stm.startTransaction();
        Stack stack = t.read(handle);

        try {
            stack.pop();
            fail();
        } catch (RetryError ex) {
        }

        assertIsActive(t);
    }
}
