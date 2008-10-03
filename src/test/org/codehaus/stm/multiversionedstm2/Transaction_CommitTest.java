package org.codehaus.stm.multiversionedstm2;

import org.codehaus.stm.multiversionedstm2.examples.Stack;

public class Transaction_CommitTest extends AbstractTransactionalTest {

    public void testFreshTransaction() {
        long version = stm.getActiveVersion();
        createActiveTransaction();

        transaction.commit();

        assertTransactionIsCommitted();
        assertActiveStmVersion(version);
        //todo: stm state controleren
    }

    public void testFreshCitizens() {
        long version = stm.getActiveVersion();
        createActiveTransaction();
        Stack stack = new Stack();
        long ptr = transaction.attachRoot(stack);

        transaction.commit();

        assertTransactionIsCommitted();
        assertActiveStmVersion(version + 1);
        //todo: stm state controleren
    }

    public void testNonFreshCitizensButNoWrites(){
        long ptr = atomicInsert(new Stack());
        long version = stm.getActiveVersion();
        createActiveTransaction();
        Stack stack = (Stack)transaction.readRoot(ptr);
        transaction.commit();

        assertTransactionIsCommitted();
        assertActiveStmVersion(version);
        //todo: inhoud stack nog controleren
    }

    public void testNoWriteConflicts() {
        long ptr = atomicInsert(new Stack());
        long version = stm.getActiveVersion();
        createActiveTransaction();
        Stack stack = (Stack)transaction.readRoot(ptr);
        stack.push("Peter");
        transaction.commit();

        assertTransactionIsCommitted();
        assertActiveStmVersion(version+1);
        //todo: inhoud van stack nog controleren
    }

    public void testWriteConflicts() {
        //todo
    }

    public void testAlreadyCommitted() {
        createCommittedTransaction();
        long version = stm.getActiveVersion();

        transaction.commit();

        assertTransactionIsCommitted();
        assertActiveStmVersion(version);
        //todo: stm state controleren
    }

    public void testAlreadyAborted() {
        createAbortedTransaction();
        long version = stm.getActiveVersion();

        try {
            transaction.commit();
            fail();
        } catch (IllegalStateException ex) {
        }

        assertTransactionIsAborted();
        assertActiveStmVersion(version);
        //todo: stm state controleren
    }
}
