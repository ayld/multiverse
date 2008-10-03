package org.codehaus.stm;

import org.codehaus.stm.transaction.RetryException;
import org.codehaus.stm.transaction.Transaction;

public final class TransactionMethods {

    public static void retry(){        
        throw new RetryException();
    }

    private TransactionMethods(){}
}
