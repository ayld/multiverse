package org.codehaus.multiverse.multiversionedstm.growingheap;

import junit.framework.TestCase;
import org.codehaus.multiverse.util.latches.Latch;
import org.codehaus.multiverse.util.latches.OpenLatch;
import org.codehaus.multiverse.util.latches.StandardLatch;

/**
 * todo: we needs to some really good concurrent tests for this class.
 */
public class VersionedLatchGroupTest extends TestCase {
    private VersionedLatchGroup latchGroup;
    private long initialActiveVersion;

    public void setUp() {
        initialActiveVersion = 10;
        latchGroup = new VersionedLatchGroup(initialActiveVersion);
    }

    public void assertActiveVersionHasNotChanges() {
        assertEquals(initialActiveVersion, latchGroup.getActiveVersion());
    }

    public void assertActiveVersion(long expectedActiveVersion) {
        assertEquals(expectedActiveVersion, latchGroup.getActiveVersion());
    }

    public void assertIsOpen(Latch... latches) {
        for (Latch latch : latches)
            assertTrue(latch.isOpen());
    }

    public void assertIsClosed(Latch... latches) {
        for (Latch latch : latches)
            assertFalse(latch.isOpen());
    }

    // ================== add latch ===========================

    public void testAddLatch_nullLatch() {
        try {
            latchGroup.addLatch(initialActiveVersion + 1, null);
            fail();
        } catch (NullPointerException ex) {
        }
    }

    public void testAddLatch_openLatch() {
        Latch latch = OpenLatch.INSTANCE;
        latchGroup.addLatch(latchGroup.getActiveVersion() + 10, latch);

        assertIsOpen(latch);
        assertActiveVersionHasNotChanges();
    }

    public void testAddLatch_minimalTriggerVersionAlreadyHasPassed() {
        testAddLatch_minimalTriggerVersion(initialActiveVersion);
    }

    public void testAddLatch_minimalTrigger() {
        testAddLatch_minimalTriggerVersion(initialActiveVersion - 1);
    }

    public void testAddLatch_minimalTriggerVersion(long version) {
        Latch latch = new StandardLatch();
        latchGroup.addLatch(version, latch);

        assertIsOpen(latch);
        assertActiveVersionHasNotChanges();
    }

    public void testAddLatch_minimalTriggerHasNotHappenedYet() {
        Latch latch = new StandardLatch();
        latchGroup.addLatch(initialActiveVersion + 1, latch);

        assertIsClosed(latch);
        assertActiveVersionHasNotChanges();
    }

    // ==================== activateVersion ===================

    public void testActivateVersion_noChange() {
        Latch latch1 = new StandardLatch();
        Latch latch2 = new StandardLatch();

        latchGroup.addLatch(initialActiveVersion + 1, latch1);
        latchGroup.addLatch(initialActiveVersion + 2, latch2);

        latchGroup.activateVersion(initialActiveVersion);

        assertActiveVersionHasNotChanges();
        assertIsClosed(latch1, latch2);
    }

    public void testActivateVersion_versionIsSmaller() {
        Latch latch1 = new StandardLatch();
        Latch latch2 = new StandardLatch();

        latchGroup.addLatch(initialActiveVersion + 1, latch1);
        latchGroup.addLatch(initialActiveVersion + 2, latch2);

        latchGroup.activateVersion(initialActiveVersion - 1);

        assertActiveVersionHasNotChanges();
        assertIsClosed(latch1, latch2);
    }

    public void testActivateVersion_versionIsBigger() {
        Latch latch1 = new StandardLatch();
        Latch latch2 = new StandardLatch();
        Latch latch3 = new StandardLatch();

        latchGroup.addLatch(initialActiveVersion + 1, latch1);
        latchGroup.addLatch(initialActiveVersion + 2, latch2);
        latchGroup.addLatch(initialActiveVersion + 3, latch3);

        latchGroup.activateVersion(initialActiveVersion + 2);

        assertActiveVersion(initialActiveVersion + 2);
        assertIsOpen(latch1);
        assertIsOpen(latch2);
        assertIsClosed(latch3);
    }
}

