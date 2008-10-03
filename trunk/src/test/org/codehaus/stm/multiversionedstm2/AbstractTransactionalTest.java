package org.codehaus.stm.multiversionedstm2;

import junit.framework.TestCase;
import org.codehaus.stm.IllegalPointerException;
import org.codehaus.stm.transaction.TransactionStatus;

public abstract class AbstractTransactionalTest extends TestCase {
    protected MultiversionedStm stm;
    protected Transaction transaction;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        stm = new MultiversionedStm();
    }

    public void sleepRandom(long ms){
        sleep((long)(Math.random()*ms));
    }

    public void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Thread.interrupted();
            throw new RuntimeException(ex);
        }
    }
    

    public void assertActiveStmVersion(long expected){
        assertEquals(expected, stm.getActiveVersion());
    }

    public Citizen atomicRead(long ptr) {
        Transaction t = stm.startTransaction();
        try {
            return t.readRoot(ptr);
        } finally {
            t.abort();
        }
    }

    public void assertNoObjectInStm(long ptr) {
        Transaction t = stm.startTransaction();
        try {
            try {
                t.readRoot(ptr);
                fail();
            } catch (IllegalPointerException ex) {
            }
        } finally {
            t.abort();
        }
    }

    public long atomicInsert(Object object) {
        Transaction t = stm.startTransaction();
        long ptr = t.attachRoot(object);
        t.commit();
        return ptr;
    }

    public void createActiveTransaction() {
        transaction = stm.startTransaction();
    }

    public void createCommittedTransaction() {
        createActiveTransaction();
        transaction.commit();
        assertTransactionIsCommitted();
    }

    public void createAbortedTransaction() {
        createActiveTransaction();
        transaction.abort();
        assertTransactionIsAborted();
    }

    public void assertTransactionIsActive() {
        assertTransactionStatus(TransactionStatus.active);
    }

    public void assertTransactionIsAborted() {
        assertTransactionStatus(TransactionStatus.aborted);
    }

    public void assertTransactionIsCommitted() {
        assertTransactionStatus(TransactionStatus.committed);
    }

    public void assertTransactionStatus(TransactionStatus expected) {
        assertNotNull(transaction);
        assertEquals(expected, transaction.getStatus());
    }
}
