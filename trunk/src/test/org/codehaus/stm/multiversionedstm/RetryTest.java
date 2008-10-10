package org.codehaus.stm.multiversionedstm;

import org.codehaus.stm.multiversionedstm.examples.Stack;
import org.codehaus.stm.transaction.RetryException;
import org.codehaus.stm.transaction.Transaction;
import org.codehaus.stm.transaction.TransactionStatus;

public class RetryTest extends AbstractMultiversionedStmTest {

    public void testPopFromEmptyStack() {
        long address = atomicInsert(new Stack());

        Transaction t = stm.startTransaction();
        Stack stack = (Stack) t.readRoot(address);

        try {
            stack.pop();
            fail();
        } catch (RetryException ex) {
        }

        assertEquals(TransactionStatus.active, t.getStatus());
    }
}
