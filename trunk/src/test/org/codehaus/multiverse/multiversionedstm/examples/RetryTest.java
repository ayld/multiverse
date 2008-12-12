package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.multiversionedstm.examples.Stack;
import org.codehaus.multiverse.multiversionedstm.AbstractMultiversionedStmTest;
import org.codehaus.multiverse.core.RetryException;

public class RetryTest extends AbstractMultiversionedStmTest {

    public void testPopFromEmptyStack() {
        long address = atomicInsert(new Stack());

        createActiveTransaction();
        Stack stack = (Stack) transaction.read(address);

        try {
            stack.pop();
            fail();
        } catch (RetryException ex) {
        }

        assertTransactionIsActive();
    }
}
