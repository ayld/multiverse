package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.multiversionedstm.examples.Person;

public class Transaction_AbortTest extends AbstractMultiversionedStmTest {

    public void testFreshTransaction() {
        createActiveTransaction();

        transaction.abort();
        assertTransactionIsAborted();
        assertStmVersionHasNotChanged();
        assertTransactionHasNoWrites();
    }

    public void testFreshObjectsAreNotCommitted() {
        Person p1 = new Person();
        Person p2 = new Person();
        long version = stm.getCurrentVersion();

        createActiveTransaction();
        transaction.attachAsRoot(p1);
        transaction.attachAsRoot(p2);
        transaction.abort();

        assertCurrentStmVersion(version);
        assertTransactionIsAborted();
        assertTransactionHasNoWrites();
    }

    public void testChangesAreNotCommitted() {
        long ptr = atomicInsert(new Person());
        long version = stm.getCurrentVersion();

        createActiveTransaction();
        Person p = (Person) transaction.read(ptr);
        p.setAge(10);
        p.setName("John Doe");
        p.setParent(new Person());
        transaction.abort();

        assertCurrentStmVersion(version);
        assertActualVersion(ptr, version);
        assertTransactionHasNoWrites();
    }

    public void testAlreadyRolledback() {
        createAbortedTransaction();
        transaction.abort();
        assertTransactionIsAborted();
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
        assertTransactionIsCommitted();
        assertStmVersionHasNotChanged();
        assertTransactionHasNoWrites();
    }
}
