package org.multiverse.api;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.DummyTransaction;

public class TransactionThreadLocalTest {

    @Before
    public void setUp() {
        TransactionThreadLocal.remove();
    }

    @Test
    public void testGetInitial() {
        Transaction t = TransactionThreadLocal.getTransaction();
        assertNull(t);
    }

    @Test
    public void testGetAfterInitialization() {
        Transaction t = new DummyTransaction();
        TransactionThreadLocal.set(t);

        Transaction found = TransactionThreadLocal.getTransaction();
        assertSame(t, found);
    }

    @Test
    public void testSetOfNullTransactionShouldFail() {
        Transaction oldTransaction = new DummyTransaction();
        TransactionThreadLocal.set(oldTransaction);

        try {
            TransactionThreadLocal.set(null);
            fail();
        } catch (NullPointerException ex) {

        }

        Transaction found = TransactionThreadLocal.getTransaction();
        assertSame(oldTransaction, found);
    }

    @Test
    public void testNestedSetShouldFail() {
        Transaction oldTransaction = new DummyTransaction();
        TransactionThreadLocal.set(oldTransaction);

        try {
            TransactionThreadLocal.set(new DummyTransaction());
            fail();
        } catch (IllegalStateException ex) {

        }

        Transaction found = TransactionThreadLocal.getTransaction();
        assertSame(oldTransaction, found);
    }

    @Test
    public void testClean() {
        Transaction oldTransaction = new DummyTransaction();
        TransactionThreadLocal.set(oldTransaction);

        TransactionThreadLocal.remove();

        Transaction found = TransactionThreadLocal.getTransaction();
        assertNull(found);
    }

    @Test
    public void testCleanOfNullTransactionIsIgnored() {
        TransactionThreadLocal.remove();
        TransactionThreadLocal.remove();

        Transaction found = TransactionThreadLocal.getTransaction();
        assertNull(found);
    }
}
