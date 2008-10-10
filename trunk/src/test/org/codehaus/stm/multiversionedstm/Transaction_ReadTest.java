package org.codehaus.stm.multiversionedstm;

import org.codehaus.stm.IllegalPointerException;
import org.codehaus.stm.IllegalVersionException;
import org.codehaus.stm.AbstractTransactionTest;
import org.codehaus.stm.multiversionedstm.examples.Person;
import org.codehaus.stm.transaction.Transaction;

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
            transaction.readRoot(ptr);
            fail();
        } catch (IllegalPointerException ex) {
        }
        assertTransactionIsActive();
    }

    public void testOnlyTooNewVersionExist() {
        createActiveTransaction();

        //let a different transaction insert a new person.. this person should not be visible to
        //the current transaction.
        Transaction previousTransaction = stm.startTransaction();
        Person p = new Person();
        previousTransaction.attachRoot(p);
        previousTransaction.commit();

        try {
            transaction.readRoot(p.___getPointer());
            fail();
        } catch (IllegalVersionException ex) {
        }

        assertTransactionIsActive();
    }

    public void testReadFromPreviousComittedTransaction() {
        int age = 32;
        String name = "peter";

        long ptr = createPersonUnderOwnTransaction(name, age);

        createActiveTransaction();
        Object found = transaction.readRoot(ptr);
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
        int age = 32;
        long ptr = createPersonUnderOwnTransaction(name, age);

        createActiveTransaction();
        updateAgeUnderOwnTransaction(ptr, age + 1);
        Person p = (Person) transaction.readRoot(ptr);
        assertEquals(age, p.getAge());
        assertTransactionHasNoWrites();
    }

    private void updateAgeUnderOwnTransaction(long ptr, int newage) {
        MultiversionedStm.MultiversionedTransaction t = stm.startTransaction();
        Person person = (Person) t.readRoot(ptr);
        person.setAge(newage);
        t.commit();
    }

    private long createPersonUnderOwnTransaction(String name, int age) {
        Transaction t = stm.startTransaction();
        Person p = new Person();
        p.setAge(age);
        p.setName(name);
        t.attachRoot(p);
        t.commit();
        return p.___getPointer();
    }

    public void testRereadSameInstance() {
        long ptr = createPersonUnderOwnTransaction("peter", 32);

        createActiveTransaction();
        Object found1 = transaction.readRoot(ptr);
        Object found2 = transaction.readRoot(ptr);
        assertNotNull(found1);
        assertSame(found1, found2);
        assertTransactionHasNoWrites();
    }

    public void testReadWhileRolledback() {
        createAbortedTransaction();

        try {
            transaction.readRoot(1);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertTransactionHasNoWrites();
        assertTransactionIsAborted();
    }

    public void testReadWhileComitted() {
        createCommittedTransaction();

        try {
            transaction.readRoot(1);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertTransactionHasNoWrites();
        assertTransactionIsCommitted();
    }
}
