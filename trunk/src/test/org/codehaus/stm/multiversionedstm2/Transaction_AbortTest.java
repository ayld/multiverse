package org.codehaus.stm.multiversionedstm2;

import org.codehaus.stm.multiversionedstm2.examples.Stack;

public class Transaction_AbortTest extends AbstractTransactionalTest {

    public void testFreshTransaction() {
        createActiveTransaction();
        long version = stm.getActiveVersion();

        transaction.abort();

        assertTransactionIsAborted();
        assertActiveStmVersion(version);
        //todo: stm state testen
    }

    public void testTransactionWithAttachedRoots() {
        createActiveTransaction();
        long version = stm.getActiveVersion();

        Stack stack = new Stack();
        long ptr = transaction.attachRoot(stack);
        transaction.abort();

        assertTransactionIsAborted();
        assertNoObjectInStm(ptr);
        assertActiveStmVersion(version);
        //todo: stm state testen
    }

    public void testTransactionWithNonDirtyDehydratedRoots(){
        long ptr = atomicInsert(new Stack());

        createActiveTransaction();
        long version = stm.getActiveVersion();

        Stack stack = (Stack)transaction.readRoot(ptr);
        transaction.abort();

        assertTransactionIsAborted();
        assertActiveStmVersion(version);
        //todo: stm content controleren
    }

    public void testTransactionWithDirtyDehydratedRoots(){
        long ptr = atomicInsert(new Stack());
        createActiveTransaction();
        long version = stm.getActiveVersion();

        Stack stack = (Stack)transaction.readRoot(ptr);
        stack.push("foo");
        transaction.abort();

        assertTransactionIsAborted();
        assertActiveStmVersion(version);
        //todo: stm content nog controleren
    }

    public void testTransactionAlreadyIsAborted() {
        createAbortedTransaction();
        long version = stm.getActiveVersion();

        transaction.abort();

        assertTransactionIsAborted();
        assertActiveStmVersion(version);
    }

    public void testTransactionAlreadyIsCommitted() {
        createCommittedTransaction();
        long version = stm.getActiveVersion();

        try {
            transaction.abort();
            fail();
        } catch (IllegalStateException ex) {

        }

        assertTransactionIsCommitted();
        assertActiveStmVersion(version);        
    }
}
