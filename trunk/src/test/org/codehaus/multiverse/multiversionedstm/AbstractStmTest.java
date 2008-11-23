package org.codehaus.multiverse.multiversionedstm;

import junit.framework.TestCase;
import org.codehaus.multiverse.transaction.Transaction;

public abstract class AbstractStmTest extends TestCase {

    protected MultiversionedStm stm;
    protected GrowingMultiversionedHeap<DehydratedCitizen> heap;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        heap = new GrowingMultiversionedHeap<DehydratedCitizen>();
        stm = new MultiversionedStm(heap);
    }

    public void sleepRandom(long ms){
        sleep((long)(Math.random()*ms));
    }

    public void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Thread.interrupted();
            throw new RuntimeException(ex);
        }
    }

    public long insert(Citizen obj) {
        MultiversionedStm.MultiversionedTransaction t = stm.startTransaction();
        try {
            t.attach(obj);
            t.commit();
            return obj.___getHandle();
        } catch (RuntimeException ex) {
            t.abort();
            throw ex;
        }
    }

    public void assertHasPointer(long expectedPtr, Citizen... citizens) {
        for (Citizen citizen : citizens)
            assertEquals("Pointer is not the same", expectedPtr, citizen.___getHandle());
    }

    public void assertHasTransaction(Transaction expected, Citizen... citizens) {
        for (Citizen citizen : citizens)
            assertSame("Transaction is not the same", expected, citizen.___getTransaction());
    }

    public void assertCommitCount(long expected) {
        assertEquals(expected, stm.getCommittedCount());
    }

    public void assertStartedCount(long expected) {
        assertEquals(expected, stm.getStartedCount());
    }

    public void assertAbortedCount(long expected) {
        assertEquals(expected, stm.getAbortedCount());
    }

    public void assertActualVersion(long ptr, long expectedVersion) {
        long foundVersion = heap.readVersion(ptr);
        assertEquals(expectedVersion, foundVersion);
    }

    public void assertCurrentStmVersion(long expectedVersion) {
        assertEquals(expectedVersion, stm.getActiveVersion());
    }

    public void assertStmContains(long ptr, long expectedVersion, DehydratedCitizen expected) {
        assertEquals("Versions don't match", expectedVersion, heap.readVersion(ptr));
        DehydratedCitizen found = heap.read(ptr, expectedVersion);
        assertEquals("Content doesn't match", expected, found);
    }
}
