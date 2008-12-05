package org.codehaus.multiverse.multiversionedstm;

import junit.framework.TestCase;
import org.codehaus.multiverse.transaction.Transaction;
import org.codehaus.multiverse.multiversionedstm.growingheap.GrowingHeap;

public abstract class AbstractStmTest extends TestCase {

    protected MultiversionedStm stm;
    protected GrowingHeap heap;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        heap = new GrowingHeap();
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

    public long insert(StmObject obj) {
        MultiversionedStm.MultiversionedTransaction t = stm.startTransaction();
        try {
            t.attachAsRoot(obj);
            t.commit();
            return obj.___getHandle();
        } catch (RuntimeException ex) {
            t.abort();
            throw ex;
        }
    }

    public void assertHasPointer(long expectedPtr, StmObject... citizens) {
        for (StmObject citizen : citizens)
            assertEquals("Pointer is not the same", expectedPtr, citizen.___getHandle());
    }

    public void assertHasTransaction(Transaction expected, StmObject... citizens) {
        for (StmObject citizen : citizens)
            assertSame("Transaction is not the same", expected, citizen.___getTransaction());
    }

    public void assertCommitCount(long expected) {
        assertEquals(expected, stm.getTransactionsCommitedCount());
    }

    public void assertStartedCount(long expected) {
        assertEquals(expected, stm.getTransactionsStartedCount());
    }

    public void assertAbortedCount(long expected) {
        assertEquals(expected, stm.getTransactionsAbortedCount());
    }

    public void assertActualVersion(long handle, long expectedVersion) {
        long foundVersion = heap.getSnapshot().getVersion(handle);
        assertEquals(expectedVersion, foundVersion);
    }

    public void assertCurrentStmVersion(long expectedVersion) {
        assertEquals(expectedVersion, stm.getCurrentVersion());
    }

    public void assertStmContains(long handle, long expectedVersion, DehydratedStmObject expected) {
        assertEquals("Versions don't match", expectedVersion, heap.getSnapshot().getVersion(handle));
        DehydratedStmObject found = heap.getSnapshot(expectedVersion).read(handle);
        assertEquals("Content doesn't match", expected, found);
    }
}
