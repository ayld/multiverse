package org.codehaus.multiverse.multiversionedstm;

import junit.framework.TestCase;
import org.codehaus.multiverse.IllegalPointerException;
import org.codehaus.multiverse.IllegalVersionException;

import static java.util.Arrays.asList;

public class MultiversionedHeapTest extends TestCase {

    private MultiversionedHeap heap;

    public void setUp() {
        heap = new MultiversionedHeap();
    }

    public void assertActualContent(long ptr, long expectedVersion, Object[] expectedContent) {
        assertEquals(expectedVersion, heap.getActualVersion(ptr));
        Object[] foundContent = heap.read(ptr, expectedVersion);
        assertEquals(asList(expectedContent), asList(foundContent));
    }

    public void assertContent(long ptr, long version, Object[] expectedContent) {
        Object[] foundContent = heap.read(ptr, version);
        assertEquals(asList(expectedContent), asList(foundContent));
    }

    public void assertWriteCount(int expectedWrites){
        assertEquals(expectedWrites, heap.getWriteCount());
    }

    public void assertReadCount(int expectedReads){
        assertEquals(expectedReads, heap.getReadCount());
    }

    public void assertNoReads(){
        assertReadCount(0);
    }

    public void assertNoWrites(){
        assertWriteCount(0);
    }

    public void testReadNonExistingPtr() {
        try {
            heap.read(10, 0);
            fail();
        } catch (IllegalPointerException ex) {
        }

        assertReadCount(1);
        assertNoWrites();
    }

    public void testReadTooNewVersion() {
        long ptr = 1000;
        long oldVersion = 1;
        long newVersion = oldVersion + 1;

        heap.write(ptr, newVersion, new Object[]{"foo"});
        try {
            heap.read(ptr, oldVersion);
            fail();
        } catch (IllegalVersionException ex) {
        }

        assertWriteCount(1);
        assertReadCount(1);
    }

    public void testReadOldVersionNoTrailingNewVersion() {
        Object[] content = new Object[]{"foo"};
        long ptr = 1000;
        long oldVersion = 1;
        long newVersion = oldVersion + 1;

        heap.write(ptr, oldVersion, content);
        assertContent(ptr, newVersion, content);
    }

    public void testReadCurrentVersion() {
        Object[] content = new Object[]{"foo"};
        long ptr = 1000;
        long version = 25;

        heap.write(ptr, version, content);
        assertActualContent(ptr, version, content);
    }

    public void testWriteNullContent(){
        try{
            heap.write(10, 10, null);
            fail();
        }catch(NullPointerException ex){
        }

        assertWriteCount(0);
    }

    //======================= overwrite ==========================

    public void testOverwrite() {
        Object[] oldContent = new Object[]{"foo"};
        Object[] newContent = new Object[]{"bar"};
        long ptr = 1000;
        long oldVersion = 1;
        long newVersion = oldVersion + 1;

        heap.write(ptr, oldVersion, oldContent);
        assertActualContent(ptr, oldVersion, oldContent);

        heap.write(ptr, newVersion, newContent);
        assertActualContent(ptr, newVersion, newContent);

        assertContent(ptr, oldVersion, oldContent);
    }

    public void testOverwriteWithSameVersionFails() {
        Object[] oldContent = new Object[]{"foo"};
        Object[] newContent = new Object[]{"bar"};
        long ptr = 1000;
        long version = 10;

        heap.write(ptr, version, oldContent);
        try {
            heap.write(ptr, version, newContent);
            fail();
        } catch (IllegalVersionException ex) {
        }

        assertContent(ptr, version, oldContent);
    }

    public void testOverwriteNewVersionWithOldVersionFails() {
        Object[] oldContent = new Object[]{"foo"};
        Object[] newContent = new Object[]{"bar"};
        long ptr = 1000;
        long oldVersion = 1;
        long newVersion = oldVersion + 1;

        heap.write(ptr, newVersion, newContent);
        try {
            heap.write(ptr, oldVersion, oldContent);
            fail();
        } catch (IllegalVersionException ex) {
        }

        assertContent(ptr, newVersion, newContent);
    }
}
