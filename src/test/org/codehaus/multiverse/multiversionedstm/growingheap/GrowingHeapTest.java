package org.codehaus.multiverse.multiversionedstm.growingheap;

import junit.framework.TestCase;
import org.codehaus.multiverse.util.iterators.ArrayIterator;
import org.codehaus.multiverse.multiversionedstm.examples.Person;
import org.codehaus.multiverse.multiversionedstm.growingheap.GrowingHeap;
import org.codehaus.multiverse.multiversionedstm.DehydratedStmObject;
import org.codehaus.multiverse.multiversionedstm.HeapSnapshot;
import org.codehaus.multiverse.multiversionedstm.StmObject;

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

    public void assertEmpty(long version, long handle) {
        HeapSnapshot snapshot = heap.getSnapshot(version);
        DehydratedStmObject found = snapshot.read(handle);
        assertNull(found);
    }

    public void writeUnconflicted(long expectedVersion, StmObject... citizens) {
        long commitVersion = heap.write(new ArrayIterator(citizens));
        assertEquals(expectedVersion, commitVersion);
        assertTrue(commitVersion > 0);
    }

    /*
    public void writeConflicted(DehydratedStmObject... dehydratedObjects) {
        long oldVersion = heap.getVersion();
        long commitVersion = heap.write(new long[]{}, dehydratedObjects);
        assertEquals(-1, commitVersion);
        assertEquals(oldVersion, heap.getVersion());
    }       */

    //================ read ===============================



    public void testRead_nonExistingHandle() {
        DehydratedStmObject cell = heap.getSnapshot(1).read(1);
        assertNull(cell);
    }

    public void testRead_versions() {
        long version = 1;
        Person person = new Person();
        writeUnconflicted(version, person);

        //assertHeapContent(version, content1);
        //assertHeapContent(version + 1, content1);
        //assertHeapContent(version + 2, content1);
    }

    //===============================================

    /*
    public void testWrite() {
        long handle = 1000;
        DehydratedStmObject content = new DummyDehydratedStmObject(handle);
        writeUnconflicted(1, content);

        assertEmpty(0, handle);
        assertHeapContent(1, content);
    } */

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

    /*
    public void testWrite_writeConflict() {
        long handle = 1;

        DehydratedStmObject initialCell = new DummyDehydratedStmObject(handle);
        writeUnconflicted(initialVersion + 1, initialCell);

        DehydratedStmObject thatCell = new DummyDehydratedStmObject(handle);
        writeUnconflicted(initialVersion + 2, thatCell);

        DehydratedStmObject thisCell = new DummyDehydratedStmObject(handle);
        writeConflicted(thisCell);

        assertHeapContent(initialVersion + 1, initialCell);
        assertHeapContent(initialVersion + 2, thatCell);
    }

    public void testWrite_concurrentWritingTransactionsNoConflict() {
        long handle1 = 1;
        long handle2 = 2;

        DehydratedStmObject initial1 = new DummyDehydratedStmObject(handle1);
        DehydratedStmObject initial2 = new DummyDehydratedStmObject(handle2);
        writeUnconflicted(initialVersion + 1, initial1, initial2);

        DehydratedStmObject updated1 = new DummyDehydratedStmObject(handle1);
        writeUnconflicted(initialVersion + 2, updated1);

        DehydratedStmObject updated2 = new DummyDehydratedStmObject(handle2);
        writeUnconflicted(initialVersion + 3, updated2);

        assertEmpty(initialVersion, handle1);
        assertEmpty(initialVersion, handle2);
        assertHeapContent(initialVersion + 1, initial1);
        assertHeapContent(initialVersion + 1, initial2);
        assertHeapContent(initialVersion + 2, updated1);
        assertHeapContent(initialVersion + 3, updated2);
    }

    public void testWrite_multipleWrites() {
        DehydratedStmObject content1 = new DummyDehydratedStmObject(1);
        DehydratedStmObject content2 = new DummyDehydratedStmObject(2);
        DehydratedStmObject content3 = new DummyDehydratedStmObject(3);

        writeUnconflicted(initialVersion + 1, content1, content2, content3);

        assertHeapContent(initialVersion + 1, content1);
        assertHeapContent(initialVersion + 1, content2);
        assertHeapContent(initialVersion + 1, content3);
    }

    public void testWrite_multipleOverwrites() {
        long handle = 1;
        DehydratedStmObject version1Content = new DummyDehydratedStmObject(handle);
        DehydratedStmObject version2Content = new DummyDehydratedStmObject(handle);
        DehydratedStmObject version3Content = new DummyDehydratedStmObject(handle);

        writeUnconflicted(initialVersion + 1, version1Content);
        writeUnconflicted(initialVersion + 2, version2Content);
        writeUnconflicted(initialVersion + 3, version3Content);

        assertEmpty(initialVersion, handle);
        assertHeapContent(initialVersion + 1, version1Content);
        assertHeapContent(initialVersion + 2, version2Content);
        assertHeapContent(initialVersion + 3, version3Content);
    } */
}
