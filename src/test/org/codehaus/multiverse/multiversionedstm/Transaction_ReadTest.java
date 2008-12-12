package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.core.NoSuchObjectException;
import org.codehaus.multiverse.multiversionedstm.examples.Person;
import org.codehaus.multiverse.core.Transaction;

public class Transaction_ReadTest extends AbstractMultiversionedStmTest {

    public void testNegativePointer(){
        assertIllegalPointer(-1);
    }

    public void testNullPointer(){
        assertIllegalPointer(0);
    }

    public void testNonExistingPtr() {
        assertIllegalPointer(10000);
    }

    private void assertIllegalPointer(long ptr){
        createActiveTransaction();
        try {
            transaction.read(ptr);
            fail();
        } catch (NoSuchObjectException ex) {
        }
        assertTransactionIsActive();
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
    }

    public void testReadFromPreviousComittedTransaction() {
        int age = 32;
        String name = "peter";

        long ptr = atomicInsertPerson(name, age);

        createActiveTransaction();
        Object found = transaction.read(ptr);
        assertNotNull(found);
        assertTrue(found instanceof Person);
        Person foundPerson = (Person) found;
        assertEquals(32, foundPerson.getAge());
        assertEquals("peter", foundPerson.getName());
        assertNull(foundPerson.getParent());
        assertTransactionHasNoWrites();
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

    private void atomicIncAge(long ptr, int newage) {
        MultiversionedStm.MultiversionedTransaction t = stm.startTransaction();
        Person person = (Person) t.read(ptr);
        person.setAge(newage);
        t.commit();
    }

    private long atomicInsertPerson(String name, int age) {
        Transaction t = stm.startTransaction();
        Person p = new Person();
        p.setAge(age);
        p.setName(name);
        t.attachAsRoot(p);
        t.commit();
        return p.___getHandle();
    }

    public void testRereadSameInstance() {
        long ptr = atomicInsertPerson("peter", 32);

        createActiveTransaction();
        Object found1 = transaction.read(ptr);
        Object found2 = transaction.read(ptr);
        assertNotNull(found1);
        assertSame(found1, found2);
        assertTransactionHasNoWrites();
    }

    public void testReadWhileRolledback() {
        createAbortedTransaction();

        try {
            transaction.read(1);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertTransactionHasNoWrites();
        assertTransactionIsAborted();
    }

    public void testReadWhileComitted() {
        createCommittedTransaction();

        try {
            transaction.read(1);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertTransactionHasNoWrites();
        assertTransactionIsCommitted();
    }
}
