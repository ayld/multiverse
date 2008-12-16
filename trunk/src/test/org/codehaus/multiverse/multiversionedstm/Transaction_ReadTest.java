package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.core.NoSuchObjectException;
import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.multiversionedstm.examples.Person;

public class Transaction_ReadTest extends AbstractMultiversionedStmTest {

    public void testNegativePointer() {
        assertIllegalHandle(-1);
        assertTransactionHasNoReadsFromHeap();
    }

    public void testNullHandle() {
        createActiveTransaction();

        Object result = transaction.read(0);
        assertNull(result);
        assertTransactionIsActive();
        assertTransactionHasNoReadsFromHeap();
    }

    public void testNonExistingHandle() {
        assertIllegalHandle(10000);
    }

    private void assertIllegalHandle(long ptr) {
        createActiveTransaction();
        try {
            transaction.read(ptr);
            fail();
        } catch (NoSuchObjectException ex) {
        }
        assertTransactionIsActive();
        assertTransactionHasNoReadsFromHeap();
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
        assertTransactionHasNoReadsFromHeap();
    }

    public void testReadFromPreviousComittedTransaction() {
        int age = 32;
        String name = "peter";

        long handle = atomicInsertPerson(name, age);

        createActiveTransaction();
        Object found = transaction.read(handle);

        assertNotNull(found);
        assertTrue(found instanceof Person);
        Person foundPerson = (Person) found;
        assertEquals(32, foundPerson.getAge());
        assertEquals("peter", foundPerson.getName());
        assertNull(foundPerson.getParent());
        assertTransactionHasNoWrites();
        assertTransactionReadsFromHeap(1);
    }

    public void testUpdatesByLaterTransactionsAreNotSeen() {
        String name = "peter";
        int oldAge = 32;
        long ptr = atomicInsertPerson(name, oldAge);

        createActiveTransaction();
        atomicIncAge(ptr, oldAge + 1);

        Person p = (Person) transaction.read(ptr);
        assertEquals(oldAge, p.getAge());
        assertTransactionHasNoWrites();
    }

    public void testRereadSameInstance() {
        long handle = atomicInsertPerson("peter", 33);

        createActiveTransaction();
        Object found1 = transaction.read(handle);
        Object found2 = transaction.read(handle);
        assertNotNull(found1);
        assertSame(found1, found2);
        assertTransactionHasNoWrites();
        assertTransactionReadsFromHeap(1);
    }

    public void testReadWhileAborted() {
        createAbortedTransaction();

        try {
            transaction.read(1);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertTransactionHasNoWrites();
        assertTransactionIsAborted();
        assertTransactionHasNoReadsFromHeap();
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
        assertTransactionHasNoReadsFromHeap();
    }
}
