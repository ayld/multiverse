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
        long ptr = transaction.attachRoot(freshPerson);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasTransaction(transaction, freshPerson);
        assertHasPointer(ptr, freshPerson);
    }

    public void testAttachOfDehydratedCitizen() {
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
        long childPtr = transaction.attachRoot(child);

        Person parent = new Person();
        child.setParent(parent);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();

        assertHasTransaction(transaction, child);
        assertHasPointer(childPtr, child);

        assertHasNoTransaction(parent);
        assertHasPointer(0, parent);
    }

    public void testSameObjectAttachedMultipleTimes() {
        createActiveTransaction();

        Person person = new Person();
        long ptr1 = transaction.attachRoot(person);
        long ptr2 = transaction.attachRoot(person);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertEquals("multiple attachRoot should give the same value for the same object ",ptr1,ptr2);
        assertHasPointer(ptr1, person);
    }

    public void testShallowCycle() {
        createActiveTransaction();

        Person person = new Person();
        person.setParent(person);

        long personPtr = transaction.attachRoot(person);
        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasPointer(personPtr, person);
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

        long person4Ptr = transaction.attachRoot(person4);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasPointer(person4Ptr, person4);
        //todo: person2 ptr
        //todo: person3 ptr
        //todo: person1 ptr
        assertHasTransaction(transaction, person1, person2, person3, person4);
    }

    public void testFreshChain() {
        createActiveTransaction();

        Person grandparent = new Person();
        Person parent = new Person();
        parent.setParent(grandparent);
        Person child = new Person();
        child.setParent(parent);

        long ptr = transaction.attachRoot(child);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasPointer(ptr, child);
        //todo: ptr parent
        //todo: ptr grandparent
        assertHasTransaction(transaction, child, parent, grandparent);
    }

    public void testIndirectReferenceAlreadyAttached() {
        createActiveTransaction();

        Person parent = new Person();
        long parentPtr = transaction.attachRoot(parent);

        Person child = new Person();
        child.setParent(parent);
        long childPtr = transaction.attachRoot(child);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasPointer(parentPtr, parent);
        assertHasPointer(childPtr, child);
        assertHasTransaction(transaction, child, parent);
    }

    public void testMultipleFreshAndUnreferencedItems() {
        createActiveTransaction();

        Person person1 = new Person();
        Person person2 = new Person();

        long person1Ptr = transaction.attachRoot(person1);
        long person2Ptr = transaction.attachRoot(person2);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasPointer(person1Ptr, person1);
        assertHasPointer(person2Ptr, person2);
        assertHasTransaction(transaction, person1, person2);
    }

    public void testAlreadyAttachedToDifferentTransaction() {
        Transaction otherTransaction = stm.startTransaction();
        Person person = new Person();
        long personPtr = otherTransaction.attachRoot(person);

        createActiveTransaction();

        try {
            transaction.attachRoot(person);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasPointer(personPtr, person);
        assertHasTransaction(otherTransaction, person);
    }

    public void testReachableObjectAttachedToDifferentTransaction() {
        Transaction otherTransaction = stm.startTransaction();
        Person parent = new Person();
        long parentPtr = otherTransaction.attachRoot(parent);

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

        assertHasPointer(0, child);
        assertHasTransaction(null, child);

        assertHasPointer(parentPtr, parent);
        assertHasTransaction(otherTransaction, parent);
        //todo: testen dat de parent bij het comitten gaat zeuren dat die aan een verkeerde transactie zit
    }

    //================ the other states a transaction can be in

    public void testTransactionIsRolledback() {
        createAbortedTransaction();
        long version = stm.getActiveVersion();

        Person obj = new Person();
        try {
            transaction.attachRoot(obj);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertTransactionIsAborted();
        assertTransactionHasNoWrites();
        assertActiveStmVersion(version);

        assertNull(obj.___getTransaction());
        assertEquals(0, obj.___getPointer());
    }

    public void testTransactionIsComitted() {
        createCommittedTransaction();
        long version = stm.getActiveVersion();

        Person object = new Person();
        try {
            transaction.attachRoot(object);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertTransactionIsCommitted();
        assertTransactionHasNoWrites();
        assertActiveStmVersion(version);

        assertNull(object.___getTransaction());
        assertEquals(0, object.___getPointer());
    }
}
