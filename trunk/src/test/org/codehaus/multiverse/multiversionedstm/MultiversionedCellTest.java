package org.codehaus.multiverse.multiversionedstm;

import junit.framework.TestCase;
import org.codehaus.multiverse.IllegalVersionException;
import org.codehaus.multiverse.util.CheapLatch;
import org.codehaus.multiverse.util.Latch;

public class MultiversionedCellTest extends TestCase {
    private MultiversionedCell cell;
    private Object initialValue;
    private long initialVersion;

    @Override
    public void setUp() {
        initialValue = 10;
        initialVersion = 30;
        cell = new MultiversionedCell(initialValue, initialVersion);
    }

    public void assertActiveVersion(long expected) {
        assertEquals(expected, cell.getActiveVersion());
    }

    public void assertActiveValue(Object expected) {
        assertSame(expected, cell.getActiveValue());
    }

    public void assertValue(long version, Object expected) {
        assertSame(expected, cell.getValue(version));
    }

    public void testInitialCreation() {
        assertActiveVersion(initialVersion);
        assertActiveValue(initialValue);
    }

    //====================== prune ========================

    public void _testPrune(){
        cell.prune(initialVersion);
    }

    //====================== write ========================

    public void testOverwriteWithNewerVersion() {
        long newversion = initialVersion + 1;
        Object newvalue = "foo";
        cell.write(newversion, newvalue);

        assertActiveVersion(newversion);
        assertActiveValue(newvalue);
        assertValue(initialVersion, initialValue);
    }

    public void testOverwriteWithNewerVersionButSameValue() {
        long newversion = initialVersion + 1;
        cell.write(newversion, initialValue);

        assertActiveVersion(newversion);
        assertActiveValue(initialValue);
        assertValue(initialVersion, initialValue);
    }

    public void testOverwriteWithSameVersionShouldFail() {
        try {
            cell.write(initialVersion, "foo");
            fail();
        } catch (IllegalVersionException ex) {
        }

        assertActiveVersion(initialVersion);
        assertActiveValue(initialValue);
    }

    public void testOverwriteWithOlderVersionShouldFail() {
        try {
            cell.write(initialVersion - 1, "foo");
            fail();
        } catch (IllegalVersionException ex) {
        }

        assertActiveVersion(initialVersion);
        assertActiveValue(initialValue);
    }

    //================= listenToChange =================================

    public void testListenToChange_overwriteAlreadyHappened() {
        Latch latch = new CheapLatch();
        cell.listen(initialVersion - 1, latch);

        assertTrue(latch.isOpen());
        assertActiveValue(initialValue);
        assertActiveVersion(initialVersion);
    }

    public void testListenToChange_overwriteHasNotOccurredYet() {
        Latch latch = new CheapLatch();
        cell.listen(initialVersion, latch);

        assertFalse(latch.isOpen());
        assertActiveValue(initialValue);
        assertActiveVersion(initialVersion);

        long newVersion = initialVersion + 1;
        Object newValue = "foo";
        cell.write(newVersion, newValue);

        assertTrue(latch.isOpen());
        assertActiveValue(newValue);
        assertActiveVersion(newVersion);
        assertValue(initialVersion, initialValue);
    }

    public void testListenToChange_versionIsNotPossible() {

    }

    public void testListenToChange_illegalVersion() {

    }

}
