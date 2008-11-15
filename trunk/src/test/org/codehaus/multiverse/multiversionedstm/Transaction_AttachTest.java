package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.multiversionedstm.examples.Person;
import org.codehaus.multiverse.transaction.Transaction;
import org.codehaus.multiverse.transaction.AttachedToDifferentTransactionException;

public class Transaction_AttachTest extends AbstractMultiversionedStmTest {

    public void testNull() {
        createActiveTransaction();

        try {
            transaction.attach(null);
            fail();
        } catch (NullPointerException ex) {
        }

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
    }

    public void testNonCitizen() {
        createActiveTransaction();
        try {
            transaction.attach(new String("foo"));
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
        long ptr = transaction.attach(freshPerson);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasPointerAndTransaction(freshPerson, ptr, transaction);
    }

    public void testAttachOfDehydratedCitizen() {
        long ptr = atomicInsert(new Person());

        createActiveTransaction();
        Person dehydratedPerson = (Person) transaction.read(ptr);
        transaction.attach(dehydratedPerson);

        assertTransactionIsActive();
        assertHasPointerAndTransaction(dehydratedPerson, ptr, transaction);
    }

    public void testReferenceIsMadeAfterAttach() {
        createActiveTransaction();

        Person child = new Person();
        long childPtr = transaction.attach(child);

        Person parent = new Person();
        child.setParent(parent);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();

        assertHasPointerAndTransaction(child, childPtr, transaction);
        assertHasPointerAndTransaction(parent, 0, null);
    }

    public void testSameObjectAttachedMultipleTimes() {
        createActiveTransaction();

        Person person = new Person();
        long ptr1 = transaction.attach(person);
        long ptr2 = transaction.attach(person);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertEquals("multiple attach should give the same value for the same object ", ptr1, ptr2);
        assertHasPointer(ptr1, person);
    }

    public void testShallowCycle() {
        createActiveTransaction();

        Person person = new Person();
        person.setParent(person);

        long personPtr = transaction.attach(person);
        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasPointerAndTransaction(person, personPtr, transaction);
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

        long person4Ptr = transaction.attach(person4);

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

        long ptr = transaction.attach(child);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasPointerAndTransaction(child, ptr, transaction);
        //todo: ptr parent
        //todo: ptr grandparent
        assertHasTransaction(transaction, parent, grandparent);
    }

    public void testIndirectReferenceAlreadyAttached() {
        createActiveTransaction();

        Person parent = new Person();
        long parentPtr = transaction.attach(parent);

        Person child = new Person();
        child.setParent(parent);
        long childPtr = transaction.attach(child);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasPointerAndTransaction(child, childPtr, transaction);
        assertHasPointerAndTransaction(parent, parentPtr, transaction);
    }

    public void testMultipleFreshAndUnreferencedItems() {
        createActiveTransaction();

        Person person1 = new Person();
        Person person2 = new Person();

        long person1Ptr = transaction.attach(person1);
        long person2Ptr = transaction.attach(person2);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasPointerAndTransaction(person1, person1Ptr, transaction);
        assertHasPointerAndTransaction(person2, person2Ptr, transaction);
    }

    public void testAlreadyAttachedToDifferentTransaction() {
        Transaction otherTransaction = stm.startTransaction();
        Person person = new Person();
        long personPtr = otherTransaction.attach(person);

        createActiveTransaction();

        try {
            transaction.attach(person);
            fail();
        } catch (AttachedToDifferentTransactionException ex) {
        }

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasPointerAndTransaction(person, personPtr, otherTransaction);
    }

    public void testReachableObjectAttachedToDifferentTransaction() {
        Transaction otherTransaction = stm.startTransaction();
        Person parent = new Person();
        long parentPtr = otherTransaction.attach(parent);

        createActiveTransaction();
        Person child = new Person();
        child.setParent(parent);
        try {
            transaction.attach(child);
            fail();
        } catch (AttachedToDifferentTransactionException ex) {
        }

        assertTransactionIsActive();
        assertTransactionHasNoWrites();

        assertHasPointerAndTransaction(child, 0, null);
        assertHasPointerAndTransaction(parent, parentPtr, otherTransaction);
        //todo: testen dat de parent bij het comitten gaat zeuren dat die aan een verkeerde transactie zit
    }

    public void testBadReachableObjectIsSetAfterAttach() {
        Transaction otherTransaction = stm.startTransaction();
        Person parent = new Person();
        long parentPtr = otherTransaction.attach(parent);

        createActiveTransaction();
        Person child = new Person();
        long childPtr=    transaction.attach(child);

        child.setParent(parent);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();

        assertHasPointerAndTransaction(child, childPtr, transaction);
        assertHasPointerAndTransaction(parent, parentPtr, otherTransaction);
    }

    //================ the other states a transaction can be in

    public void testTransactionIsRolledback() {
        createAbortedTransaction();
        long version = stm.getActiveVersion();

        Person obj = new Person();
        try {
            transaction.attach(obj);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertTransactionIsAborted();
        assertTransactionHasNoWrites();
        assertActiveStmVersion(version);
        assertHasPointerAndTransaction(obj, 0, null);
    }

    public void testTransactionIsComitted() {
        createCommittedTransaction();
        long version = stm.getActiveVersion();

        Person object = new Person();
        try {
            transaction.attach(object);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertTransactionIsCommitted();
        assertTransactionHasNoWrites();
        assertActiveStmVersion(version);
        assertHasPointerAndTransaction(object, 0, null);
    }
}
