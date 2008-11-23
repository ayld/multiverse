package org.codehaus.multiverse.multiversionedstm;

import junit.framework.TestCase;
import org.codehaus.multiverse.transaction.BadVersionException;
import org.codehaus.multiverse.transaction.NoSuchObjectException;

import static java.util.Arrays.asList;

public class GrowingMultiversionedHeapTest extends TestCase {

    private GrowingMultiversionedHeap<String> heap;

    public void setUp() {
        heap = new GrowingMultiversionedHeap<String>();
    }

    public void assertContent(long handle, long expectedVersion, String expectedContent) {
        assertEquals(expectedVersion, heap.readVersion(handle));
        String foundContent = heap.read(handle, expectedVersion);
        assertEquals(asList(expectedContent), asList(foundContent));
    }

    public void assertContentAtVersion(long handle, long version, String expectedContent) {
        String foundContent = heap.read(handle, version);
        assertEquals(expectedContent, foundContent);
    }

    public void assertWriteCount(int expectedWrites) {
        assertEquals(expectedWrites, heap.getWriteCount());
    }

    public void assertReadCount(int expectedReads) {
        assertEquals(expectedReads, heap.getReadCount());
    }

    public void assertIsDeleted(long handle) {
        assertTrue(heap.isDeleted(handle));
    }

    public void assertNoReads() {
        assertReadCount(0);
    }

    public void assertNoWrites() {
        assertWriteCount(0);
    }

    //================= delete ==================================

    public void testDelete() {
        long handle = heap.createHandle();
        long version = 1;
        String content = "foo";

        heap.write(handle, version, content);

        heap.delete(handle, version + 1);
        assertIsDeleted(handle);
        assertContentAtVersion(handle, version, content);
    }

    public void testDelete_justWrittenValueWithTheSameVersion() {
        long handle = heap.createHandle();
        long version = 1;
        String content = "foo";

        heap.write(handle, version, content);

        try {
            heap.delete(handle, version);
            fail();
        } catch (BadVersionException e) {

        }

        assertContent(handle, version, content);
    }

    public void testDelete_alreadyDeleted() {
        long handle = heap.createHandle();
        long version = 1;
        String content = "foo";

        heap.write(handle, version, content);
        heap.delete(handle, version + 1);

        try {
            heap.delete(handle, version + 2);
            fail();
        } catch (NoSuchObjectException ex) {
        }

        assertIsDeleted(handle);
        assertContentAtVersion(handle, version, content);
    }

    public void testDelete_NonExistingHandle() {
        try {
            heap.delete(-1, 1);
            fail();
        } catch (NoSuchObjectException ex) {
        }
    }

    //public void testDeleteIllegalVersion() {
    //    try {
    //        heap.delete(1, 1);
    //        fail();
    //    } catch (IllegalVersionException ex) {
    //    }
    //}


    // ================ read (handle) ============================================

    public void testRead_deleted() {
        String content = "foo";
        long handle = 1000;
        long version = 25;

        heap.write(handle, version, content);
        heap.delete(handle, version + 1);
        try {
            heap.read(handle);
            fail();
        } catch (NoSuchObjectException ex) {
        }
    }

    //================ read (handle, version)=====================================

    public void testReadWithVersion_currentVersion() {
        String content = "foo";
        long handle = 1000;
        long version = 25;

        heap.write(handle, version, content);
        Object foundContent = heap.read(handle, version);
        assertEquals(content, foundContent);
    }

    public void testReadWithVersion_oldVersionNoTrailingNewVersion() {
        String content = "foo";
        long handle = 1000;
        long oldVersion = 1;
        long newVersion = oldVersion + 1;

        heap.write(handle, oldVersion, content);
        assertContentAtVersion(handle, newVersion, content);
    }

    public void testReadWithVersion_olderVersions() {
        String content0 = "foo1";
        String content1 = "foo2";
        String content2 = "foo3";
        long handle = 1000;
        long version = 1;
        heap.write(handle, version, content0);
        heap.write(handle, version + 1, content1);
        heap.write(handle, version + 2, content2);

        assertContentAtVersion(handle, version, content0);
        assertContentAtVersion(handle, version + 1, content1);
        assertContentAtVersion(handle, version + 2, content2);
    }

    public void testReadWithVersion_deleted() {
        long handle = 10;
        long version = 1;
        String content = "foo";
        heap.write(handle, version, content);
        heap.delete(handle, version + 1);

        try {
            heap.read(handle, version + 2);
            fail();
        } catch (NoSuchObjectException ex) {
        }
    }

