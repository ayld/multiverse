package org.codehaus.multiverse.multiversionedstm;

import junit.framework.TestCase;
import org.codehaus.multiverse.transaction.IllegalVersionException;
import org.codehaus.multiverse.util.CheapLatch;
import org.codehaus.multiverse.util.Latch;

public class MultiversionedCellTest extends TestCase {
    private MultiversionedCell cell;
    private Object initialContent;
    private long initialVersion;

    @Override
    public void setUp() {
        initialContent = 10;
        initialVersion = 30;
        cell = new MultiversionedCell(initialContent, initialVersion);
    }

    public void assertVersion(long expected) {
        assertEquals(expected, cell.readVersion());
    }

    public void assertContent(Object expected) {
        assertSame(expected, cell.read());
    }

    public void assertContent(Object expectedContent, long expectedVersion){
        assertContent(expectedContent);
        assertVersion(expectedVersion);
    }

    public void assertContentAtVersion(Object expected, long version) {
        assertSame(expected, cell.read(version));
    }

    public void testInitialCreation() {
        assertVersion(initialVersion);
        assertContent(initialContent);
    }

    private void assertIsDeleted() {
        assertTrue(cell.isDeleted());
    }


    //====================== read =========================

    public void testRead(){

    }

    //====================== read =========================

    public void testReadSpecific(){

    }

    //====================== write ========================

    public void testWrite() {
        long newVersion = initialVersion + 1;
        Object newContent = "foo";
        cell.write(newVersion, newContent);

        assertContent(newContent, newVersion);
        assertContentAtVersion(initialContent, initialVersion);
    }

    public void testWrite_AfterDeleteShouldFail() {
        cell.delete(cell.readVersion() + 1);
        long versionAfterDelete = cell.readVersion();

        try {
            cell.write(versionAfterDelete + 1, 10);
            fail();
        } catch (CellDeletedException ex) {
        }

        assertIsDeleted();
        assertVersion(versionAfterDelete);
        assertContentAtVersion(initialContent, initialVersion);
    }

    public void testWrite_sameValue() {
        long newversion = initialVersion + 1;
        cell.write(newversion, initialContent);

        assertContent(initialContent, newversion);
        assertContentAtVersion(initialContent, initialVersion);
    }

    public void testWrite_sameVersionShouldFail() {
        testWriteShouldFail(initialVersion);
    }

    public void testWrite_olderVersionShouldFail() {
        testWriteShouldFail(initialVersion - 1);
    }

    public void testWriteShouldFail(long version) {
        try {
            cell.write(version, "foo");
            fail();
        } catch (IllegalVersionException ex) {
        }

        assertContent(initialContent, initialVersion);
    }

    //================= listen =================================

    public void testListen_overwriteAlreadyHappened() {
        Latch latch = new CheapLatch();
        cell.listen(initialVersion - 1, latch);

        assertTrue(latch.isOpen());
        assertContent(initialContent, initialVersion);
    }

    public void testListen_overwriteHasNotOccurredYet() {
        Latch latch = new CheapLatch();
        cell.listen(initialVersion, latch);

        assertFalse(latch.isOpen());
        assertContent(initialContent);
        assertVersion(initialVersion);

        long newVersion = initialVersion + 1;
        Object newContent = "foo";
        cell.write(newVersion, newContent);

        assertTrue(latch.isOpen());
        assertContent(newContent, newVersion);
        assertContentAtVersion(initialContent, initialVersion);
    }

    public void testListen_alreadyDeleted() {
        //todo
    }

    public void testListen_versionIsNotPossible() {
        //todo
    }

    public void testListen_illegalVersion() {
        //todo
    }

    //============= delete ======================================

    public void testDelete() {
        cell.delete(cell.readVersion() + 1);
        assertIsDeleted();
        assertContentAtVersion(initialContent, initialVersion);
    }

    public void testDelete_alreadyDeleted() {
        cell.delete(cell.readVersion() + 1);

        try {
            cell.delete(cell.readVersion() + 1);
            fail();
        } catch (CellDeletedException ex) {
        }
        assertContentAtVersion(initialContent, initialVersion);
    }

//====================== prune ========================

    public void _testPrune() {
        cell.prune(initialVersion);
    }

}
