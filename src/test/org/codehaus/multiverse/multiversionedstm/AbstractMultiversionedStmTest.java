package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.AbstractTransactionTest;
import org.codehaus.multiverse.transaction.Transaction;

public abstract class AbstractMultiversionedStmTest extends AbstractTransactionTest<MultiversionedStm, MultiversionedStm.MultiversionedTransaction> {
    protected GrowingHeap heap;

    public MultiversionedStm createStm() {
        heap = new GrowingHeap();
        return new MultiversionedStm(heap);
    }

    public void assertStmVersionHasNotChanged() {
        assertEquals(transaction.getVersion(), stm.getActiveVersion());
    }

    public void assertStmActiveVersion(long expected) {
        assertEquals(expected, stm.getActiveVersion());
    }

    public void assertTransactionHasNoWrites() {
        assertEquals(0, transaction.getNumberOfWrites());
    }

    public void assertTransactionNumberOfWrites(long expected) {
        assertEquals(expected, transaction.getNumberOfWrites());
    }

    public void assertActualVersion(long ptr, long expectedVersion) {
        long foundVersion = heap.getSnapshot().getVersion(ptr);
        assertEquals(expectedVersion, foundVersion);
    }

    public void assertCurrentStmVersion(long expectedVersion) {
        assertEquals(expectedVersion, stm.getActiveVersion());
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

    public void assertHasPointerAndTransaction(StmObject citizen, long expectedPtr, Transaction expectedTrans) {
        assertNotNull(citizen);
        assertEquals(expectedPtr, citizen.___getHandle());
        assertEquals(expectedTrans, citizen.___getTransaction());
    }

    public void assertHasPointer(long expectedPtr, StmObject... citizens) {
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
        assertEquals("Versions don't match. -1 indicates no cell with given address",
                expectedVersion,
                heap.getSnapshot().getVersion(handle));
        DehydratedStmObject found = heap.getSnapshot(expectedVersion).read(handle);
        assertEquals("Content doesn't match", expected, found);
    }
}
