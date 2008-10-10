package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.multiversionedstm.examples.Person;
import org.codehaus.multiverse.transaction.Transaction;

public class Transaction_AttachTest extends AbstractMultiversionedStmTest {

    public void testNull() {
        createActiveTransaction();

        try {
            transaction.attachRoot(null);
            fail();
        } catch (NullPointerException ex) {
        }

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
    }

    public void testNonCitizen() {
        createActiveTransaction();
        try {
            transaction.attachRoot(new String("foo"));
            fail();
        } catch (IllegalArgumentException ex) {
        }

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        //todo: check that the current Transaction has not been damaged
    }

    public void testFresh() {
        createActiveTransaction();

        Person freshPerson = new Person();
        transaction.attachRoot(freshPerson);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasTransaction(transaction, freshPerson);
        assertHasPointer(0, freshPerson);
    }

    public void testAttachOfDehydratedObject() {
        long ptr = atomicInsert(new Person());

        createActiveTransaction();
        Person dehydratedPerson = (Person) transaction.readRoot(ptr);
        transaction.attachRoot(dehydratedPerson);

        assertTransactionIsActive();
        assertHasTransaction(transaction, dehydratedPerson);
        assertHasPointer(ptr, dehydratedPerson);
    }

    public void testReferenceIsMadeAfterAttach() {
        createActiveTransaction();

        Person child = new Person();
        transaction.attachRoot(child);

        Person parent = new Person();
        child.setParent(parent);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();

        assertHasTransaction(transaction, child, parent);
        assertHasPointer(0, child, parent);
    }

    public void testSameObjectAttachedMultipleTimes() {
        createActiveTransaction();

        Person person = new Person();
        transaction.attachRoot(person);
        transaction.attachRoot(person);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasPointer(0, person);
    }

    public void testShallowCycle() {
        createActiveTransaction();

        Person person = new Person();
        person.setParent(person);

        transaction.attachRoot(person);
        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasPointer(0, person);
        assertHasTransaction(transaction, person);
    }

    public void testDeepCycle() {
        createActiveTransaction();

        Person person1 = new Person();
        Person person2 = new Person();
        person2.setParent(person1);
        Person person3 = new Person();
        person3.setParent(person2);
        Person person4 = new Person();
        person4.setParent(person3);

        transaction.attachRoot(person4);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasPointer(0, person1, person2, person3, person4);
        assertHasTransaction(transaction, person1, person2, person3, person4);
    }

    public void testFreshChain() {
        createActiveTransaction();

        Person grandparent = new Person();
        Person parent = new Person();
        parent.setParent(grandparent);
        Person child = new Person();
        child.setParent(parent);

        transaction.attachRoot(child);
        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasPointer(0, child, parent, grandparent);
        assertHasTransaction(transaction, child, parent, grandparent);
    }

    public void testIndirectReferenceAlreadyAttached() {
        createActiveTransaction();

        Person parent = new Person();
        transaction.attachRoot(parent);

        Person child = new Person();
        child.setParent(parent);
        transaction.attachRoot(child);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasPointer(0, child, parent);
        assertHasTransaction(transaction, child, parent);
    }

    public void testMultipleFreshAndUnreferencedItems() {
        createActiveTransaction();

        Person person1 = new Person();
        Person person2 = new Person();

        transaction.attachRoot(person1);
        transaction.attachRoot(person2);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasPointer(0, person1, person2);
        assertHasTransaction(transaction, person1, person2);
    }

    public void testAlreadyAttachedToDifferentTransaction() {
        Transaction otherTransaction = stm.startTransaction();
        Person person = new Person();
        otherTransaction.attachRoot(person);

        createActiveTransaction();

        try {
            transaction.attachRoot(person);
            fail();
        } catch (IllegalStateException ex) {

        }
        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasPointer(0, person);
        assertHasTransaction(transaction, person);
    }

    public void testReachableObjectAttachedToDifferentTransaction() {
        Transaction otherTransaction = stm.startTransaction();
        Person parent = new Person();
        otherTransaction.attachRoot(parent);

        createActiveTransaction();
        Person child = new Person();
        child.setParent(parent);
        try {
            transaction.attachRoot(child);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasPointer(0, parent, child);
        assertHasTransaction(otherTransaction, parent);
        assertHasTransaction(transaction, child);

        //todo: testen dat de parent bij het comitten gaat zeuren dat die aan een verkeerde transactie zit
    }

    //================ the other states a transaction can be in

    public void testTransactionIsRolledback() {
        createAbortedTransaction();

        Person obj = new Person();
        try {
            transaction.attachRoot(obj);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertTransactionIsAborted();
        assertTransactionHasNoWrites();
        assertNull(obj.___getTransaction());
        assertEquals(0, obj.___getPointer());
    }

    public void testTransactionIsComitted() {
        createCommittedTransaction();

        Person object = new Person();
        try {
            transaction.attachRoot(object);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertTransactionIsCommitted();
        assertTransactionHasNoWrites();
        assertNull(object.___getTransaction());
        assertEquals(0, object.___getPointer());
    }
}
