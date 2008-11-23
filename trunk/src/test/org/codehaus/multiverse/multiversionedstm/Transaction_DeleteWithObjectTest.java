package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.multiversionedstm.examples.Person;
import org.codehaus.multiverse.multiversionedstm.examples.Stack;
import org.codehaus.multiverse.transaction.BadTransactionException;
import org.codehaus.multiverse.transaction.Transaction;

public class Transaction_DeleteWithObjectTest extends AbstractMultiversionedStmTest {

    public void testNoDependencies() {                              
        long handle = atomicInsert(new Person());

        createActiveTransaction();
        Person person = (Person) transaction.read(handle);
        transaction.delete(person);
        transaction.commit();

        assertTransactionIsCommitted();
        heap.isDeleted(handle);
    }

    public void testOthersDependsOnDeletedObject() {
        //todo
    }

    public void testDeletedObjectDependsOnOthers() {
        //todo
    }

    public void testDeleteAfterModification(){
        //todo        
    }

    public void testModificationAfterDelete(){
        //todo
    }

    public void testNull() {
        createActiveTransaction();
        long version = stm.getActiveVersion();

        try {
            transaction.delete(null);
            fail();
        } catch (NullPointerException ex) {
        }

        assertTransactionIsActive();
        assertStmActiveVersion(version);
    }

    public void testNoCitizen() {
        createActiveTransaction();
        long version = stm.getActiveVersion();

        try {
            transaction.delete("foo");
            fail();
        } catch (IllegalArgumentException ex) {
        }

        assertTransactionIsActive();
        assertStmActiveVersion(version);
    }

    public void testCitizenAttachedToNoTransaction() {
        createActiveTransaction();
        Person person = new Person();
        try {
            transaction.delete(person);
            fail();
        } catch (BadTransactionException ex) {

        }
        assertTransactionIsActive();
        assertStmVersionHasNotChanged();
    }

    public void testCitizenAlreadyAttachedToDifferentTransaction() {
        Person person = new Person();
        Transaction otherTransaction = stm.startTransaction();
        otherTransaction.attach(person);

        createActiveTransaction();
        try{
            transaction.delete(person);
            fail();
        }catch(BadTransactionException ex){
        }

        assertTransactionIsActive();
        assertStmVersionHasNotChanged();
        assertSame(otherTransaction, person.___getTransaction());
    }

    public void testCitizenAlreadyDeleted() {
        //todo
    }

    //test combination of both delete methods.

    //============= other states

    public void testWhileAborted() {
        createAbortedTransaction();
        long version = stm.getActiveVersion();

        try {
            transaction.delete(new Stack());
            fail();
        } catch (IllegalStateException ex) {
        }

        assertTransactionIsAborted();
        assertStmActiveVersion(version);
    }

    public void testWhileCommitted() {
        createCommittedTransaction();
        long version = stm.getActiveVersion();

        try {
            transaction.delete(new Stack());
            fail();
        } catch (IllegalStateException ex) {
        }

        assertTransactionIsCommitted();
        assertStmActiveVersion(version);
    }
}
