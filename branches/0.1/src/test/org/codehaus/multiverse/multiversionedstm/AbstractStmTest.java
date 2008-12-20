package org.codehaus.multiverse.multiversionedstm;

import junit.framework.TestCase;
import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.multiversionedstm.growingheap.GrowingMultiversionedHeap;

public abstract class AbstractStmTest extends TestCase {

    protected MultiversionedStm stm;
    protected GrowingMultiversionedHeap heap;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        heap = new GrowingMultiversionedHeap();
        stm = new MultiversionedStm(heap);
    }

    public void sleepRandom(long ms) {
        sleep((long) (Math.random() * ms));
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

    public void assertHasHandle(long expectedPtr, StmObject... citizens) {
        for (StmObject citizen : citizens)
            assertEquals("Pointer is not the same", expectedPtr, citizen.___getHandle());
    }

    public void assertHasTransaction(Transaction expected, StmObject... citizens) {
        for (StmObject citizen : citizens)
            assertSame("Transaction is not the same", expected, citizen.___getTransaction());
    }

    public void assertCommitCount(long expected) {
        assertEquals(expected, stm.getStatistics().getTransactionsCommitedCount());
    }

    public void assertStartedCount(long expected) {
        assertEquals(expected, stm.getStatistics().getTransactionsStartedCount());
    }

    public void assertAbortedCount(long expected) {
        assertEquals(expected, stm.getStatistics().getTransactionsAbortedCount());
    }

    public void assertActualVersion(long handle, long expectedVersion) {
        long foundVersion = heap.getActiveSnapshot().readVersion(handle);
        assertEquals(expectedVersion, foundVersion);
    }

    public void assertCurrentStmVersion(long expectedVersion) {
        assertEquals(expectedVersion, stm.getCurrentVersion());
    }

    public void assertStmContains(long handle, long expectedVersion, DehydratedStmObject expected) {
        assertEquals("Versions don't match", expectedVersion, heap.getActiveSnapshot().readVersion(handle));
        DehydratedStmObject found = heap.getSnapshot(expectedVersion).read(handle);
        assertEquals("Content doesn't match", expected, found);
    }
}
