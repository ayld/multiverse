package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.multiversionedstm.examples.Stack;
import org.codehaus.multiverse.transaction.RetryException;
import org.codehaus.multiverse.transaction.Transaction;
import org.codehaus.multiverse.transaction.TransactionStatus;

public class RetryTest extends AbstractMultiversionedStmTest {

    public void testPopFromEmptyStack() {
        long address = atomicInsert(new Stack());

        Transaction t = stm.startTransaction();
        Stack stack = (Stack) t.read(address);

        try {
            stack.pop();
            fail();
        } catch (RetryException ex) {
        }

        assertEquals(TransactionStatus.active, t.getStatus());
    }
}
