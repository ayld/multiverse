package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.AbstractTransactionTest;
import org.codehaus.multiverse.multiversionedstm.growingheap.GrowingHeap;
import org.codehaus.multiverse.core.Transaction;

public abstract class AbstractMultiversionedStmTest extends AbstractTransactionTest<MultiversionedStm, MultiversionedStm.MultiversionedTransaction> {
    protected GrowingHeap heap;

    public MultiversionedStm createStm() {
        heap = new GrowingHeap();
        return new MultiversionedStm(heap);
    }

    public void tearDown() throws Exception {
        super.tearDown();
        System.out.println(stm.getStatistics());
        System.out.println(heap.getStatistics());
    }

    public void assertStmVersionHasNotChanged() {
        assertEquals(transaction.getVersion(), stm.getCurrentVersion());
    }

    public void assertStmActiveVersion(long expected) {
        assertEquals(expected, stm.getCurrentVersion());
    }

    public void assertTransactionHasNoWrites() {
        assertEquals(0, transaction.getWriteCount());
    }

    public void assertTransactionNumberOfWrites(long expected) {
        assertEquals(expected, transaction.getWriteCount());
    }

    public void assertActualVersion(long ptr, long expectedVersion) {
        long foundVersion = heap.getActiveSnapshot().readVersion(ptr);
        assertEquals(expectedVersion, foundVersion);
    }

    public void assertCurrentStmVersion(long expectedVersion) {
        assertEquals(expectedVersion, stm.getCurrentVersion());
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

    public void assertHasHandleAndTransaction(StmObject citizen, long expectedPtr, Transaction expectedTrans) {
        assertNotNull(citizen);
        assertEquals(expectedPtr, citizen.___getHandle());
        assertEquals(expectedTrans, citizen.___getTransaction());
    }

    public void assertHasHandle(long expectedPtr, StmObject... citizens) {
        for (StmObject citizen : citizens)
            assertEquals("Pointer is not the same", expectedPtr, citizen.___getHandle());
    }

    public void assertHasTransaction(Transaction expected, StmObject... citizens) {
        for (StmObject citizen : citizens)
            assertSame("Transaction is not the same", expected, citizen.___getTransaction());
    }

    public void assertHasNoTransaction(StmObject... citizens) {
        for (StmObject citizen : citizens)
            assertNull("Transaction should be null", citizen.___getTransaction());
    }

    public void assertHeapContains(long handle, long expectedVersion, DehydratedStmObject expected) {
        DehydratedStmObject found = heap.getSnapshot(expectedVersion).read(handle);
        assertEquals("Content doesn't match", expected, found);
    }

    public void assertHeapContainsNow(long handle, long expectedVersion, DehydratedStmObject expected) {
        assertEquals("Versions don't match. -1 indicates no cell with given address",
                expectedVersion,
                heap.getActiveSnapshot().readVersion(handle));
        DehydratedStmObject found = heap.getSnapshot(expectedVersion).read(handle);
        assertEquals("Content doesn't match", expected, found);
    }
}
