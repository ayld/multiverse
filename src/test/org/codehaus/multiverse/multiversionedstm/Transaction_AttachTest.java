package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.core.BadTransactionException;
import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.multiversionedstm.examples.IntegerConstant;
import org.codehaus.multiverse.multiversionedstm.examples.Person;

public class Transaction_AttachTest extends AbstractMultiversionedStmTest {

    // ================= testing of arguments ===================

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

    public void testNonStmObject() {
        createActiveTransaction();
        try {
            transaction.attachAsRoot(new String("foo"));
            fail();
        } catch (IllegalArgumentException ex) {
        }

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
    }

    // =================== fresh objects ========================

    public void testFreshImmutableStmObject() {
        createActiveTransaction();

        IntegerConstant integerConstant = new IntegerConstant(10);
        long handle = transaction.attachAsRoot(integerConstant);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasHandleAndTransaction(integerConstant, handle, null);
    }

    public void testFreshMutableStmObject() {
        createActiveTransaction();

        Person freshPerson = new Person();
        long handle = transaction.attachAsRoot(freshPerson);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasHandleAndTransaction(freshPerson, handle, transaction);
    }

    // =================== non fresh objects (so previous committed) ========================

    public void testNonFreshMutableObject() {
        long handle = atomicInsert(new Person());

        createActiveTransaction();
        Person dehydratedPerson = (Person) transaction.read(handle);
        transaction.attachAsRoot(dehydratedPerson);

        assertTransactionIsActive();
        assertHasHandleAndTransaction(dehydratedPerson, handle, transaction);
    }

    public void testNonFreshImmutableObject() {
        IntegerConstant integerConstant = new IntegerConstant(10);
        long handle = atomicInsert(integerConstant);

        createActiveTransaction();
        IntegerConstant found = (IntegerConstant) transaction.read(handle);
        transaction.attachAsRoot(found);

        assertTransactionIsActive();
        assertHasHandleAndTransaction(integerConstant, handle, null);
    }

    // ====================================================

    public void testReferenceIsMadeBeforeAttach() {
        createActiveTransaction();

        Person child = new Person();
        Person parent = new Person();
        child.setParent(parent);

        long childHandle = transaction.attachAsRoot(child);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();

        assertHasHandleAndTransaction(child, childHandle, transaction);
        assertHasHandleAndTransaction(parent, transaction);
    }

    public void testReferenceIsMadeAfterAttach() {
        createActiveTransaction();

        Person child = new Person();
        long childHandle = transaction.attachAsRoot(child);

        Person parent = new Person();
        child.setParent(parent);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();

        assertHasHandleAndTransaction(child, childHandle, transaction);
        assertHasTransaction(null, parent);
    }

    // ===================== multiple attaches of same object ==================

    public void testFreshMutableObjectAttachedMultipleTimes() {
        createActiveTransaction();

        Person person = new Person();
        long handle1 = transaction.attachAsRoot(person);
        long handle2 = transaction.attachAsRoot(person);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertEquals("multiple attachAsRoot should give the same handle for the same object ", handle1, handle2);
        assertHasHandle(handle1, person);
    }

    public void testFreshImmutableObjectAttachedMultipleTimes() {
        createActiveTransaction();

        IntegerConstant integerConstant = new IntegerConstant(10);
        long handle1 = transaction.attachAsRoot(integerConstant);
        long handle2 = transaction.attachAsRoot(integerConstant);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertEquals("multiple attachAsRoot should give the same handle for the same object ", handle1, handle2);
        assertHasHandle(handle1, integerConstant);
    }

    // ==================== cycles and chains ===========================

    public void testShallowCycle() {
        createActiveTransaction();

        Person person = new Person();
        person.setParent(person);

        long personHandle = transaction.attachAsRoot(person);
        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasHandleAndTransaction(person, personHandle, transaction);
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

        long person4Handle = transaction.attachAsRoot(person4);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasHandle(person4Handle, person4);
        assertHasTransaction(transaction, person1, person2, person3, person4);
    }

