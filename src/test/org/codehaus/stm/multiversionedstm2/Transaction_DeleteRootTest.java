package org.codehaus.stm.multiversionedstm2;

import org.codehaus.stm.IllegalPointerException;
import org.codehaus.stm.multiversionedstm2.examples.Stack;

public class Transaction_DeleteRootTest extends AbstractTransactionalTest {

    public void testIllegalPointer() {
        createActiveTransaction();
        long version = stm.getActiveVersion();

        try {
            transaction.deleteRoot(-1);
            fail();
        } catch (IllegalPointerException ex) {
        }

        assertTransactionIsActive();
        assertActiveStmVersion(version);
    }

    public void testDeleteFreshCitizen(){

    }

    public void testDeleteFreshCitizenMultipleTimes(){

    }

    public void testDeleteNonFreshCitizen() {

    }

    public void testDeleteNonFreshCitizenMultipleTimes(){

    }

    public void testWhileAborted() {
        long ptr = atomicInsert(new Stack());
        createAbortedTransaction();
        long version = stm.getActiveVersion();

        try {
            transaction.deleteRoot(ptr);
            fail();
        } catch (IllegalStateException ex) {

        }

        assertTransactionIsAborted();
        assertActiveStmVersion(version);
        //todo: controleren dat object nog aanwezig
    }

    public void testWhileCommitted() {
        long ptr = atomicInsert(new Stack());
        createCommittedTransaction();
        long version = stm.getActiveVersion();

        try {
            transaction.deleteRoot(ptr);
            fail();
        } catch (IllegalStateException ex) {

        }

        assertTransactionIsCommitted();
        assertActiveStmVersion(version);
        //todo: controleren dat object nog aanwezig
    }
}
