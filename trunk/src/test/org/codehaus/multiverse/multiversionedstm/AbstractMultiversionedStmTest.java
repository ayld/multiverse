package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.AbstractTransactionTest;
import org.codehaus.multiverse.transaction.Transaction;

public abstract class AbstractMultiversionedStmTest extends AbstractTransactionTest<MultiversionedStm, MultiversionedStm.MultiversionedTransaction> {
    private GrowingMultiversionedHeap<DehydratedCitizen> heap;

    public MultiversionedStm createStm() {
        heap = new GrowingMultiversionedHeap<DehydratedCitizen>();
        return new MultiversionedStm(heap);
    }

    public void assertStmVersionHasNotChanged() {
        assertEquals(transaction.getVersion(), stm.getActiveVersion());
    }

    public void assertActiveStmVersion(long expected) {
        assertEquals(expected, stm.getActiveVersion());
    }

    public void assertTransactionHasNoWrites() {
        assertEquals(0, transaction.getNumberOfWrites());
    }

    public void assertTransactionNumberOfWrites(long expected) {
        assertEquals(expected, transaction.getNumberOfWrites());
    }

    public void assertActualVersion(long ptr, long expectedVersion) {
        long foundVersion = heap.readVersion(ptr);
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

    public void assertHasPointerAndTransaction(Citizen citizen, long expectedPtr, Transaction expectedTrans) {
        assertNotNull(citizen);
        assertEquals(expectedPtr, citizen.___getPointer());
        assertEquals(expectedTrans, citizen.___getTransaction());
    }

    public void assertHasPointer(long expectedPtr, Citizen... citizens) {
        for (Citizen citizen : citizens)
            assertEquals("Pointer is not the same", expectedPtr, citizen.___getPointer());
    }

    public void assertHasTransaction(Transaction expected, Citizen... citizens) {
        for (Citizen citizen : citizens)
            assertSame("Transaction is not the same", expected, citizen.___getTransaction());
    }

    public void assertHasNoTransaction(Citizen... citizens) {
        for (Citizen citizen : citizens)
            assertNull("Transaction should be null", citizen.___getTransaction());
    }

    public void assertHeapContains(long ptr, long expectedVersion, DehydratedCitizen expected) {
        assertEquals("Versions don't match. -1 indicates no cell with given address", expectedVersion, heap.readVersion(ptr));
        DehydratedCitizen found = heap.read(ptr, expectedVersion);
        assertEquals("Content doesn't match", expected, found);
    }
}
