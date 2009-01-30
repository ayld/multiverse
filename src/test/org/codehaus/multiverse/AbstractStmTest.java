package org.codehaus.multiverse;

import junit.framework.TestCase;
import org.codehaus.multiverse.core.NoSuchObjectException;
import org.codehaus.multiverse.core.Stm;
import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.core.TransactionStatus;

public abstract class AbstractStmTest<S extends Stm, T extends Transaction> extends TestCase {

    protected S stm;
    protected T transaction;


    @Override
    public void setUp() throws Exception {
        super.setUp();
        stm = createStm();
    }

    public abstract S createStm();

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
        transaction = (T) stm.startTransaction();
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


    public void assertNoObjectInStm(long ptr) {
        Transaction t = stm.startTransaction();
        try {
            Object x = t.read(ptr);
            fail();
        } catch (NoSuchObjectException ex) {
        } finally {
            t.commit();
        }
    }

    public long atomicInsert(Object obj) {
        Transaction t = stm.startTransaction();
        try {
            long ptr = t.attachAsRoot(obj);
            t.commit();
            return ptr;
        } catch (RuntimeException ex) {
            t.abort();
            throw ex;
        }
    }
}
