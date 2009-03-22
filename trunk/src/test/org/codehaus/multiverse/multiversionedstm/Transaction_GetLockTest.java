package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.api.PessimisticLock;
import org.codehaus.multiverse.api.Transaction;
import org.codehaus.multiverse.multiversionedstm.examples.Stack;

public class Transaction_GetLockTest extends AbstractMultiversionedStmTest {

    public void testNullObject() {
        Transaction t = stm.startTransaction();

        try {
            t.getPessimisticLock(null);
            fail();
        } catch (NullPointerException ex) {
        }
    }

    public void testGetLockOfPersistedStmObject() {
        long handle = atomicInsert(new Stack());

        createActiveTransaction();
        Stack stack = (Stack) transaction.read(handle);
        PessimisticLock lock = transaction.getPessimisticLock(stack);

        assertNotNull(lock);
        assertTransactionIsActive();
        assertTransactionHasNoWrites();
    }

    public void testGetLockOfNonPersistedStmObjectFails() {
        createActiveTransaction();

        try {
            transaction.getPessimisticLock(new Stack());
            fail();
        } catch (RuntimeException ex) {
            //todo
        }

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
    }

    public void testNoStmObject() {
        createActiveTransaction();

        try {
            transaction.getPessimisticLock("Foo");
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

    public void testWhileAborted() {
        long handle = atomicInsert(new Stack());

        createActiveTransaction();
        Stack stack = (Stack) transaction.read(handle);
        transaction.abort();

        try {
            transaction.getPessimisticLock(stack);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertTransactionIsAborted();
    }

    public void testWhileCommitted() {
        long handle = atomicInsert(new Stack());

        createActiveTransaction();
        Stack stack = (Stack) transaction.read(handle);
        transaction.commit();

        try {
            transaction.getPessimisticLock(stack);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertTransactionIsCommitted();
    }
}
