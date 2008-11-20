package org.codehaus.multiverse.multiversionedstm;

import junit.framework.TestCase;
import org.codehaus.multiverse.transaction.ObjectDoesNotExistException;
import org.codehaus.multiverse.transaction.IllegalVersionException;

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

    public void assertNoReads() {
        assertReadCount(0);
    }

    public void assertNoWrites() {
        assertWriteCount(0);
    }


    //================= delete ==================================

    public void testDelete(){
        long handle = heap.createHandle();
        long version = 1;
        String content = "foo";

        heap.write(handle, version, content);

        heap.delete(handle, version+1);
    }

    public void testDelete_NonExistingHandle() {
        try {
            heap.delete(-1, 1);
            fail();
        } catch (ObjectDoesNotExistException ex) {
        }
    }

    //public void testDeleteIllegalVersion() {
    //    try {
    //        heap.delete(1, 1);
    //        fail();
    //    } catch (IllegalVersionException ex) {
    //    }
    //}

    //public void testDeleteNonExistingPointer() {
    //    try {
    //        heap.delete(100, 1);
    //        fail();
    //    } catch (IllegalVersionException ex) {
    //
    //    }
    //}

    //================ reads =====================================

    public void testReadNonExistingPtr() {
        try {
            heap.read(10, 0);
            fail();
        } catch (ObjectDoesNotExistException ex) {
        }

        assertReadCount(0);
        assertNoWrites();
    }

    public void testReadTooNewVersion() {
        long ptr = 1000;
        long oldVersion = 1;
        long newVersion = oldVersion + 1;

        heap.write(ptr, newVersion, "foo");
        try {
            heap.read(ptr, oldVersion);
            fail();
        } catch (IllegalVersionException ex) {
        }

        assertWriteCount(1);
        assertReadCount(1);
    }

    public void testReadOldVersionNoTrailingNewVersion() {
        String content = "foo";
        long ptr = 1000;
        long oldVersion = 1;
        long newVersion = oldVersion + 1;

        heap.write(ptr, oldVersion, content);
        assertContentAtVersion(ptr, newVersion, content);
    }

    public void testReadCurrentVersion() {
        String content = "foo";
        long ptr = 1000;
        long version = 25;

        heap.write(ptr, version, content);
        assertContent(ptr, version, content);
    }

    //========================= writes ======================

    public void testWriteNullContent() {
        try {
            heap.write(10, 10, null);
            fail();
        } catch (NullPointerException ex) {
        }

        assertWriteCount(0);
    }

    public void testWriteIllegalPointer(){
        //todo
    }

    public void testWriteIllegalVersion(){
        //todo
    }

    public void testOverwrite() {
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

    public void testOverwriteWithSameVersionFails() {
        String oldContent = "foo";
        String newContent = "bar";
        long ptr = 1000;
        long version = 10;

        heap.write(ptr, version, oldContent);
        try {
            heap.write(ptr, version, newContent);
            fail();
        } catch (IllegalVersionException ex) {
        }

        assertContentAtVersion(ptr, version, oldContent);
    }

    public void testOverwriteNewVersionWithOldVersionFails() {
        String oldContent = "foo";
        String newContent = "bar";
        long ptr = 1000;
        long oldVersion = 1;
        long newVersion = oldVersion + 1;

        heap.write(ptr, newVersion, newContent);
        try {
            heap.write(ptr, oldVersion, oldContent);
            fail();
        } catch (IllegalVersionException ex) {
        }

        assertContentAtVersion(ptr, newVersion, newContent);
    }
}
