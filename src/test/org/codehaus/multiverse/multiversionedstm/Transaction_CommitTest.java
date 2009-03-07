package org.codehaus.multiverse.multiversionedstm;

import static org.codehaus.multiverse.TestUtils.commitAll;
import org.codehaus.multiverse.core.BadTransactionException;
import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.core.WriteConflictError;
import org.codehaus.multiverse.multiversionedstm.examples.IntegerConstant;
import org.codehaus.multiverse.multiversionedstm.examples.IntegerValue;
import org.codehaus.multiverse.multiversionedstm.examples.Person;

public class Transaction_CommitTest extends AbstractMultiversionedStmTest {

    // =================   fresh objects without dependencies ============================

    public void testSingleAttachedFreshImmutableObject() {
        createActiveTransaction();
        long oldVersion = stm.getCurrentVersion();

        IntegerConstant constant = new IntegerConstant(10);
        transaction.attachAsRoot(constant);
        transaction.commit();

        assertTransactionIsCommitted();
        assertTransactionWriteCount(1);
        assertCurrentStmVersion(oldVersion + 1);
        assertCommitCount(1);
        assertAbortedCount(0);

        long newVersion = stm.getCurrentVersion();
        assertHeapContainsNow(constant.___getHandle(), newVersion, constant.___deflate(newVersion));
    }

    public void testMultipleAttachedImmutableFreshObjects() {
        createActiveTransaction();
        long oldVersion = stm.getCurrentVersion();

        IntegerConstant constant1 = new IntegerConstant(10);
        IntegerConstant constant2 = new IntegerConstant(20);
        IntegerConstant constant3 = new IntegerConstant(30);
        transaction.attachAsRoot(constant1);
        transaction.attachAsRoot(constant2);
        transaction.attachAsRoot(constant3);
        transaction.commit();

        assertTransactionIsCommitted();
        assertTransactionWriteCount(3);
        assertCurrentStmVersion(oldVersion + 1);
        assertCommitCount(1);
        assertAbortedCount(0);

        long newVersion = stm.getCurrentVersion();
        assertHeapContainsNow(constant1.___getHandle(), newVersion, constant1.___deflate(newVersion));
        assertHeapContainsNow(constant2.___getHandle(), newVersion, constant2.___deflate(newVersion));
        assertHeapContainsNow(constant3.___getHandle(), newVersion, constant3.___deflate(newVersion));
    }

    public void testMultipleAttachedFreshMutableObjects() {
        createActiveTransaction();
        long version = stm.getCurrentVersion();

        IntegerValue a = new IntegerValue(1);
        IntegerValue b = new IntegerValue(10);
        IntegerValue c = new IntegerValue(100);

        transaction.attachAsRoot(a);
        transaction.attachAsRoot(b);
        transaction.attachAsRoot(c);
        transaction.commit();

        assertTransactionIsCommitted();
        assertCommitCount(1);
        assertAbortedCount(0);
        assertTransactionWriteCount(3);
        assertCurrentStmVersion(version + 1);
        long newVersion = stm.getCurrentVersion();
        assertHeapContainsNow(a.___getHandle(), newVersion, a.___deflate(newVersion));
        assertHeapContainsNow(b.___getHandle(), newVersion, b.___deflate(newVersion));
        assertHeapContainsNow(c.___getHandle(), newVersion, c.___deflate(newVersion));
    }

    //====================================================

    public void testFreshMutableObject_noAccessHasBeenMade() {
        createActiveTransaction();
        long version = stm.getCurrentVersion();

        Person person = new Person();
        long personHandle = transaction.attachAsRoot(person);
        transaction.commit();

        assertTransactionIsCommitted();
        assertCommitCount(1);
        assertAbortedCount(0);

        assertTransactionWriteCount(1);
        assertCurrentStmVersion(version + 1);
        long newVersion = stm.getCurrentVersion();
        assertHasHandle(personHandle, person);
        assertHeapContainsNow(personHandle, newVersion, person.___deflate(newVersion));
    }

    public void testFreshMutableObject_withReadOnStandardFields() {
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
        assertHeapContainsNow(person.___getHandle(), newVersion, new Person.DehydratedPerson(person, newVersion));
    }

    public void testFreshMutableObject_withWriteOnStandardField() {
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
        assertHeapContainsNow(person.___getHandle(), afterCommitVersion, new Person.DehydratedPerson(person, afterCommitVersion));
    }

    public void testChainOfFreshMutableObjects() {
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
        assertHeapContainsNow(child.___getHandle(), afterCommitVersion, child.___deflate(afterCommitVersion));
        assertHeapContainsNow(parent.___getHandle(), afterCommitVersion, parent.___deflate(afterCommitVersion));
        assertCommitCount(1);
        assertAbortedCount(0);
    }

    public void testFreshMutableObject_DirectCycleShouldNotCrachSystem() {
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
        assertHeapContainsNow(person.___getHandle(), afterCommitVersion, person.___deflate(afterCommitVersion));
        assertCommitCount(1);
        assertAbortedCount(0);
    }

