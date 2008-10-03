package org.codehaus.stm.multiversionedstm;

import org.codehaus.stm.transaction.TransactionStatus;

public abstract class AbstractTransactionTest extends AbstractStmTest {

    protected MultiversionedStm.MultiversionedTransaction transaction;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        transaction = null;
    }

    public void assertTransactionStatus(TransactionStatus status) {
        assertEquals(status, transaction.getStatus());
    }

    public void assertTransactionComitted() {
        assertTransactionStatus(TransactionStatus.committed);
    }

    public void assertTransactionActive() {
        assertTransactionStatus(TransactionStatus.active);
    }

    public void assertTransactionAborted() {
        assertTransactionStatus(TransactionStatus.aborted);
    }

    public void assertTransactionHasNoWrites() {
        assertEquals(0, transaction.getNumberOfWrites());
    }

    public void assertTransactionNumberOfWrites(long expected) {
        assertEquals(expected, transaction.getNumberOfWrites());
    }

    protected void createActiveTransaction() {
        transaction = stm.startTransaction();
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

    public void assertStmVersionHasNotChanged() {
        assertEquals(transaction.getVersion(), stm.getActiveVersion());
    }    
}


