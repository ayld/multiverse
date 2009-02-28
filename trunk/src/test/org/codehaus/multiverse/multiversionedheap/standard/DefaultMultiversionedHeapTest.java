package org.codehaus.multiverse.multiversionedheap.standard;

import org.codehaus.multiverse.core.NoProgressPossibleException;
import org.codehaus.multiverse.core.NoSuchObjectException;
import org.codehaus.multiverse.multiversionedheap.Deflatable;
import org.codehaus.multiverse.multiversionedheap.Deflated;
import org.codehaus.multiverse.multiversionedheap.DummyDeflatable;
import org.codehaus.multiverse.multiversionedheap.MultiversionedHeap.CommitResult;
import org.codehaus.multiverse.multiversionedheap.MultiversionedHeapSnapshot;
import org.codehaus.multiverse.multiversionedstm.HandleGenerator;
import org.codehaus.multiverse.util.iterators.ArrayIterator;
import org.codehaus.multiverse.util.iterators.EmptyIterator;
import org.codehaus.multiverse.util.latches.CheapLatch;
import org.codehaus.multiverse.util.latches.Latch;
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

    public void copyStatistics() {
        startCommitCount = statistics.commitSuccessCount.longValue();
        startCommittedStoreCount = statistics.committedStoreCount.longValue();
        startWriteConflictCount = statistics.commitWriteConflictCount.longValue();
        startCommitReadonlyCount = statistics.commitReadonlyCount.longValue();
    }

    public void assertCommitReadonlyCountIncreasedWith(int delta) {
        assertEquals(startCommitReadonlyCount + delta, statistics.commitReadonlyCount.longValue());
    }

    public void assertWriteConflictCountIncreasedWith(int delta) {
        assertEquals(startWriteConflictCount + delta, statistics.commitWriteConflictCount.longValue());
    }

    public void assertCommitSuccessCountIncreasedWith(int delta) {
        assertEquals(startCommitCount + delta, statistics.commitSuccessCount.longValue());
    }

    public void assertCommittedStoreCountIncreasedWith(int delta) {
        assertEquals(startCommittedStoreCount + delta, statistics.committedStoreCount.longValue());
    }

    public void assertActiveSnapshotContains(Deflated expected) {
        assertSnapshotContains(heap.getActiveSnapshot(), expected);
    }

    public void assertSnapshotContains(MultiversionedHeapSnapshot snapshot, Deflated expected) {
        Deflated found = snapshot.read(expected.___getHandle());
        assertEquals(expected, found);
    }

    public void assertActiveSnapshot(MultiversionedHeapSnapshot expected) {
        assertSame(expected, heap.getActiveSnapshot());
    }

    public void assertSnapshotContainsNull(MultiversionedHeapSnapshot snapshot, long handle) {
        Deflated found = snapshot.read(handle);
        assertNull(found);
    }

    public CommitResult writeUnconflicted(Deflatable... changes) {
        CommitResult result = heap.commit(heap.getActiveSnapshot(), new ArrayIterator<Deflatable>(changes));
        assertTrue(result.isSuccess());
        assertEquals(changes.length, result.getWriteCount());
        return result;
    }

    public void writeConflicted(MultiversionedHeapSnapshot startSnapshot, Deflatable... changes) {
        MultiversionedHeapSnapshot startOfCommitSnapshot = heap.getActiveSnapshot();
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
        MultiversionedHeapSnapshot startSnapshot = heap.getActiveSnapshot();

        long handle = HandleGenerator.createHandle();
        Deflatable change1 = new DummyDeflatable(handle, "foo");
        Deflatable change2 = new DummyDeflatable(handle, "foo");

        CommitResult result1 = writeUnconflicted(change1);
        CommitResult result2 = writeUnconflicted(new DummyDeflatable("bar"));
        CommitResult result3 = writeUnconflicted(change2);
        CommitResult result4 = writeUnconflicted(new DummyDeflatable("blabla"));

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

        MultiversionedHeapSnapshot startSnapshot = heap.getActiveSnapshot();

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
        MultiversionedHeapSnapshot startSnapshot = heap.getActiveSnapshot();

        copyStatistics();

        Deflatable deflatable = new DummyDeflatable("foo");
        writeUnconflicted(deflatable);

        MultiversionedHeapSnapshot endSnapshot = heap.getActiveSnapshot();
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
        MultiversionedHeapSnapshot startSnapshot = heap.getActiveSnapshot();

        copyStatistics();

        Deflatable change1 = new DummyDeflatable("a");
        Deflatable change2 = new DummyDeflatable("b");
        Deflatable change3 = new DummyDeflatable("c");

        writeUnconflicted(change1, change2, change3);

        MultiversionedHeapSnapshot endSnapshot = heap.getActiveSnapshot();
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
        MultiversionedHeapSnapshot startSnapshot = heap.getActiveSnapshot();
        Deflatable change = new DummyDeflatable("foo");

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

        Deflatable initial = new DummyDeflatable(handle, "base");
        writeUnconflicted(initial);

        MultiversionedHeapSnapshot afterInitialSnapshot = heap.getActiveSnapshot();

        //this is the first write.
        Deflatable first = new DummyDeflatable(handle, "first");
        writeUnconflicted(first);

        MultiversionedHeapSnapshot afterFirstSnapshot = heap.getActiveSnapshot();

        copyStatistics();

        //this write should fail, because another write already has happened
        Deflatable second = new DummyDeflatable(handle, "second");
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
        Deflatable change1 = new DummyDeflatable("foo");
        Deflatable change2 = new DummyDeflatable("bar");

        copyStatistics();

        MultiversionedHeapSnapshot startSnapshot = heap.getActiveSnapshot();

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
        Deflatable change1 = new DummyDeflatable(handle, "first");
        Deflatable change2 = new DummyDeflatable(handle, "second");
        Deflatable change3 = new DummyDeflatable(handle, "third");

        copyStatistics();

        CommitResult result1 = writeUnconflicted(change1);
        CommitResult result2 = writeUnconflicted(change2);
        CommitResult result3 = writeUnconflicted(change3);

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
        Deflatable change = new DummyDeflatable("first");

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
        Deflatable item1 = new DummyDeflatable("item1_v1");
        Deflatable item2 = new DummyDeflatable("item2_v1");
        writeUnconflicted(item1, item2);

        Latch latch = new CheapLatch();
        heap.listen(heap.getActiveSnapshot(), latch, new long[]{item1.___getHandle()});
        assertFalse(latch.isOpen());

        writeUnconflicted(item2);
        assertFalse(latch.isOpen());
    }

    @Test
    public void testListenWithoutAnyHandles() {
        MultiversionedHeapSnapshot snapshot = heap.getActiveSnapshot();
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
        CommitResult firstWrite = writeUnconflicted(new DummyDeflatable(handle, "first"));
        CommitResult secondWrite = writeUnconflicted(new DummyDeflatable(handle, "second"));

        Latch latch = new CheapLatch();
        heap.listen(firstWrite.getSnapshot(), latch, new long[]{handle});
        assertTrue(latch.isOpen());
    }

    @Test
    public void testListenerIsTriggeredUpByWrite() {
        Deflatable change = new DummyDeflatable("first");
        writeUnconflicted(change);

        Latch latch = new CheapLatch();
        heap.listen(heap.getActiveSnapshot(), latch, new long[]{change.___getHandle()});
        assertFalse(latch.isOpen());

        copyStatistics();

        writeUnconflicted(change);

        assertCommitSuccessCountIncreasedWith(1);
        assertCommitReadonlyCountIncreasedWith(0);
        assertCommittedStoreCountIncreasedWith(1);
        assertWriteConflictCountIncreasedWith(0);

        assertTrue(latch.isOpen());
    }
}
