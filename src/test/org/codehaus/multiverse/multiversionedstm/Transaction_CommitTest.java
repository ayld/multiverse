package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.core.BadTransactionException;
import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.core.WriteConflictException;
import org.codehaus.multiverse.multiversionedstm.examples.Person;

public class Transaction_CommitTest extends AbstractMultiversionedStmTest {

    public void testNoLoadedObjects() {
        createActiveTransaction();
        long version = stm.getCurrentVersion();
        transaction.commit();

        assertTransactionIsCommitted();
        assertCommitCount(1);
        assertAbortedCount(0);

        assertTransactionHasNoWrites();
        assertCurrentStmVersion(version);
    }

    public void testFreshObject_noAccessHasBeenMade() {
        createActiveTransaction();
        long version = stm.getCurrentVersion();

        Person person = new Person();
        long personPtr = transaction.attachAsRoot(person);
        transaction.commit();

        assertTransactionIsCommitted();
        assertCommitCount(1);
        assertAbortedCount(0);

        assertTransactionWriteCount(1);
        assertCurrentStmVersion(version + 1);
        long newVersion = stm.getCurrentVersion();
        assertHasHandle(personPtr, person);
        assertHeapContainsNow(personPtr, newVersion, person.___dehydrate());
    }

    public void testFreshObject_withReadOnStandardFields() {
        createActiveTransaction();
        long oldVersion = stm.getCurrentVersion();

        String name = "peter";
        int age = 32;
        Person person = new Person(age, name);
        person.getAge();
        person.getName();
        transaction.attachAsRoot(person);
        transaction.commit();

        assertTransactionIsCommitted();
        assertTransactionWriteCount(1);
        assertCurrentStmVersion(oldVersion + 1);
        assertCommitCount(1);
        assertAbortedCount(0);
        long newVersion = stm.getCurrentVersion();
        assertHeapContainsNow(person.___getHandle(), newVersion, new Person.DehydratedPerson(person));
    }

    public void testFreshObject_withWriteOnStandardField() {
        createActiveTransaction();
        long initialVersion = stm.getCurrentVersion();

        Person person = new Person();
        transaction.attachAsRoot(person);
        int age = 100;
        person.setAge(age);
        transaction.commit();

        assertTransactionIsCommitted();
        assertTransactionWriteCount(1);
        assertCommitCount(1);
        assertAbortedCount(0);
        long afterCommitVersion = initialVersion + 1;
        assertCurrentStmVersion(afterCommitVersion);
        assertHeapContainsNow(person.___getHandle(), afterCommitVersion, new Person.DehydratedPerson(person));
    }

    public void testChainOfFreshObjects() {
        createActiveTransaction();
        long initialVersion = stm.getCurrentVersion();

        Person parent = new Person();
        Person child = new Person();
        child.setParent(parent);

        transaction.attachAsRoot(child);
        transaction.commit();

        assertTransactionIsCommitted();
        assertTransactionWriteCount(2);
        long afterCommitVersion = initialVersion + 1;
        assertCurrentStmVersion(afterCommitVersion);
        assertHeapContainsNow(child.___getHandle(), afterCommitVersion, new Person.DehydratedPerson(child));
        assertHeapContainsNow(parent.___getHandle(), afterCommitVersion, new Person.DehydratedPerson(parent));
        assertCommitCount(1);
        assertAbortedCount(0);
    }

    public void testFreshObject_DirectCycleShouldNotCrachSystem() {
        createActiveTransaction();
        long initialVersion = stm.getCurrentVersion();

        Person person = new Person();
        transaction.attachAsRoot(person);
        int age = 100;
        person.setAge(age);
        person.setParent(person);
        transaction.commit();

        assertTransactionIsCommitted();
        assertTransactionWriteCount(1);
        long afterCommitVersion = initialVersion + 1;
        assertCurrentStmVersion(afterCommitVersion);
        assertHeapContainsNow(person.___getHandle(), afterCommitVersion, new Person.DehydratedPerson(person));
        assertCommitCount(1);
        assertAbortedCount(0);
    }

    public void testFreshObjects_IndirectCycleShouldNotCrashSystem() {
        createActiveTransaction();
        long initialVersion = stm.getCurrentVersion();

        Person grandparent = new Person();
        Person parent = new Person();
        parent.setParent(grandparent);
        Person child = new Person();
        child.setParent(parent);
        grandparent.setParent(child);//this cause the cycle

        transaction.attachAsRoot(child);
        transaction.commit();

        assertTransactionIsCommitted();
        assertTransactionWriteCount(3);
        long afterCommitVersion = initialVersion + 1;
        assertCurrentStmVersion(afterCommitVersion);
        assertHeapContainsNow(grandparent.___getHandle(), afterCommitVersion, new Person.DehydratedPerson(grandparent));
        assertHeapContainsNow(parent.___getHandle(), afterCommitVersion, new Person.DehydratedPerson(parent));
        assertHeapContainsNow(child.___getHandle(), afterCommitVersion, new Person.DehydratedPerson(child));
        assertCommitCount(1);
        assertAbortedCount(0);
    }

