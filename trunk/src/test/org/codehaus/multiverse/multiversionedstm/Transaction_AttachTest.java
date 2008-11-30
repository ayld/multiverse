package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.multiversionedstm.examples.Person;
import org.codehaus.multiverse.transaction.Transaction;
import org.codehaus.multiverse.transaction.BadTransactionException;

public class Transaction_AttachTest extends AbstractMultiversionedStmTest {

    public void testNull() {
        createActiveTransaction();

        try {
            transaction.attachAsRoot(null);
            fail();
        } catch (NullPointerException ex) {
        }

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
    }

    public void testNonCitizen() {
        createActiveTransaction();
        try {
            transaction.attachAsRoot(new String("foo"));
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
        long ptr = transaction.attachAsRoot(freshPerson);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasPointerAndTransaction(freshPerson, ptr, transaction);
    }

    public void testAttachOfDehydratedCitizen() {
        long ptr = atomicInsert(new Person());

        createActiveTransaction();
        Person dehydratedPerson = (Person) transaction.read(ptr);
        transaction.attachAsRoot(dehydratedPerson);

        assertTransactionIsActive();
        assertHasPointerAndTransaction(dehydratedPerson, ptr, transaction);
    }

    public void testReferenceIsMadeAfterAttach() {
        createActiveTransaction();

        Person child = new Person();
        long childPtr = transaction.attachAsRoot(child);

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
        long ptr1 = transaction.attachAsRoot(person);
        long ptr2 = transaction.attachAsRoot(person);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertEquals("multiple attachAsRoot should give the same content for the same object ", ptr1, ptr2);
        assertHasPointer(ptr1, person);
    }

    public void testShallowCycle() {
        createActiveTransaction();

        Person person = new Person();
        person.setParent(person);

        long personPtr = transaction.attachAsRoot(person);
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

        long person4Ptr = transaction.attachAsRoot(person4);

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

        long ptr = transaction.attachAsRoot(child);

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
        long parentPtr = transaction.attachAsRoot(parent);

        Person child = new Person();
        child.setParent(parent);
        long childPtr = transaction.attachAsRoot(child);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasPointerAndTransaction(child, childPtr, transaction);
        assertHasPointerAndTransaction(parent, parentPtr, transaction);
    }

    public void testMultipleFreshAndUnreferencedItems() {
        createActiveTransaction();

        Person person1 = new Person();
        Person person2 = new Person();

        long person1Ptr = transaction.attachAsRoot(person1);
        long person2Ptr = transaction.attachAsRoot(person2);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasPointerAndTransaction(person1, person1Ptr, transaction);
        assertHasPointerAndTransaction(person2, person2Ptr, transaction);
    }

    public void testAlreadyAttachedToDifferentTransaction() {
        Transaction otherTransaction = stm.startTransaction();
        Person person = new Person();
        long personPtr = otherTransaction.attachAsRoot(person);

        createActiveTransaction();

        try {
            transaction.attachAsRoot(person);
            fail();
        } catch (BadTransactionException ex) {
        }

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasPointerAndTransaction(person, personPtr, otherTransaction);
    }

    public void testReachableObjectAttachedToDifferentTransaction() {
        Transaction otherTransaction = stm.startTransaction();
        Person parent = new Person();
        long parentPtr = otherTransaction.attachAsRoot(parent);

        createActiveTransaction();
        Person child = new Person();
        child.setParent(parent);
        try {
            transaction.attachAsRoot(child);
            fail();
        } catch (BadTransactionException ex) {
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
        long parentPtr = otherTransaction.attachAsRoot(parent);

        createActiveTransaction();
        Person child = new Person();
        long childPtr=    transaction.attachAsRoot(child);

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
            transaction.attachAsRoot(obj);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertTransactionIsAborted();
        assertTransactionHasNoWrites();
        assertStmActiveVersion(version);
        assertHasPointerAndTransaction(obj, 0, null);
    }

    public void testTransactionIsComitted() {
        createCommittedTransaction();
        long version = stm.getActiveVersion();

        Person object = new Person();
        try {
            transaction.attachAsRoot(object);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertTransactionIsCommitted();
        assertTransactionHasNoWrites();
        assertStmActiveVersion(version);
        assertHasPointerAndTransaction(object, 0, null);
    }
}
