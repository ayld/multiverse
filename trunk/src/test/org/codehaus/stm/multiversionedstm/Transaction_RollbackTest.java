package org.codehaus.stm.multiversionedstm;

import org.codehaus.stm.multiversionedstm.AbstractTransactionTest;
import org.codehaus.stm.multiversionedstm.examples.Person;

public class Transaction_RollbackTest extends AbstractTransactionTest {

    public void testFreshTransaction() {
        createActiveTransaction();

        transaction.abort();
        assertTransactionAborted();
        assertStmVersionHasNotChanged();
        assertTransactionHasNoWrites();
    }

    public void testFreshObjectsAreNotCommitted() {
        Person p1 = new Person();
        Person p2 = new Person();
        long version = stm.getActiveVersion();

        createActiveTransaction();
        transaction.attach(p1);
        transaction.attach(p2);
        transaction.abort();

        assertCurrentStmVersion(version);
        assertTransactionAborted();
        assertEquals(0, p1.___getPointer());
        assertEquals(0, p2.___getPointer());
        assertTransactionHasNoWrites();
    }

    public void testChangedMadeOnPrivatedObjectsAreNotCommitted() {
        long ptr = insert(new Person());
        long version = stm.getActiveVersion();

        createActiveTransaction();
        Person p = (Person) transaction.read(ptr);
        p.setAge(10);
        p.setName("John Doe");
        p.setParent(new Person());
        transaction.abort();

        assertCurrentStmVersion(version);
        long newVersion = stm.getActiveVersion();
        assertStmContains(ptr, newVersion,new Person.HydratedPerson(0, null, 0L));
        assertTransactionHasNoWrites();
    }

    public void testAlreadyRolledback() {
        createAbortedTransaction();
        transaction.abort();
        assertTransactionAborted();
        assertStmVersionHasNotChanged();
        assertTransactionHasNoWrites();
    }

    public void testAlreadyCommitted() {
        createCommittedTransaction();

        try {
            transaction.abort();
            fail();
        } catch (IllegalStateException ex) {
        }
        assertTransactionComitted();
        assertStmVersionHasNotChanged();
        assertTransactionHasNoWrites();
    }
}
