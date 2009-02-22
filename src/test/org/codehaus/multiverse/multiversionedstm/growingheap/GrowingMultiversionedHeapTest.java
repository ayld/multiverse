package org.codehaus.multiverse.multiversionedstm.growingheap;

import org.codehaus.multiverse.multiversionedstm.DehydratedStmObject;
import org.codehaus.multiverse.multiversionedstm.DummyDehydratedStmObject;
import org.codehaus.multiverse.multiversionedstm.MultiversionedHeap;
import org.codehaus.multiverse.multiversionedstm.MultiversionedHeapSnapshot;
import org.codehaus.multiverse.util.iterators.ArrayIterator;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class GrowingMultiversionedHeapTest {

    private GrowingMultiversionedHeap heap;
    private long initialVersion;

    @Before
    public void setUp() {
        heap = new GrowingMultiversionedHeap();
        initialVersion = heap.getActiveSnapshot().getVersion();
    }

    public void assertHeapContent(long version, DehydratedStmObject expected) {
        MultiversionedHeapSnapshot snapshot = heap.getSnapshot(version);
        DehydratedStmObject found = snapshot.read(expected.getHandle());
        assertSame(expected, found);
    }

    public void assertHeapNull(long version, long handle) {
        MultiversionedHeapSnapshot snapshot = heap.getSnapshot(version);
        DehydratedStmObject found = snapshot.read(handle);
        assertNull(found);
    }

    public void writeUnconflicted(DehydratedStmObject... dehydratedStmObjects) {
        MultiversionedHeap.CommitResult result = heap.commit(heap.getActiveSnapshot(), new ArrayIterator(dehydratedStmObjects));
        assertTrue(result.isSuccess());
        assertTrue(result.getWriteCount() > 0);
    }

    public void writeConflicted(MultiversionedHeapSnapshot startOfTransactionSnapshot, DehydratedStmObject... dehydratedObjects) {
        MultiversionedHeapSnapshot startOfCommitSnapshot = heap.getActiveSnapshot();
        MultiversionedHeap.CommitResult result = heap.commit(startOfTransactionSnapshot, dehydratedObjects);
        assertFalse(result.isSuccess());
        assertSame(startOfCommitSnapshot, heap.getActiveSnapshot());
    }

    //================ read ===============================

    @Test
    public void testRead_nonExistingHandle() {
        DehydratedStmObject cell = heap.getActiveSnapshot().read(1);
        assertNull(cell);
    }

    @Test
    public void testRead_versions() {
        long handle = 10;
        DehydratedStmObject content = new DummyDehydratedStmObject(handle);
        writeUnconflicted(content);

        assertHeapNull(initialVersion, handle);
        assertHeapContent(initialVersion + 1, content);
        assertHeapContent(initialVersion + 2, content);
        assertHeapContent(initialVersion + 3, content);
    }

    //================ commit ===============================

    @Test
    public void testWrite() {
        long handle = 1000;
        DehydratedStmObject content = new DummyDehydratedStmObject(handle);
        writeUnconflicted(content);

        assertHeapNull(initialVersion, handle);
        assertHeapContent(initialVersion + 1, content);
    }

    /*
    @Test
    public void testWrite_badVersion() {
        long version = 1;
        long handle = 10;
        DehydratedStmObject contentOld = new DummyDehydratedStmObject(handle, version);
        commit(contentOld);

        try {
            commit(new DummyDehydratedStmObject(handle, version));
            fail();
        } catch (BadVersionException ex) {
        }

        assertHeapContent(contentOld);
    } */

    @Test
    public void testWrite_writeConflict() {
        long handle = 1;

        DehydratedStmObject initial = new DummyDehydratedStmObject(handle);
        writeUnconflicted(initial);

        MultiversionedHeapSnapshot startSnapshot = heap.getActiveSnapshot();

        //this is the first write.
        DehydratedStmObject thatCell = new DummyDehydratedStmObject(handle);
        writeUnconflicted(thatCell);

        //this write should fail, because another write already has happened
        DehydratedStmObject thisCell = new DummyDehydratedStmObject(handle);
        writeConflicted(startSnapshot, thisCell);

        assertHeapNull(initialVersion, handle);
        assertHeapContent(startSnapshot.getVersion(), initial);
        assertHeapContent(heap.getActiveSnapshot().getVersion(), thatCell);
    }

    @Test
    public void testWrite_concurrentWritingTransactionsNoConflict() {
        long handle1 = 1;
        long handle2 = 2;

        DehydratedStmObject initial1 = new DummyDehydratedStmObject(handle1);
        DehydratedStmObject initial2 = new DummyDehydratedStmObject(handle2);
        writeUnconflicted(initial1, initial2);

        DehydratedStmObject updated1 = new DummyDehydratedStmObject(handle1);
        writeUnconflicted(updated1);

        DehydratedStmObject updated2 = new DummyDehydratedStmObject(handle2);
        writeUnconflicted(updated2);

        assertHeapNull(initialVersion, handle1);
        assertHeapNull(initialVersion, handle2);
        assertHeapContent(initialVersion + 1, initial1);
        assertHeapContent(initialVersion + 1, initial2);
        assertHeapContent(initialVersion + 2, updated1);
        assertHeapContent(initialVersion + 3, updated2);
    }

    @Test
    public void testWrite_multipleWrites() {
        DehydratedStmObject content1 = new DummyDehydratedStmObject(1);
        DehydratedStmObject content2 = new DummyDehydratedStmObject(2);
        DehydratedStmObject content3 = new DummyDehydratedStmObject(3);

        writeUnconflicted(content1, content2, content3);

        assertHeapContent(initialVersion + 1, content1);
        assertHeapContent(initialVersion + 1, content2);
        assertHeapContent(initialVersion + 1, content3);
    }

    @Test
    public void testWrite_multipleOverwrites() {
        long handle = 1;
        DehydratedStmObject version1Content = new DummyDehydratedStmObject(handle);
        DehydratedStmObject version2Content = new DummyDehydratedStmObject(handle);
        DehydratedStmObject version3Content = new DummyDehydratedStmObject(handle);

        writeUnconflicted(version1Content);
        writeUnconflicted(version2Content);
        writeUnconflicted(version3Content);

        assertHeapNull(initialVersion, handle);
        assertHeapContent(initialVersion + 1, version1Content);
        assertHeapContent(initialVersion + 2, version2Content);
        assertHeapContent(initialVersion + 3, version3Content);
    }

    // ==========================================

    @Test
    public void testListenForEventThatHasNotYetOccurred() {
        //todo
    }

    @Test
    public void testListenForEventThatAlreadyHasOccurred() {
        //todo
    }
}