    public void testFreshMutableObjects_IndirectCycleShouldNotCrashSystem() {
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

        long afterCommitVersion = initialVersion + 1;

        assertTransactionIsCommitted();
        assertTransactionWriteCount(3);
        assertCurrentStmVersion(afterCommitVersion);
        assertHeapContainsNow(grandparent.___getHandle(), afterCommitVersion, grandparent.___deflate(afterCommitVersion));
        assertHeapContainsNow(parent.___getHandle(), afterCommitVersion, parent.___deflate(afterCommitVersion));
        assertHeapContainsNow(child.___getHandle(), afterCommitVersion, child.___deflate(afterCommitVersion));
        assertCommitCount(1);
        assertAbortedCount(0);
    }

    // =================== non fresh

    public void testNonFreshImmutableObject() {
        IntegerConstant constant = new IntegerConstant(10);
        long handle = atomicInsert(constant);

        createActiveTransaction();
        long oldVersion = stm.getCurrentVersion();

        IntegerConstant loadedConstant = (IntegerConstant) transaction.read(handle);
        transaction.attachAsRoot(loadedConstant);
        transaction.commit();

        assertTransactionIsCommitted();
        assertTransactionHasNoWrites();
        assertCurrentStmVersion(oldVersion);

        assertCommitCount(2);
        assertAbortedCount(0);

        long newVersion = stm.getCurrentVersion();
        assertHeapContainsNow(handle, newVersion, constant.___deflate(oldVersion));
    }

    public void testNonFreshMutableObjectThatIsNotUsed() {
        IntegerValue original = new IntegerValue(10);
        long handle = atomicInsert(original);

        long initialVersion = stm.getCurrentVersion();

        createActiveTransaction();
        IntegerValue loaded = (IntegerValue) transaction.read(handle);
        transaction.commit();

        assertTransactionIsCommitted();
        assertTransactionHasNoWrites();
        assertCurrentStmVersion(initialVersion);

        assertHeapContainsNow(handle, initialVersion, original.___deflate(initialVersion));
    }

    public void testNonFreshMutableObjectWithReadOnStandardMember() {
        IntegerValue original = new IntegerValue();
        long handle = atomicInsert(original);

        long initialVersion = stm.getCurrentVersion();

        createActiveTransaction();
        IntegerValue loaded = (IntegerValue) transaction.read(handle);
        loaded.get();
        transaction.commit();

        assertTransactionIsCommitted();
        assertTransactionHasNoWrites();
        assertCurrentStmVersion(initialVersion);

        assertHeapContainsNow(loaded.___getHandle(), initialVersion, original.___deflate(initialVersion));
    }

    public void testNonFreshObjectWithReadOnStandardMember() {
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
        assertHeapContainsNow(person.___getHandle(), initialVersion, person.___deflate(initialVersion));
        assertHeapContainsNow(parent.___getHandle(), initialVersion, parent.___deflate(initialVersion));
    }

    public void testNonFreshMutableObjectWithWriteOnStandardMember() {
        int oldAge = 10;
        String name = "peter";
        Person oringalPerson = new Person(oldAge, name);
        long handle = atomicInsert(oringalPerson);

        long initialVersion = stm.getCurrentVersion();

        createActiveTransaction();
        Person updatedPerson = (Person) transaction.read(handle);
        updatedPerson.incAge();
        transaction.commit();

        long afterCommitVersion = initialVersion + 1;

        assertTransactionIsCommitted();
        assertTransactionWriteCount(1);
        assertCurrentStmVersion(afterCommitVersion);
        assertHeapContains(handle, initialVersion, oringalPerson.___deflate(initialVersion));
        assertHeapContainsNow(handle, afterCommitVersion, updatedPerson.___deflate(afterCommitVersion));
    }

    public void testNonFreshObjectWithWriteOnStmMember() {
        Person person = new Person();
        Person parent = new Person();
        atomicInsert(person);
        atomicInsert(parent);

        long initialVersion = stm.getCurrentVersion();

        createActiveTransaction();
        Person loadedPerson = (Person) transaction.read(person.___getHandle());
        Person loadedParent = (Person) transaction.read(parent.___getHandle());
        loadedPerson.setParent(loadedParent);
        transaction.commit();

        assertTransactionIsCommitted();
        assertTransactionWriteCount(1);
        long afterCommitVersion = initialVersion + 1;
        assertCurrentStmVersion(afterCommitVersion);

        assertHeapContainsNow(loadedPerson.___getHandle(), afterCommitVersion, loadedPerson.___deflate(afterCommitVersion));
    }

