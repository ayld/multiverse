package org.codehaus.stm.multiversionedstm2;

import org.codehaus.stm.IllegalPointerException;
import org.codehaus.stm.multiversionedstm2.examples.Stack;

public class Transaction_ReadRootTest extends AbstractTransactionalTest {

    public void testIllegalPointer() {
        createActiveTransaction();
        long version = stm.getActiveVersion();

        try {
            transaction.readRoot(-1);
            fail();
        } catch (IllegalPointerException ex) {
        }

        assertTransactionIsActive();
        assertActiveStmVersion(version);
    }

    public void testNonExistingRoot() {
        createActiveTransaction();
        long version = stm.getActiveVersion();

        try {
            transaction.readRoot(10);
            fail();
        } catch (IllegalPointerException ex) {
        }

        assertTransactionIsActive();
        assertActiveStmVersion(version);
    }

    public void testPreviousCommittedRoot() {
        long ptr = atomicInsert(new Stack());
        createActiveTransaction();
        long version = stm.getActiveVersion();

        Object object = transaction.readRoot(ptr);

        assertTransactionIsActive();
        assertTrue(object instanceof Stack);
        assertActiveStmVersion(version);
    }

    public void testFreshRoot() {
        createActiveTransaction();
        long version = stm.getActiveVersion();
        Stack stack = new Stack();
        long ptr = transaction.attachRoot(stack);
        Object found = transaction.readRoot(ptr);

        assertSame(stack, found);
        assertTransactionIsActive();
        assertActiveStmVersion(version);
    }

    public void testTransactionAlreadyAborted() {
        long ptr = atomicInsert(new Stack());
        createCommittedTransaction();
        long version = stm.getActiveVersion();

        try {
            transaction.readRoot(ptr);
        } catch (IllegalStateException ex) {
        }

        assertTransactionIsCommitted();
        assertActiveStmVersion(version);
        //todo: stm state
    }

    public void testTransactionAlreadyCommitted() {
        long ptr = atomicInsert(new Stack());
        createCommittedTransaction();
        long version = stm.getActiveVersion();

        try {
            transaction.readRoot(ptr);
        } catch (IllegalStateException ex) {
        }

        assertTransactionIsCommitted();
        assertActiveStmVersion(version);
        //todo: stm state
    }
}
