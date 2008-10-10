package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.AbstractTransactionTest;
import org.codehaus.multiverse.transaction.Transaction;

import static java.util.Arrays.asList;

public class AbstractMultiversionedStmTest extends AbstractTransactionTest<MultiversionedStm, MultiversionedStm.MultiversionedTransaction> {
    private MultiversionedHeap heap;

    public MultiversionedStm createStm() {
        heap = new MultiversionedHeap();
        return new MultiversionedStm();
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
        long foundVersion = heap.getActualVersion(ptr);
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

    public void assertHasPointer(long expectedPtr, Citizen... citizens) {
        for (Citizen citizen : citizens)
            assertEquals("Pointer is not the same", expectedPtr, citizen.___getPointer());
    }

    public void assertHasTransaction(Transaction expected, Citizen... citizens) {
        for (Citizen citizen : citizens)
            assertSame("Transaction is not the same", expected, citizen.___getTransaction());
    }

    public void assertStmContains(long ptr, long expectedVersion, Object... expected) {
        assertEquals("Versions don't match", expectedVersion, heap.getActualVersion(ptr));
        Object[] found = heap.read(ptr, expectedVersion);
        assertEquals("Content doesn't match", asList(expected), asList(found));
    }
}
