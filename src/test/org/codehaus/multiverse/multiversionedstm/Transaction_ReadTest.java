package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.core.NoSuchObjectException;
import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.multiversionedstm.examples.IntegerConstant;
import org.codehaus.multiverse.multiversionedstm.examples.Person;

public class Transaction_ReadTest extends AbstractMultiversionedStmTest {

    public void testNegativePointer() {
        assertNoSuchHandle(-1);
        assertTransactionHasNoHydratedObjects();
    }

    public void testNullHandle() {
        createActiveTransaction();

        Object result = transaction.read(0);
        assertNull(result);
        assertTransactionIsActive();
        assertTransactionHasNoHydratedObjects();
    }

    public void testNonExistingHandle() {
        assertNoSuchHandle(10000);
    }

    private void assertNoSuchHandle(long ptr) {
        createActiveTransaction();
        try {
            transaction.read(ptr);
            fail();
        } catch (NoSuchObjectException ex) {
        }
        assertTransactionIsActive();
        assertTransactionHasNoHydratedObjects();
    }

    public void testOnlyTooNewVersionExist() {
        createActiveTransaction();

        //let a different transaction insert a new person.. this person should not be visible to
        //the current transaction.
        Transaction previousTransaction = stm.startTransaction();
        Person p = new Person();
        previousTransaction.attachAsRoot(p);
        previousTransaction.commit();

        try {
            transaction.read(p.___getHandle());
            fail();
        } catch (NoSuchObjectException ex) {
        }

        assertTransactionIsActive();
        assertTransactionHasNoHydratedObjects();
    }

    public void testReadMutableObjectFromPreviousComittedTransaction() {
        int age = 32;
        String name = "peter";
        long handle = atomicInsertPerson(name, age);

        createActiveTransaction();
        Object found = transaction.read(handle);

        assertNotNull(found);
        assertTrue(found instanceof Person);
        Person foundPerson = (Person) found;
        assertEquals(age, foundPerson.getAge());
        assertEquals(name, foundPerson.getName());
        assertNull(foundPerson.getParent());
        assertTransactionHasNoWrites();
        assertTransactionHydratedObjectCount(1);
    }

    public void testReadImmutableObjectFromPreviousComittedTransaction() {
        IntegerConstant integerConstant = new IntegerConstant(10);
        long handle = atomicInsert(integerConstant);

        createActiveTransaction();
        Object found = transaction.read(handle);

        assertSame(integerConstant, found);
        assertTransactionHasNoWrites();
        assertTransactionHydratedObjectCount(0);
    }

    public void testUpdatesOnMutableObjectByLaterTransactionsAreNotSeen() {
        String name = "peter";
        int oldAge = 32;
        long ptr = atomicInsertPerson(name, oldAge);

        createActiveTransaction();
        atomicIncAge(ptr, oldAge + 1);

        Person p = (Person) transaction.read(ptr);
        assertEquals(oldAge, p.getAge());
        assertTransactionHasNoWrites();
    }

    public void testRereadSameMutableInstance() {
        long handle = atomicInsertPerson("peter", 33);

        createActiveTransaction();
        Object found1 = transaction.read(handle);
        Object found2 = transaction.read(handle);
        assertNotNull(found1);
        assertSame(found1, found2);
        assertTransactionHasNoWrites();
        assertTransactionHydratedObjectCount(1);
    }

    public void testRereadSameImmutableMutableInstance() {
        IntegerConstant integerConstant = new IntegerConstant(10);
        long handle = atomicInsert(integerConstant);

        createActiveTransaction();
        Object found1 = transaction.read(handle);
        Object found2 = transaction.read(handle);
        assertNotNull(found1);
        assertSame(found1, found2);
        assertTransactionHasNoWrites();
        assertTransactionHydratedObjectCount(0);
    }


    //=============== other states ======================

    public void testReadWhileAborted() {
        createAbortedTransaction();

        try {
            transaction.read(1);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertTransactionHasNoWrites();
        assertTransactionIsAborted();
        assertTransactionHasNoHydratedObjects();
    }

    public void testReadWhileCommitted() {
        createCommittedTransaction();

        try {
            transaction.read(1);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertTransactionHasNoWrites();
        assertTransactionIsCommitted();
        assertTransactionHasNoHydratedObjects();
    }
}
