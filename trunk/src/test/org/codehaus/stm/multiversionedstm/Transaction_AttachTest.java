package org.codehaus.stm.multiversionedstm;

import org.codehaus.stm.multiversionedstm.examples.Person;
import org.codehaus.stm.transaction.Transaction;

public class Transaction_AttachTest extends AbstractTransactionTest {

    public void testNull() {
        createActiveTransaction();

        try {
            transaction.attach(null);
            fail();
        } catch (NullPointerException ex) {
        }

        assertTransactionActive();
        assertTransactionHasNoWrites();
    }

    public void testNonCitizen() {
        createActiveTransaction();
        try {
            transaction.attach(new String("foo"));
            fail();
        } catch (IllegalArgumentException ex) {
        }

        assertTransactionActive();
        assertTransactionHasNoWrites();
        //todo: check that the current Transaction has not been damaged
    }

    public void testFresh() {
        createActiveTransaction();

        Person freshPerson = new Person();
        transaction.attach(freshPerson);

        assertTransactionActive();
        assertTransactionHasNoWrites();
        assertHasTransaction(transaction, freshPerson);
        assertHasPointer(0, freshPerson);
    }

    public void testAttachOfDehydratedObject() {
        long ptr = insert(new Person());

        createActiveTransaction();
        Person dehydratedPerson = (Person) transaction.read(ptr);
        transaction.attach(dehydratedPerson);

        assertTransactionActive();
        assertHasTransaction(transaction, dehydratedPerson);
        assertHasPointer(ptr, dehydratedPerson);
    }

    public void testReferenceIsMadeAfterAttach() {
        createActiveTransaction();

        Person child = new Person();
        transaction.attach(child);

        Person parent = new Person();
        child.setParent(parent);

        assertTransactionActive();
        assertTransactionHasNoWrites();

        assertHasTransaction(transaction, child, parent);
        assertHasPointer(0, child, parent);
    }

    public void testSameObjectAttachedMultipleTimes() {
        createActiveTransaction();

        Person person = new Person();
        transaction.attach(person);
        transaction.attach(person);

        assertTransactionActive();
        assertTransactionHasNoWrites();
        assertHasPointer(0, person);
    }

    public void testShallowCycle() {
        createActiveTransaction();

        Person person = new Person();
        person.setParent(person);

        transaction.attach(person);
        assertTransactionActive();
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

        transaction.attach(person4);

        assertTransactionActive();
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

        transaction.attach(child);
        assertTransactionActive();
        assertTransactionHasNoWrites();
        assertHasPointer(0, child, parent, grandparent);
        assertHasTransaction(transaction, child, parent, grandparent);
    }

    public void testIndirectReferenceAlreadyAttached() {
        createActiveTransaction();

        Person parent = new Person();
        transaction.attach(parent);

        Person child = new Person();
        child.setParent(parent);
        transaction.attach(child);

        assertTransactionActive();
        assertTransactionHasNoWrites();
        assertHasPointer(0, child, parent);
        assertHasTransaction(transaction, child, parent);
    }

    public void testMultipleFreshAndUnreferencedItems() {
        createActiveTransaction();

        Person person1 = new Person();
        Person person2 = new Person();

        transaction.attach(person1);
        transaction.attach(person2);

        assertTransactionActive();
        assertTransactionHasNoWrites();
        assertHasPointer(0, person1, person2);
        assertHasTransaction(transaction, person1, person2);
    }

    public void testAlreadyAttachedToDifferentTransaction() {
        Transaction otherTransaction = stm.startTransaction();
        Person person = new Person();
        otherTransaction.attach(person);

        createActiveTransaction();

        try {
            transaction.attach(person);
            fail();
        } catch (IllegalStateException ex) {

        }
        assertTransactionActive();
        assertTransactionHasNoWrites();
        assertHasPointer(0, person);
        assertHasTransaction(transaction, person);
    }

    public void testReachableObjectAttachedToDifferentTransaction() {
        Transaction otherTransaction = stm.startTransaction();
        Person parent = new Person();
        otherTransaction.attach(parent);

        createActiveTransaction();
        Person child = new Person();
        child.setParent(parent);
        try {
            transaction.attach(child);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertTransactionActive();
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
            transaction.attach(obj);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertTransactionAborted();
        assertTransactionHasNoWrites();
        assertNull(obj.___getTransaction());
        assertEquals(0, obj.___getPointer());
    }

    public void testTransactionIsComitted() {
        createCommittedTransaction();

        Person object = new Person();
        try {
            transaction.attach(object);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertTransactionComitted();
        assertTransactionHasNoWrites();
        assertNull(object.___getTransaction());
        assertEquals(0, object.___getPointer());
    }
}
