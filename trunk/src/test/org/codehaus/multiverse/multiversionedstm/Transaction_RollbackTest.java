package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.multiversionedstm.examples.Person;

public class Transaction_RollbackTest extends AbstractMultiversionedStmTest {

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
        long version = stm.getActiveVersion();

        createActiveTransaction();
        transaction.attachRoot(p1);
        transaction.attachRoot(p2);
        transaction.abort();

        assertCurrentStmVersion(version);
        assertTransactionIsAborted();
        //assertEquals(0, p1.___getPointer());
        //assertEquals(0, p2.___getPointer());
        assertTransactionHasNoWrites();
    }

    public void testChangedMadeOnPrivatedObjectsAreNotCommitted() {
        long ptr = atomicInsert(new Person());
        long version = stm.getActiveVersion();

        createActiveTransaction();
        Person p = (Person) transaction.readRoot(ptr);
        p.setAge(10);
        p.setName("John Doe");
        p.setParent(new Person());
        transaction.abort();

        assertCurrentStmVersion(version);
        long newVersion = stm.getActiveVersion();
        assertHeapContains(ptr, newVersion, new Person.DehydratedPerson(0, null, 0L));
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
