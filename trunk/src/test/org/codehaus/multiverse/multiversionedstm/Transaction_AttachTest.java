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

    //=====================================

    public void testAttachOfDifferentInstanceOfTheSameStmobject() {
        //todo
    }

    // =================== fresh objects ========================

    public void testFreshImmutableStmObject() {
        createActiveTransaction();

        IntegerConstant integerConstant = new IntegerConstant(10);
        long handle = transaction.attachAsRoot(integerConstant);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasHandle(integerConstant, handle);
    }

    public void testFreshMutableStmObject() {
        createActiveTransaction();

        Person freshPerson = new Person();
        long handle = transaction.attachAsRoot(freshPerson);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasHandle(freshPerson, handle);
    }

    // =================== non fresh objects (so previous committed) ========================

    public void testNonFreshImmutableObject() {
        IntegerConstant integerConstant = new IntegerConstant(10);
        long handle = atomicInsert(integerConstant);

        createActiveTransaction();
        IntegerConstant found = (IntegerConstant) transaction.read(handle);
        transaction.attachAsRoot(found);

        assertTransactionIsActive();
        assertHasHandle(integerConstant, handle);
    }

    public void testNonFreshMutableObject() {
        long handle = atomicInsert(new Person());

        createActiveTransaction();
        Person dehydratedPerson = (Person) transaction.read(handle);
        transaction.attachAsRoot(dehydratedPerson);

        assertTransactionIsActive();
        assertHasHandle(dehydratedPerson, handle);
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

        assertHasHandle(child, childHandle);
    }

    public void testReferenceIsMadeAfterAttach() {
        createActiveTransaction();

        Person child = new Person();
        long childHandle = transaction.attachAsRoot(child);

        Person parent = new Person();
        child.setParent(parent);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();

        assertHasHandle(child, childHandle);
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
        assertHasHandle(person, personHandle);
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
        assertHasHandle(child, handle);
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
        assertHasHandle(child, childHandle);
        assertHasHandle(parent, parentHandle);
    }

    public void testMultipleFreshAndUnreferencedItems() {
        createActiveTransaction();

        Person person1 = new Person();
        Person person2 = new Person();

        long person1Handle = transaction.attachAsRoot(person1);
        long person2Handle = transaction.attachAsRoot(person2);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasHandle(person1, person1Handle);
        assertHasHandle(person2, person2Handle);
    }

    // ===================== transaction conflicts ===================================

    public void _testMutableObjectAlreadyAttachedToDifferentTransaction() {
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
        assertHasHandle(person, personHandle);
    }

    public void testImmutableObjectsAttachedToDifferentTransactions() {
        IntegerConstant integerConstant = new IntegerConstant(25);

        Transaction otherTransaction = stm.startTransaction();
        long handle = otherTransaction.attachAsRoot(integerConstant);

        createActiveTransaction();

        transaction.attachAsRoot(integerConstant);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();
        assertHasHandle(integerConstant, handle);
    }

    public void testMutableObjectReachableObjectAttachedToDifferentTransaction() {
        Transaction otherTransaction = stm.startTransaction();
        Person parent = new Person();
        long parentHandle = otherTransaction.attachAsRoot(parent);

        createActiveTransaction();
        Person child = new Person();
        child.setParent(parent);

        transaction.attachAsRoot(child);

        assertTransactionIsActive();
        assertTransactionHasNoWrites();

        assertHasHandle(parent, parentHandle);
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
    }
}
