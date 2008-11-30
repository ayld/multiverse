package org.codehaus.multiverse.util.latches;

import junit.framework.TestCase;

import static java.util.Arrays.asList;
import java.util.HashSet;

import org.codehaus.multiverse.util.latches.CheapLatch;
import org.codehaus.multiverse.util.latches.Latch;
import org.codehaus.multiverse.util.latches.LatchGroup;

public class LatchGroupTest extends TestCase {
    private LatchGroup latchGroup;

    @Override
    public void setUp() {
        latchGroup = new LatchGroup();
    }

    public void assertIsOpen(boolean open) {
        assertEquals(open, latchGroup.isOpen());
    }

    public void assertIsOpen(Latch latch, boolean isOpen) {
        assertEquals(isOpen, latch.isOpen());
    }

    public void assertNoLatches() {
        assertTrue(latchGroup.getLatches().isEmpty());
    }

    public void assertLatches(Latch... expected) {
        assertEquals(latchGroup.getLatches(), new HashSet(asList(expected)));
    }

    public void testConstructed() {
        assertIsOpen(false);
    }

    public void testOpenEmpty() {
        latchGroup.open();
        assertIsOpen(true);
    }

    public void testOpen() {
        Latch latch1 = new CheapLatch();
        Latch latch2 = new CheapLatch();
        latchGroup.add(latch1);
        latchGroup.add(latch2);
        latchGroup.open();

        assertIsOpen(true);
        assertNoLatches();
        assertIsOpen(latch1, true);
        assertIsOpen(latch2, true);
    }

    public void testAddOpenLatchToClosedLatchGroup() {
        Latch latch = new CheapLatch(true);
        latchGroup.add(latch);

        assertIsOpen(false);
        assertNoLatches();
    }

    public void testAddClosedLatchToOpenedLatchGroup() {
        latchGroup.open();

        Latch latch = new CheapLatch();
        latchGroup.add(latch);

        assertIsOpen(true);
        assertNoLatches();
        assertIsOpen(latch, true);
    }

    public void testAddOpenedLatchToOpenedLatchGroup() {
        latchGroup.open();

        Latch latch = new CheapLatch(true);
        latchGroup.add(latch);

        assertIsOpen(true);
        assertNoLatches();
        assertIsOpen(latch, true);
    }

    public void testClosedLatchToClosedLatchGroup() {
        Latch latch1 = new CheapLatch();
        Latch latch2 = new CheapLatch();
        latchGroup.add(latch1);
        latchGroup.add(latch2);

        assertIsOpen(false);
        assertLatches(latch1, latch2);
        assertIsOpen(latch1, false);
        assertIsOpen(latch2, false);
    }
}
