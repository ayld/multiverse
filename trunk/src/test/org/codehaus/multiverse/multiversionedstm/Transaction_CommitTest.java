package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.multiversionedstm.examples.Person;
import org.codehaus.multiverse.transaction.AbortedException;
import org.codehaus.multiverse.transaction.Transaction;

public class Transaction_CommitTest extends AbstractMultiversionedStmTest {

    public void testNoLoadedObjects() {
        createActiveTransaction();
        long version = stm.getActiveVersion();
        transaction.commit();

        assertTransactionIsCommitted();
        assertCommitCount(1);
        assertAbortedCount(0);

        assertTransactionHasNoWrites();
        assertCurrentStmVersion(version);
    }

    public void testFreshObject_noAccessHasBeenMade() {
        createActiveTransaction();
        long version = stm.getActiveVersion();

        Person person = new Person();
        long personPtr = transaction.attachAsRoot(person);
        transaction.commit();

        assertTransactionIsCommitted();
        assertCommitCount(1);
        assertAbortedCount(0);

        assertTransactionNumberOfWrites(1);
        assertCurrentStmVersion(version + 1);
        long newVersion = stm.getActiveVersion();
        assertHasPointer(personPtr, person);
        assertHeapContains(personPtr, newVersion, person.___dehydrate(0));
    }

