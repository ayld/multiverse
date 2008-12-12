package org.codehaus.multiverse;

import org.codehaus.multiverse.core.TransactionStatus;
import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.core.Stm;
import org.codehaus.multiverse.AbstractStmTest;

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