    public void testPrivatizedObjectWithReadOnStandardMember() {
        Person person = new Person();
        long handle = atomicInsert(person);

        long initialVersion = stm.getCurrentVersion();

        createActiveTransaction();
        Person p1 = (Person) transaction.read(handle);
        p1.getName();
        p1.getParent();
        transaction.commit();

        assertTransactionIsCommitted();
        assertTransactionHasNoWrites();
        assertCurrentStmVersion(initialVersion);
        assertHeapContainsNow(p1.___getHandle(), initialVersion, new Person.DehydratedPerson(person));
    }

    public void testPrivatizedObjectWithReadOnStmMember() {
        Person person = new Person();
        Person parent = new Person();
        person.setParent(parent);
        long handle = atomicInsert(person);

        long initialVersion = stm.getCurrentVersion();

        createActiveTransaction();
        Person p1 = (Person) transaction.read(handle);
        p1.getParent();
        transaction.commit();

        assertTransactionIsCommitted();
        assertTransactionHasNoWrites();
        assertCurrentStmVersion(initialVersion);
        assertHeapContainsNow(p1.___getHandle(), initialVersion, new Person.DehydratedPerson(person));
    }

    public void testReadObjectWithWriteOnStandardMember() {
        int oldAge = 10;
        String name = "peter";
        long handle = atomicInsert(new Person(oldAge, name));

        long initialVersion = stm.getCurrentVersion();

        createActiveTransaction();
        Person p1 = (Person) transaction.read(handle);
        int newAge = oldAge + 1;
        p1.setAge(newAge);
        transaction.commit();

        assertTransactionIsCommitted();
        assertTransactionWriteCount(1);
        long afterCommitVersion = initialVersion + 1;
        assertCurrentStmVersion(afterCommitVersion);

        assertHeapContains(handle, initialVersion, new Person.DehydratedPerson(handle, oldAge, name));
        assertHeapContainsNow(handle, afterCommitVersion, new Person.DehydratedPerson(handle, newAge, name));
    }

    public void testReadObjectWithWriteOnStmMember() {
        long personHandle = atomicInsert(new Person());
        long parentHandle = atomicInsert(new Person());

        long initialVersion = stm.getCurrentVersion();

        createActiveTransaction();
        Person person = (Person) transaction.read(personHandle);
        Person parent = (Person) transaction.read(parentHandle);
        person.setParent(parent);
        transaction.commit();

        assertTransactionIsCommitted();
        assertTransactionWriteCount(1);
        long afterCommitVersion = initialVersion + 1;
        assertCurrentStmVersion(afterCommitVersion);

        assertHeapContains(parentHandle, initialVersion, new Person.DehydratedPerson(parentHandle, 0, null));
        assertHeapContainsNow(personHandle, afterCommitVersion, new Person.DehydratedPerson(personHandle, 0, null, parentHandle));
    }

    public void testStartedAndConflictingWrite() {
        Person person = new Person();
        long ptr = atomicInsert(person);

        createActiveTransaction();
        Person p1 = (Person) transaction.read(ptr);

        MultiversionedStm.MultiversionedTransaction interferingTransaction = atomicIncAge(ptr);

        long commitCount = stm.getStatistics().getTransactionsCommitedCount();

        p1.setAge(p1.getAge() + 10);
        try {
            transaction.commit();
            fail();
        } catch (WriteConflictException ex) {
        }

        assertTransactionIsAborted();
        assertTransactionHasNoWrites();
        assertHeapContainsNow(ptr, interferingTransaction.getVersion() + 1, new Person.DehydratedPerson(person.___getHandle(), 1, null));
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

    public void testReachableObjectIsConnectedToDifferentTransaction() {
        Transaction otherTransaction = stm.startTransaction();
        Person parent = new Person();
        long parentPtr = otherTransaction.attachAsRoot(parent);

        createActiveTransaction();
        Person child = new Person();
        long childPtr = transaction.attachAsRoot(child);

        child.setParent(parent);

        try {
            transaction.commit();
            fail();
        } catch (BadTransactionException ex) {

        }

        assertTransactionIsAborted();
        assertTransactionHasNoWrites();

        assertHasHandleAndTransaction(child, childPtr, transaction);
        assertHasHandleAndTransaction(parent, parentPtr, otherTransaction);
    }

    //=================================================================

    public void testComittedTransaction() {
        createCommittedTransaction();
        long currentVersion = stm.getCurrentVersion();

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
