package org.codehaus.multiverse.multiversionedstm.tests;

import org.codehaus.multiverse.core.RetryError;
import org.codehaus.multiverse.multiversionedstm.AbstractMultiversionedStmTest;
import org.codehaus.multiverse.multiversionedstm.examples.Stack;

public class RetryTest extends AbstractMultiversionedStmTest {

    public void testPopFromEmptyStack() {
        long address = atomicInsert(new Stack());

        createActiveTransaction();
        Stack stack = (Stack) transaction.read(address);

        try {
            stack.pop();
            fail();
        } catch (RetryError ex) {
        }

        assertTransactionIsActive();
    }
}
