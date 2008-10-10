package org.codehaus.stm;

import org.codehaus.stm.transaction.TransactionStatus;
import org.codehaus.stm.transaction.Transaction;
import org.codehaus.stm.AbstractStmTest;
import org.codehaus.stm.multiversionedstm.MultiversionedStm;

public abstract class AbstractTransactionTest<S extends Stm, T extends Transaction> extends AbstractStmTest<S> {

    protected T transaction;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        transaction = null;
    }

    public void assertTransactionStatus(TransactionStatus status) {
        assertEquals(status, transaction.getStatus());
    }

    public void assertTransactionIsCommitted() {
        assertTransactionStatus(TransactionStatus.committed);
    }

    public void assertTransactionIsActive() {
        assertTransactionStatus(TransactionStatus.active);
    }

    public void assertTransactionIsAborted() {
        assertTransactionStatus(TransactionStatus.aborted);
    }

    protected void createActiveTransaction() {
        transaction = (T)stm.startTransaction();
    }

    protected void createAbortedTransaction() {
        createActiveTransaction();
        transaction.abort();
        assertTransactionStatus(TransactionStatus.aborted);
    }

    protected void createCommittedTransaction() {
        createActiveTransaction();
        transaction.commit();
        assertTransactionStatus(TransactionStatus.committed);
    }

    
}


