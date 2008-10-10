package org.codehaus.multiverse;

import org.codehaus.multiverse.transaction.RetryException;

public final class TransactionMethods {

    public static void retry(){        
        throw new RetryException();
    }

    private TransactionMethods(){}
}