    public void testMultipleUpdatedNonFreshObjects() {
        IntegerValue a1 = new IntegerValue(1);
        IntegerValue b1 = new IntegerValue(100);
        IntegerValue c1 = new IntegerValue(100);

        commitAll(stm, a1, b1, c1);

        long initialVersion = stm.getCurrentVersion();

        createActiveTransaction();
        IntegerValue a2 = (IntegerValue) transaction.read(a1.___getHandle());
        IntegerValue b2 = (IntegerValue) transaction.read(b1.___getHandle());
        IntegerValue c2 = (IntegerValue) transaction.read(c1.___getHandle());
        a2.inc();
        b2.inc();
        c2.inc();
        transaction.commit();

        assertTransactionIsCommitted();
        assertTransactionWriteCount(3);
        long afterCommitVersion = initialVersion + 1;
        assertCurrentStmVersion(afterCommitVersion);
        assertHeapContains(a1.___getHandle(), initialVersion, a1.___deflate(initialVersion));
        assertHeapContains(b1.___getHandle(), initialVersion, b1.___deflate(initialVersion));
        assertHeapContains(c1.___getHandle(), initialVersion, c1.___deflate(initialVersion));
        assertHeapContainsNow(a1.___getHandle(), afterCommitVersion, a2.___deflate(afterCommitVersion));
        assertHeapContainsNow(b1.___getHandle(), afterCommitVersion, b2.___deflate(afterCommitVersion));
        assertHeapContainsNow(c1.___getHandle(), afterCommitVersion, c2.___deflate(afterCommitVersion));
    }

    /// ================================= bad transactions ======================

    public void testReachableObjectIsConnectedToDifferentTransaction() {
        Transaction otherTransaction = stm.startTransaction();
        Person parent = new Person();
        long parentHandle = otherTransaction.attachAsRoot(parent);

        createActiveTransaction();
        Person child = new Person();
        long childHandle = transaction.attachAsRoot(child);

        child.setParent(parent);

        try {
            transaction.commit();
            fail();
        } catch (BadTransactionException ex) {
        }

        assertTransactionIsAborted();
        assertTransactionHasNoWrites();

        assertHasHandleAndTransaction(child, childHandle, transaction);
        assertHasHandleAndTransaction(parent, parentHandle, otherTransaction);
        //todo: assert state of heap content
    }

    // ====================== write conflicts ==========================

    public void testConflictingWriteOnConflictingField() {
        Person original = new Person(33, "peter");
        long handle = atomicInsert(original);

        //lets start a transaction
        createActiveTransaction();
        Person p1 = (Person) transaction.read(handle);

        //lets a different transaction interfere.
        Transaction t = stm.startTransaction();
        Person p2 = (Person) t.read(handle);
        p2.incAge();
        t.commit();
        long endVersion = stm.getCurrentVersion();

        //lets continu with the transaction
        long commitCount = stm.getStatistics().getTransactionsCommitedCount();
        p1.incAge();
        try {
            transaction.commit();
            fail();
        } catch (WriteConflictError ex) {
        }

        assertTransactionIsAborted();
        assertTransactionHasNoWrites();
        assertHeapContainsNow(handle, endVersion, p2.___deflate(endVersion));
        assertCommitCount(commitCount);
        assertAbortedCount(1);
    }

    public void testWriteConflict_nonConflictingFieldsAlsoGiveWriteConflict() {
        Person original = new Person(33, "Peter");
        long handle = atomicInsert(original);

        //lets start the transaction
        createActiveTransaction();
        Person p1 = (Person) transaction.read(handle);

        //lets a different transaction interfere and do an update on the age.
        Transaction t = stm.startTransaction();
        Person p2 = (Person) t.read(handle);
        p2.incAge();
        t.commit();
        long endVersion = stm.getCurrentVersion();

        //lets continue with the transaction and do a write an a different field
        long commitCount = stm.getStatistics().getTransactionsCommitedCount();
        p1.setName("John");
        try {
            transaction.commit();
            fail();
        } catch (WriteConflictError ex) {
        }

        assertTransactionIsAborted();
        assertTransactionHasNoWrites();
        assertHeapContainsNow(handle, endVersion, p2.___deflate(endVersion));
        assertCommitCount(commitCount);
        assertAbortedCount(1);
    }

    //todo
    public void testWriteConflict_onReachableObject() {

    }

    //=================== other states ==============================================

    public void testUnusedTransaction() {
        createActiveTransaction();
        long originalVersion = stm.getCurrentVersion();
        transaction.commit();

        assertTransactionIsCommitted();
        assertCommitCount(1);
        assertAbortedCount(0);

        assertTransactionHasNoWrites();
        assertCurrentStmVersion(originalVersion);
    }

    public void testTransactionAlreadyIsCommitted() {
        createCommittedTransaction();
        long currentVersion = stm.getCurrentVersion();

        transaction.commit();

        assertTransactionIsCommitted();
        assertTransactionHasNoWrites();
        assertCurrentStmVersion(currentVersion);
        assertAbortedCount(0);
        assertCommitCount(1);
    }

    public void testTransactionAlreadyIsAborted() {
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
