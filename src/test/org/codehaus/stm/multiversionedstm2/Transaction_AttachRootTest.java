package org.codehaus.stm.multiversionedstm2;

import org.codehaus.stm.multiversionedstm2.examples.Stack;

import java.util.Vector;

public class Transaction_AttachRootTest extends AbstractTransactionalTest {

    public void testNull() {
        createActiveTransaction();

        try {
            transaction.attachRoot(null);
            fail();
        } catch (NullPointerException ex) {

        }

        assertTransactionIsActive();
    }

    public void testNonCitizen() {
        createActiveTransaction();

        try {
            transaction.attachRoot(new Vector());
            fail();
        } catch (IllegalArgumentException ex) {
        }

        assertTransactionIsActive();
    }

    public void testSuccess() {
        createActiveTransaction();

        Stack stack = new Stack();
        transaction.attachRoot(stack);
        //todo, nog testen dat hij gecommit gaat worden

        assertTransactionIsActive();
    }

    public void testFreshCitizenIsAttachedMultipleTimes() {
        createActiveTransaction();

        Stack stack = new Stack();
        long ptr1 = transaction.attachRoot(stack);
        long ptr2 = transaction.attachRoot(stack);
        //todo, nog testen dat hij gecommit gaat worden

        assertSame(ptr1, ptr2);
        assertSame(stack, transaction.readRoot(ptr1));
        assertTransactionIsActive();
    }

    public void testCitizenIsNotFresh() {
        long origPtr = atomicInsert(new Stack());

        createActiveTransaction();
        Stack stack = (Stack)transaction.readRoot(origPtr);
        long foundptr = transaction.attachRoot(stack);

        assertTransactionIsActive();
        assertSame(origPtr, foundptr);
    }

    public void testWhileAborted() {
        createAbortedTransaction();

        try {
            transaction.attachRoot(new Stack());
            fail();
        } catch (IllegalStateException ex) {
        }

        assertTransactionIsAborted();
    }

    public void testWhileCommitted() {
        createCommittedTransaction();

        try {
            transaction.attachRoot(new Stack());
            fail();
        } catch (IllegalStateException ex) {
        }

        assertTransactionIsCommitted();
    }
}