    public void testFreshChain() {
        createActiveTransaction();

        Person grandparent = new Person();
        Person parent = new Person();
        parent.setParent(grandparent);
        Person child = new Person();
        child.setParent(parent);

        long handle = transaction.attachAsRoot(child);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasHandleAndTransaction(child, handle, transaction);
        assertHasHandleAndTransaction(parent, transaction);
        assertHasHandleAndTransaction(grandparent, transaction);
        assertHasTransaction(transaction, parent, grandparent);
    }

    public void testIndirectReferenceAlreadyAttached() {
        createActiveTransaction();

        Person parent = new Person();
        long parentHandle = transaction.attachAsRoot(parent);

        Person child = new Person();
        child.setParent(parent);
        long childHandle = transaction.attachAsRoot(child);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasHandleAndTransaction(child, childHandle, transaction);
        assertHasHandleAndTransaction(parent, parentHandle, transaction);
    }

    public void testMultipleFreshAndUnreferencedItems() {
        createActiveTransaction();

        Person person1 = new Person();
        Person person2 = new Person();

        long person1Handle = transaction.attachAsRoot(person1);
        long person2Handle = transaction.attachAsRoot(person2);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasHandleAndTransaction(person1, person1Handle, transaction);
        assertHasHandleAndTransaction(person2, person2Handle, transaction);
    }

    // ===================== transaction conflicts ===================================

    public void testMutableObjectAlreadyAttachedToDifferentTransaction() {
        Transaction otherTransaction = stm.startTransaction();
        Person person = new Person();
        long personHandle = otherTransaction.attachAsRoot(person);

        createActiveTransaction();

        try {
            transaction.attachAsRoot(person);
            fail();
        } catch (BadTransactionException ex) {
        }

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasHandleAndTransaction(person, personHandle, otherTransaction);
    }

    public void testImmutableObjectsAttachedToDifferentTransactions() {
        IntegerConstant integerConstant = new IntegerConstant(25);

        Transaction otherTransaction = stm.startTransaction();
        long handle = otherTransaction.attachAsRoot(integerConstant);

        createActiveTransaction();

        transaction.attachAsRoot(integerConstant);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasHandleAndTransaction(integerConstant, handle, null);
    }

    public void testMutableObjectReachableObjectAttachedToDifferentTransaction() {
        Transaction otherTransaction = stm.startTransaction();
        Person parent = new Person();
        long parentHandle = otherTransaction.attachAsRoot(parent);

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

        assertHasTransaction(null, child);
        assertHasHandleAndTransaction(parent, parentHandle, otherTransaction);
        //todo: testen dat de parent bij het comitten gaat zeuren dat die aan een verkeerde transactie zit
    }

    public void testBadReachableObjectIsSetAfterAttach() {
        Transaction otherTransaction = stm.startTransaction();
        Person parent = new Person();
        long parentHandle = otherTransaction.attachAsRoot(parent);

        createActiveTransaction();
        Person child = new Person();
        long childHandle = transaction.attachAsRoot(child);

        child.setParent(parent);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();

        assertHasHandleAndTransaction(child, childHandle, transaction);
        assertHasHandleAndTransaction(parent, parentHandle, otherTransaction);
    }

    //================ the other states a transaction can be in ====================

    public void testTransactionIsRolledback() {
        createAbortedTransaction();
        long version = stm.getCurrentVersion();

        Person obj = new Person();
        try {
            transaction.attachAsRoot(obj);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertTransactionIsAborted();
        assertTransactionHasNoWrites();
        assertStmActiveVersion(version);
        assertHasTransaction(null, obj);
    }

    public void testTransactionIsComitted() {
        createCommittedTransaction();
        long version = stm.getCurrentVersion();

        Person person = new Person();
        try {
            transaction.attachAsRoot(person);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertTransactionIsCommitted();
        assertTransactionHasNoWrites();
        assertStmActiveVersion(version);
        assertHasTransaction(null, person);
    }
}
