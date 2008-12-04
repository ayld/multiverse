package org.codehaus.multiverse.multiversionedstm.growingheap;

import junit.framework.TestCase;
import org.codehaus.multiverse.multiversionedstm.DehydratedStmObject;
import org.codehaus.multiverse.multiversionedstm.DummyDehydratedStmObject;
import org.codehaus.multiverse.multiversionedstm.HeapSnapshot;
import org.codehaus.multiverse.util.iterators.ArrayIterator;

public class GrowingHeapTest extends TestCase {
    private GrowingHeap heap;
    private long initialVersion;

    public void setUp() {
        heap = new GrowingHeap();
        initialVersion = heap.getSnapshot().getVersion();
    }

    public void assertHeapContent(long version, DehydratedStmObject expected) {
        HeapSnapshot snapshot = heap.getSnapshot(version);
        DehydratedStmObject found = snapshot.read(expected.getHandle());
        assertSame(expected, found);
    }

    public void assertHeapNull(long version, long handle) {
        HeapSnapshot snapshot = heap.getSnapshot(version);
        DehydratedStmObject found = snapshot.read(handle);
        assertNull(found);
    }

    public void writeUnconflicted(DehydratedStmObject... dehydratedStmObjects) {
        long beforeCommitVersion = heap.getSnapshot().getVersion();
        long commitVersion = heap.write(beforeCommitVersion, new ArrayIterator(dehydratedStmObjects));
        assertEquals(heap.getSnapshot().getVersion(), commitVersion);
        assertTrue(commitVersion > 0);
    }

    public void writeConflicted(long startVersion, DehydratedStmObject... dehydratedObjects) {
        long beforeCommitVersion = heap.getSnapshot().getVersion();
        long commitVersion = heap.write(startVersion, dehydratedObjects);
        assertEquals(-1, commitVersion);
        assertEquals(beforeCommitVersion, heap.getSnapshot().getVersion());
    }

    //================ read ===============================


    public void testRead_nonExistingHandle() {
        DehydratedStmObject cell = heap.getSnapshot(1).read(1);
        assertNull(cell);
    }

    public void testRead_versions() {
        long handle = 10;
        DehydratedStmObject content = new DummyDehydratedStmObject(handle);
        writeUnconflicted(content);

        assertHeapNull(initialVersion, handle);
        assertHeapContent(initialVersion + 1, content);
        assertHeapContent(initialVersion + 2 + 1, content);
        assertHeapContent(initialVersion + 3, content);
    }

    //================ write ===============================

    public void testWrite() {
        long handle = 1000;
        DehydratedStmObject content = new DummyDehydratedStmObject(handle);
        writeUnconflicted(content);

        assertHeapNull(initialVersion, handle);
        assertHeapContent(initialVersion+1, content);
    }

    /*
    public void testWrite_badVersion() {
        long version = 1;
        long handle = 10;
        DehydratedStmObject contentOld = new DummyDehydratedStmObject(handle, version);
        write(contentOld);

        try {
            write(new DummyDehydratedStmObject(handle, version));
            fail();
        } catch (BadVersionException ex) {
        }

        assertHeapContent(contentOld);
    } */

    public void testWrite_writeConflict() {
        long handle = 1;

        DehydratedStmObject initialCell = new DummyDehydratedStmObject(handle);
        writeUnconflicted(initialCell);

        long version = heap.getSnapshot().getVersion();

        DehydratedStmObject thatCell = new DummyDehydratedStmObject(handle);
        writeUnconflicted(thatCell);

        DehydratedStmObject thisCell = new DummyDehydratedStmObject(handle);
        writeConflicted(version, thisCell);

        assertHeapNull(initialVersion, handle);
        assertHeapContent(version, initialCell);
        assertHeapContent(version + 1, thatCell);
    }

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

    public void testWrite_multipleWrites() {
        DehydratedStmObject content1 = new DummyDehydratedStmObject(1);
        DehydratedStmObject content2 = new DummyDehydratedStmObject(2);
        DehydratedStmObject content3 = new DummyDehydratedStmObject(3);

        writeUnconflicted(content1, content2, content3);

        assertHeapContent(initialVersion + 1, content1);
        assertHeapContent(initialVersion + 1, content2);
        assertHeapContent(initialVersion + 1, content3);
    }

    public void testWrite_multipleOverwrites() {
        long handle = 1;
        DehydratedStmObject version1Content = new DummyDehydratedStmObject(handle);
        DehydratedStmObject version2Content = new DummyDehydratedStmObject(handle);
        DehydratedStmObject version3Content = new DummyDehydratedStmObject(handle);

        writeUnconflicted(version1Content);
        writeUnconflicted( version2Content);
        writeUnconflicted(version3Content);

        assertHeapNull(initialVersion, handle);
        assertHeapContent(initialVersion + 1, version1Content);
        assertHeapContent(initialVersion + 2, version2Content);
        assertHeapContent(initialVersion + 3, version3Content);
    }

    // ==========================================

    public void testListenForEventThatHasNotYetOccurred(){

    }

    public void testListenForEventThatAlreadyHasOccurred(){

    }        
}