    /*
    public void testFreshObject_withReadOnStandardFields() {
        createActiveTransaction();
        long oldVersion = stm.getActiveVersion();

        String name = "peter";
        int age = 32;
        Person person = new Person(age, name);
        person.getAge();
        person.getName();
        transaction.attachAsRoot(person);
        transaction.commit();

        assertTransactionIsCommitted();
        assertTransactionNumberOfWrites(1);
        assertCurrentStmVersion(oldVersion + 1);
        long newVersion = stm.getActiveVersion();
        assertHeapContains(person.___getHandle(), newVersion, new Person.DehydratedPerson(age, name, 0L));
    }

    public void testFreshObject_withWriteOnStandardField() {
        createActiveTransaction();
        long initialVersion = stm.getActiveVersion();

        Person person = new Person();
        transaction.attachAsRoot(person);
        int age = 100;
        person.setAge(age);
        transaction.commit();

        assertTransactionIsCommitted();
        assertTransactionNumberOfWrites(1);
        long afterCommitVersion = initialVersion + 1;
        assertCurrentStmVersion(afterCommitVersion);
        assertHeapContains(person.___getHandle(), afterCommitVersion, new Person.DehydratedPerson(age, null, 0L));
    }

    public void testChainOfFreshObjects() {
        createActiveTransaction();
        long initialVersion = stm.getActiveVersion();

        Person parent = new Person();
        Person child = new Person();
        child.setParent(parent);

        transaction.attachAsRoot(child);
        transaction.commit();

        assertTransactionIsCommitted();
        assertTransactionNumberOfWrites(2);
        long afterCommitVersion = initialVersion + 1;
        assertCurrentStmVersion(afterCommitVersion);
        assertHeapContains(child.___getHandle(), afterCommitVersion, new Person.DehydratedPerson(0, null, parent.___getHandle()));
        assertHeapContains(parent.___getHandle(), afterCommitVersion, new Person.DehydratedPerson(0, null, 0L));
    }

    public void testFreshObject_DirectCycleShouldNotCrachSystem() {
        createActiveTransaction();
        long initialVersion = stm.getActiveVersion();

        Person person = new Person();
        transaction.attachAsRoot(person);
        int age = 100;
        person.setAge(age);
        person.setParent(person);
        transaction.commit();

        assertTransactionIsCommitted();
        assertTransactionNumberOfWrites(1);
        long afterCommitVersion = initialVersion + 1;
        assertCurrentStmVersion(afterCommitVersion);
        assertHeapContains(person.___getHandle(), afterCommitVersion, new Person.DehydratedPerson(age, null, person.___getHandle()));
    }

    public void testFreshObjects_IndirectCycleShouldNotCrashSystem() {
        createActiveTransaction();
        long initialVersion = stm.getActiveVersion();

        Person grandparent = new Person();
        Person parent = new Person();
        parent.setParent(grandparent);
        Person child = new Person();
        child.setParent(parent);
        grandparent.setParent(child);//this cause the cycle

        transaction.attachAsRoot(child);
        transaction.commit();

        assertTransactionIsCommitted();
        assertTransactionNumberOfWrites(3);
        long afterCommitVersion = initialVersion + 1;
        assertCurrentStmVersion(afterCommitVersion);
        assertHeapContains(grandparent.___getHandle(), afterCommitVersion, new Person.DehydratedPerson(0, null, child.___getHandle()));
        assertHeapContains(parent.___getHandle(), afterCommitVersion, new Person.DehydratedPerson(0, null, grandparent.___getHandle()));
        assertHeapContains(child.___getHandle(), afterCommitVersion, new Person.DehydratedPerson(0, null, parent.___getHandle()));
    }

    public void testPrivatizedObjectWithRead() {
        Person p = new Person();
        long ptr = atomicInsert(p);

        long initialVersion = stm.getActiveVersion();

        createActiveTransaction();
        Person p1 = (Person) transaction.read(ptr);
        p1.getName();
        p1.getParent();
        transaction.commit();

        assertTransactionIsCommitted();
        assertTransactionHasNoWrites();
        assertCurrentStmVersion(initialVersion);
        assertHeapContains(p1.___getHandle(), initialVersion, new Person.DehydratedPerson(0, null, 0L));
    }

    public void testPrivatizedObjectWithWrite() {
        int oldAge = 10;
        Person p = new Person();
        p.setAge(oldAge);
        long ptr = atomicInsert(p);

        long initialVersion = stm.getActiveVersion();

        createActiveTransaction();
        Person p1 = (Person) transaction.read(ptr);
        int newAge = oldAge + 1;
        p1.setAge(newAge);
        transaction.commit();

        assertTransactionIsCommitted();
        assertTransactionNumberOfWrites(1);
        long afterCommitVersion = initialVersion + 1;
        assertCurrentStmVersion(afterCommitVersion);
        assertHeapContains(p1.___getHandle(), afterCommitVersion, new Person.DehydratedPerson(newAge, null, 0L));
    }

    public void testStartedAndConflictingWrite() {
        Person person = new Person();
        long ptr = atomicInsert(person);

        createActiveTransaction();
        Person p1 = (Person) transaction.read(ptr);

        MultiversionedStm.MultiversionedTransaction interferingTransaction = atomicIncAge(ptr);

        long commitCount = stm.getCommittedCount();

        p1.setAge(p1.getAge() + 10);
        try {
            transaction.commit();
            fail();
        } catch (AbortedException ex) {
        }

        assertTransactionIsAborted();
        assertTransactionHasNoWrites();
        assertHeapContains(ptr, interferingTransaction.getVersion() + 1, new Person.DehydratedPerson(1, null, 0L));
        assertCommitCount(commitCount);
        assertAbortedCount(1);
    }

    private MultiversionedStm.MultiversionedTransaction atomicIncAge(long personPtr) {
        MultiversionedStm.MultiversionedTransaction transaction = stm.startTransaction();
        Person p2 = (Person) transaction.read(personPtr);
        p2.setAge(p2.getAge() + 1);
        transaction.commit();
        return transaction;
    }

    public void _testReachableObjectIsConnectedToDifferentTransaction() {
        Transaction otherTransaction = stm.startTransaction();
        Person parent = new Person();
        long parentPtr = otherTransaction.attachAsRoot(parent);

        createActiveTransaction();
        Person child = new Person();
        long childPtr = transaction.attachAsRoot(child);

        child.setParent(parent);

        transaction.commit();

        assertTransactionIsAborted();
        assertTransactionHasNoWrites();

        assertHasPointerAndTransaction(child, childPtr, transaction);
        assertHasPointerAndTransaction(parent, parentPtr, otherTransaction);
    }
        */
    //=================================================================

    public void testComittedTransaction() {
        createCommittedTransaction();
        long currentVersion = stm.getActiveVersion();

        transaction.commit();
        assertTransactionIsCommitted();
        assertTransactionHasNoWrites();
        assertCurrentStmVersion(currentVersion);
        assertAbortedCount(0);
        assertCommitCount(1);
    }

    public void testRolledbackTransaction() {
        createAbortedTransaction();

        try {
            transaction.commit();
            fail();
        } catch (IllegalStateException ex) {

        }

        assertTransactionHasNoWrites();
        assertStmVersionHasNotChanged();
        assertCommitCount(0);
        assertAbortedCount(1);
        assertTransactionIsAborted();
    }
}
