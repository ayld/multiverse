package org.codehaus.multiverse.core;

import junit.framework.TestCase;

public class TransactionThreadLocalTest extends TestCase {

    public void setUp() {
        TransactionThreadLocal.remove();
    }

    public void testGetInitial() {
        Transaction t = TransactionThreadLocal.get();
        assertNull(t);
    }

    public void testGetAfterInitialization() {
        Transaction t = new DummyTransaction();
        TransactionThreadLocal.set(t);

        Transaction found = TransactionThreadLocal.get();
        assertSame(t, found);
    }

    public void testSetOfNullTransactionShouldFail() {
        Transaction oldTransaction = new DummyTransaction();
        TransactionThreadLocal.set(oldTransaction);

        try {
            TransactionThreadLocal.set(null);
            fail();
        } catch (NullPointerException ex) {

        }

        Transaction found = TransactionThreadLocal.get();
        assertSame(oldTransaction, found);
    }

    public void testNestedSetShouldFail() {
        Transaction oldTransaction = new DummyTransaction();
        TransactionThreadLocal.set(oldTransaction);

        try {
            TransactionThreadLocal.set(new DummyTransaction());
            fail();
        } catch (IllegalStateException ex) {

        }

        Transaction found = TransactionThreadLocal.get();
        assertSame(oldTransaction, found);
    }

    public void testClean() {
        Transaction oldTransaction = new DummyTransaction();
        TransactionThreadLocal.set(oldTransaction);

        TransactionThreadLocal.remove();

        Transaction found = TransactionThreadLocal.get();
        assertNull(found);
    }

    public void testCleanOfNullTransactionIsIgnored() {
        TransactionThreadLocal.remove();
        TransactionThreadLocal.remove();

        Transaction found = TransactionThreadLocal.get();
        assertNull(found);
    }
}