    public void testReadWithVersion_nonExistingHandle() {
        try {
            heap.read(10, 0);
            fail();
        } catch (NoSuchObjectException ex) {
        }

        assertReadCount(0);
        assertNoWrites();
    }

    public void testReadWithVersion_tooNewVersion() {
        long handle = 1000;
        long oldVersion = 1;
        long newVersion = oldVersion + 1;

        heap.write(handle, newVersion, "foo");
        try {
            heap.read(handle, oldVersion);
            fail();
        } catch (BadVersionException ex) {
        }

        assertWriteCount(1);
        assertReadCount(1);
    }

    // =================== readVersion =====================

    public void testReadVersion() {
        long handle = 10;
        long version = 100;
        String content = "foo";

        heap.write(handle, version, content);
        long foundVersion = heap.readVersion(handle);
        assertEquals(version, foundVersion);
    }

    public void testReadVersion_nonExistingCell() {
        try {
            heap.readVersion(100);
            fail();
        } catch (NoSuchObjectException ex) {
        }
    }

    public void testReadVersion_deletedCell() {
        long handle = 10;
        long version = 100;
        String content = "foo";

        heap.write(handle, version, content);
        heap.delete(handle, version + 1);

        long foundVersion = heap.readVersion(handle);
        assertEquals(version + 1, foundVersion);
    }

    //========================= writes ======================

    public void testWrite() {
        long handle = 10;
        long version = 20;
        String content = "foo";

        assertWriteCount(0);

        heap.write(handle, version, content);

        assertWriteCount(1);
        assertContent(handle, version, content);
    }

    public void _testWrite_nullContent() {
        try {
            heap.write(10, 10, null);
            fail();
            //todo: doet moet geen null pointer zijn. maar een assertion fout.
        } catch (NullPointerException ex) {
        }

        assertWriteCount(0);
    }

    public void testWriteIllegalVersion() {
        //todo
    }

    public void testWrite_overwrite() {
        String oldContent = "foo";
        String newContent = "bar";
        long ptr = 1000;
        long oldVersion = 1;
        long newVersion = oldVersion + 1;

        heap.write(ptr, oldVersion, oldContent);
        assertContent(ptr, oldVersion, oldContent);

        heap.write(ptr, newVersion, newContent);
        assertContent(ptr, newVersion, newContent);

        assertContentAtVersion(ptr, oldVersion, oldContent);
    }

    public void testWrite_overwriteDeletedWithSameVersionShouldFail() {
        String content = "foo";
        long handle = 1000;
        long version = 20;

        heap.write(handle, version, content);
        heap.delete(handle, version + 1);

        try {
            heap.write(handle, version + 1, "bar");
            fail();
        } catch (BadVersionException ex) {
        }

        assertContentAtVersion(handle, version, content);
        assertIsDeleted(handle);
    }


    public void testWrite_overwriteDeletedShouldFail() {
        String content = "foo";
        long handle = 1000;
        long version = 20;

        heap.write(handle, version, content);
        heap.delete(handle, version + 1);

        try {
            heap.write(handle, version + 2, "bar");
            fail();
        } catch (NoSuchObjectException ex) {

        }

        assertContentAtVersion(handle, version, content);
        assertIsDeleted(handle);
    }

    public void testWrite_overwriteWithSameVersionFails() {
        testWrite_overwriteWithBadVersionsFails(10, 10);
    }

    public void testWrite_overwriteWithOlderVersionFails() {
        testWrite_overwriteWithBadVersionsFails(10, 9);
    }

    public void testWrite_overwriteWithBadVersionsFails(long writeVersion, long overwriteVersion) {
        String oldContent = "foo";
        String newContent = "bar";
        long ptr = 1000;

        heap.write(ptr, writeVersion, oldContent);
        try {
            heap.write(ptr, overwriteVersion, newContent);
            fail();
        } catch (BadVersionException ex) {
        }

        assertContentAtVersion(ptr, writeVersion, oldContent);
    }

    public void testWrite_overwriteNewVersionWithOldVersionFails() {
        String oldContent = "foo";
        String newContent = "bar";
        long ptr = 1000;
        long oldVersion = 1;
        long newVersion = oldVersion + 1;

        heap.write(ptr, newVersion, newContent);
        try {
            heap.write(ptr, oldVersion, oldContent);
            fail();
        } catch (BadVersionException ex) {
        }

        assertContentAtVersion(ptr, newVersion, newContent);
    }
}
