package org.codehaus.stm.multiversionedstm2;

import org.codehaus.stm.transaction.TransactionStatus;

public interface Transaction {

    long attachRoot(Object root);

    Citizen readRoot(long rootPtr);

    void deleteRoot(long rootPtr);

    void abort();

    void commit();

    TransactionStatus getStatus();
}
