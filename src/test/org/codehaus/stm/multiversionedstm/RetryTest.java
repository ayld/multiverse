package org.codehaus.stm.multiversionedstm;

import org.codehaus.stm.multiversionedstm.examples.Stack;
import org.codehaus.stm.transaction.RetryException;
import org.codehaus.stm.transaction.Transaction;
import org.codehaus.stm.transaction.TransactionStatus;
import org.codehaus.stm.Stm;
import junit.framework.TestCase;

public class RetryTest extends AbstractStmTest {

    public void testPopFromEmptyStack(){
        long address = insert(new Stack());

        Transaction t = stm.startTransaction();
        Stack stack = (Stack)t.read(address);

        try{
            stack.pop();
            fail();
        }catch(RetryException ex){
        }

        assertEquals(TransactionStatus.active, t.getStatus());
    }


    
}
