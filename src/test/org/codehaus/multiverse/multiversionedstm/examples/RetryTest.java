package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.core.RetryError;
import org.codehaus.multiverse.multiversionedstm.AbstractMultiversionedStmTest;

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
