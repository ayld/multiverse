package org.codehaus.multiverse.multiversionedheap.standard;

import org.codehaus.multiverse.api.exceptions.NoProgressPossibleException;
import org.codehaus.multiverse.api.exceptions.NoSuchObjectException;
import org.codehaus.multiverse.multiversionedheap.Deflatable;
import org.codehaus.multiverse.multiversionedheap.Deflated;
import org.codehaus.multiverse.multiversionedheap.HeapSnapshot;
import org.codehaus.multiverse.multiversionedheap.MultiversionedHeap.CommitResult;
import org.codehaus.multiverse.multiversionedheap.StringDeflatable;
import org.codehaus.multiverse.multiversionedstm.HandleGenerator;
import org.codehaus.multiverse.utils.iterators.ArrayIterator;
import org.codehaus.multiverse.utils.iterators.EmptyIterator;
import org.codehaus.multiverse.utils.latches.CheapLatch;
import org.codehaus.multiverse.utils.latches.Latch;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class DefaultMultiversionedHeapTest {

    private DefaultMultiversionedHeap heap;

    private long startCommitCount;
    private long startCommittedStoreCount;
    private long startWriteConflictCount;
    private long startCommitReadonlyCount;
    private DefaultMultiversionedHeapStatistics statistics;

    @Before
    public void setUp() {
        heap = new DefaultMultiversionedHeap();
        statistics = heap.getStatistics();
    }

    void copyStatistics() {
        startCommitCount = statistics.commitSuccessCount.longValue();
        startCommittedStoreCount = statistics.committedStoreCount.longValue();
        startWriteConflictCount = statistics.commitWriteConflictCount.longValue();
        startCommitReadonlyCount = statistics.commitReadonlyCount.longValue();
    }

    void assertCommitReadonlyCountIncreasedWith(int delta) {
        assertEquals(startCommitReadonlyCount + delta, statistics.commitReadonlyCount.longValue());
    }

    void assertWriteConflictCountIncreasedWith(int delta) {
        assertEquals(startWriteConflictCount + delta, statistics.commitWriteConflictCount.longValue());
    }

    void assertCommitSuccessCountIncreasedWith(int delta) {
        assertEquals(startCommitCount + delta, statistics.commitSuccessCount.longValue());
    }

    void assertCommittedStoreCountIncreasedWith(int delta) {
        assertEquals(startCommittedStoreCount + delta, statistics.committedStoreCount.longValue());
    }

    void assertActiveSnapshotContains(Deflated expected) {
        assertSnapshotContains(heap.getActiveSnapshot(), expected);
    }

    void assertSnapshotContains(HeapSnapshot snapshot, Deflated expected) {
        Deflated found = snapshot.read(expected.___getHandle());
        assertEquals(expected, found);
    }

    void assertActiveSnapshot(HeapSnapshot expected) {
        assertSame(expected, heap.getActiveSnapshot());
    }

    void assertSnapshotContainsNull(HeapSnapshot snapshot, long handle) {
        Deflated found = snapshot.read(handle);
        assertNull(found);
    }

    CommitResult writeAndAssertNoConflicts(Deflatable... changes) {
        CommitResult result = heap.commit(heap.getActiveSnapshot(), new ArrayIterator<Deflatable>(changes));
        assertTrue(result.isSuccess());
        assertEquals(changes.length, result.getWriteCount());
        return result;
    }

    void writeConflicted(HeapSnapshot startSnapshot, Deflatable... changes) {
        HeapSnapshot startOfCommitSnapshot = heap.getActiveSnapshot();
        CommitResult result = heap.commit(startSnapshot, changes);
        assertFalse(result.isSuccess());
        assertSame(startOfCommitSnapshot, heap.getActiveSnapshot());
    }

    //================ read ===============================

    @Test
    public void testRead_nonExistingHandle() {
        Deflated deflated = heap.getActiveSnapshot().read(1);
        assertNull(deflated);
    }

    @Test
    public void testRead_differentVersion() {
        HeapSnapshot startSnapshot = heap.getActiveSnapshot();

        long handle = HandleGenerator.createHandle();
        Deflatable change1 = new StringDeflatable(handle, "foo");
        Deflatable change2 = new StringDeflatable(handle, "foo");

        CommitResult result1 = writeAndAssertNoConflicts(change1);
        CommitResult result2 = writeAndAssertNoConflicts(new StringDeflatable("bar"));
        CommitResult result3 = writeAndAssertNoConflicts(change2);
        CommitResult result4 = writeAndAssertNoConflicts(new StringDeflatable("blabla"));

        assertSnapshotContainsNull(startSnapshot, handle);
        assertSnapshotContains(result1.getSnapshot(), change1.___deflate(result1.getSnapshot().getVersion()));
        assertSnapshotContains(result2.getSnapshot(), change1.___deflate(result1.getSnapshot().getVersion()));
        assertSnapshotContains(result3.getSnapshot(), change2.___deflate(result3.getSnapshot().getVersion()));
        assertSnapshotContains(result4.getSnapshot(), change2.___deflate(result3.getSnapshot().getVersion()));
    }

    //================ commit ===============================

    @Test
    public void testWriteWithoutChanges() {
        copyStatistics();

        HeapSnapshot startSnapshot = heap.getActiveSnapshot();

        CommitResult result = heap.commit(startSnapshot, EmptyIterator.INSTANCE);

        assertTrue(result.isSuccess());
        assertEquals(0, result.getWriteCount());
        //make sure that the snapshot has not changed
        assertSame(startSnapshot, heap.getActiveSnapshot());
        //make sure that the readonly transaction is registered

        assertCommitSuccessCountIncreasedWith(0);
        assertCommitReadonlyCountIncreasedWith(1);
        assertCommittedStoreCountIncreasedWith(0);
        assertWriteConflictCountIncreasedWith(0);
    }

    @Test
    public void testSingleWrite() {
        HeapSnapshot startSnapshot = heap.getActiveSnapshot();

        copyStatistics();

        Deflatable deflatable = new StringDeflatable("foo");
        writeAndAssertNoConflicts(deflatable);

        HeapSnapshot endSnapshot = heap.getActiveSnapshot();
        assertNotSame(startSnapshot, endSnapshot);
        assertEquals(startSnapshot.getVersion() + 1, endSnapshot.getVersion());

        assertCommitSuccessCountIncreasedWith(1);
        assertCommitReadonlyCountIncreasedWith(0);
        assertCommittedStoreCountIncreasedWith(1);
        assertWriteConflictCountIncreasedWith(0);

        assertSnapshotContainsNull(startSnapshot, deflatable.___getHandle());
        assertActiveSnapshotContains(deflatable.___deflate(startSnapshot.getVersion() + 1));
    }

    @Test
    public void testWrite_multipleWritesInSingleTransaction() {
        HeapSnapshot startSnapshot = heap.getActiveSnapshot();

        copyStatistics();

        Deflatable change1 = new StringDeflatable("a");
        Deflatable change2 = new StringDeflatable("b");
        Deflatable change3 = new StringDeflatable("c");

        writeAndAssertNoConflicts(change1, change2, change3);

        HeapSnapshot endSnapshot = heap.getActiveSnapshot();
        assertNotSame(startSnapshot, endSnapshot);
        assertEquals(startSnapshot.getVersion() + 1, endSnapshot.getVersion());

        assertCommitSuccessCountIncreasedWith(1);
        assertCommitReadonlyCountIncreasedWith(0);
        assertCommittedStoreCountIncreasedWith(3);
        assertWriteConflictCountIncreasedWith(0);
        assertActiveSnapshotContains(change1.___deflate(startSnapshot.getVersion() + 1));
        assertActiveSnapshotContains(change2.___deflate(startSnapshot.getVersion() + 1));
        assertActiveSnapshotContains(change3.___deflate(startSnapshot.getVersion() + 1));
    }

    public void testDuplicateWriteLeadsToWriteConflict() {
        HeapSnapshot startSnapshot = heap.getActiveSnapshot();
        Deflatable change = new StringDeflatable("foo");

        copyStatistics();

        writeConflicted(startSnapshot, change, change);

        assertCommitSuccessCountIncreasedWith(0);
        assertCommitReadonlyCountIncreasedWith(0);
        assertCommittedStoreCountIncreasedWith(0);
        assertWriteConflictCountIncreasedWith(1);
    }

    @Test
    public void testWrite_writeConflict() {
        long handle = 1;

        Deflatable initial = new StringDeflatable(handle, "base");
        writeAndAssertNoConflicts(initial);

        HeapSnapshot afterInitialSnapshot = heap.getActiveSnapshot();

        //this is the first createNewForWrite.
        Deflatable first = new StringDeflatable(handle, "first");
        writeAndAssertNoConflicts(first);

        HeapSnapshot afterFirstSnapshot = heap.getActiveSnapshot();

        copyStatistics();

        //this createNewForWrite should fail, because another createNewForWrite already has happened
        Deflatable second = new StringDeflatable(handle, "second");
        writeConflicted(afterInitialSnapshot, second);

        assertCommitSuccessCountIncreasedWith(0);
        assertCommitReadonlyCountIncreasedWith(0);
        assertCommittedStoreCountIncreasedWith(0);
        assertWriteConflictCountIncreasedWith(1);

        assertSnapshotContains(afterInitialSnapshot, initial.___deflate(afterInitialSnapshot.getVersion()));
        assertActiveSnapshotContains(first.___deflate(afterFirstSnapshot.getVersion()));
    }

    @Test
    public void testWrite_nonConflictingCommitsCanBeExecutedInParallel() {
        Deflatable change1 = new StringDeflatable("foo");
        Deflatable change2 = new StringDeflatable("bar");

        copyStatistics();

        HeapSnapshot startSnapshot = heap.getActiveSnapshot();

        CommitResult result1 = heap.commit(startSnapshot, change1);
        assertTrue(result1.isSuccess());
        assertEquals(1, result1.getWriteCount());

        CommitResult result2 = heap.commit(startSnapshot, change2);
        assertTrue(result2.isSuccess());
        assertEquals(1, result2.getWriteCount());

        assertCommitSuccessCountIncreasedWith(2);
        assertCommitReadonlyCountIncreasedWith(0);
        assertCommittedStoreCountIncreasedWith(2);
        assertWriteConflictCountIncreasedWith(0);

        assertSnapshotContains(result1.getSnapshot(), change1.___deflate(result1.getSnapshot().getVersion()));
        assertActiveSnapshotContains(change1.___deflate(result1.getSnapshot().getVersion()));
        assertActiveSnapshotContains(change2.___deflate(result2.getSnapshot().getVersion()));
    }

    @Test
    public void testWrite_multipleOverwrites() {
        long handle = 1;
        Deflatable change1 = new StringDeflatable(handle, "first");
        Deflatable change2 = new StringDeflatable(handle, "second");
        Deflatable change3 = new StringDeflatable(handle, "third");

        copyStatistics();

        CommitResult result1 = writeAndAssertNoConflicts(change1);
        CommitResult result2 = writeAndAssertNoConflicts(change2);
        CommitResult result3 = writeAndAssertNoConflicts(change3);

        assertCommitSuccessCountIncreasedWith(3);
        assertCommitReadonlyCountIncreasedWith(0);
        assertCommittedStoreCountIncreasedWith(3);
        assertWriteConflictCountIncreasedWith(0);

        assertSnapshotContains(result1.getSnapshot(), change1.___deflate(result1.getSnapshot().getVersion()));
        assertSnapshotContains(result2.getSnapshot(), change2.___deflate(result2.getSnapshot().getVersion()));
        assertActiveSnapshotContains(change3.___deflate(result3.getSnapshot().getVersion()));
    }

    // ==========================================

    @Test
    public void testListenToHandleThatDoesntExist() {
        Deflatable change = new StringDeflatable("first");

        Latch latch = new CheapLatch();
        try {
            heap.listen(heap.getActiveSnapshot(), latch, new long[]{change.___getHandle()});
            fail();
        } catch (NoSuchObjectException ex) {

        }
        //the latch is opened if the handle doesn't exist
        assertTrue(latch.isOpen());
    }

    @Test
    public void testListen_handlesThatAreNotPartOfTheCommitAreNotWokenUp() {
        Deflatable item1 = new StringDeflatable("item1_v1");
        Deflatable item2 = new StringDeflatable("item2_v1");
        writeAndAssertNoConflicts(item1, item2);

        Latch latch = new CheapLatch();
        heap.listen(heap.getActiveSnapshot(), latch, new long[]{item1.___getHandle()});
        assertFalse(latch.isOpen());

        writeAndAssertNoConflicts(item2);
        assertFalse(latch.isOpen());
    }

    @Test
    public void testListenWithoutAnyHandles() {
        HeapSnapshot snapshot = heap.getActiveSnapshot();
        Latch latch = new CheapLatch();

        try {
            heap.listen(heap.getActiveSnapshot(), latch, new long[]{});
            fail();
        } catch (NoProgressPossibleException ex) {

        }

        assertFalse(latch.isOpen());
        assertActiveSnapshot(snapshot);
    }

    @Test
    public void testListenForEventThatAlreadyHasOccurred() {
        long handle = HandleGenerator.createHandle();
        CommitResult firstWrite = writeAndAssertNoConflicts(new StringDeflatable(handle, "first"));
        CommitResult secondWrite = writeAndAssertNoConflicts(new StringDeflatable(handle, "second"));

        Latch latch = new CheapLatch();
        heap.listen(firstWrite.getSnapshot(), latch, new long[]{handle});
        assertTrue(latch.isOpen());
    }

    @Test
    public void testListenerIsTriggeredUpByWrite() {
        Deflatable change = new StringDeflatable("first");
        writeAndAssertNoConflicts(change);

        Latch latch = new CheapLatch();
        heap.listen(heap.getActiveSnapshot(), latch, new long[]{change.___getHandle()});
        assertFalse(latch.isOpen());

        copyStatistics();

        writeAndAssertNoConflicts(change);

        assertCommitSuccessCountIncreasedWith(1);
        assertCommitReadonlyCountIncreasedWith(0);
        assertCommittedStoreCountIncreasedWith(1);
        assertWriteConflictCountIncreasedWith(0);

        assertTrue(latch.isOpen());
    }
}
