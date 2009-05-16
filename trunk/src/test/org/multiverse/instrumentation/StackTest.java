package org.multiverse.instrumentation;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.collections.Stack;
import org.multiverse.multiversionedstm.MultiversionedStm;

public class StackTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void test() {
        Stack stack = new Stack();
        stack.push("foo");
        stack.push("bar");
        Handle<Stack> handle = commit(stm, stack);

        Transaction t2 = stm.startTransaction();
        Stack foundStack = t2.read(handle);
        assertEquals(stack, foundStack);
    }
}
