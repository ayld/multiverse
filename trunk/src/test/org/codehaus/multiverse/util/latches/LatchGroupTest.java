package org.codehaus.multiverse.util.latches;

import junit.framework.TestCase;
import org.codehaus.multiverse.TestUtils;

import static java.util.Arrays.asList;
import java.util.HashSet;

public class LatchGroupTest extends TestCase {
    private LatchGroup latchGroup;

    @Override
    public void setUp() {
        latchGroup = new LatchGroup();
    }

    public void assertIsOpen(boolean open) {
        assertEquals(open, latchGroup.isOpen());
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
        TestUtils.assertIsOpen(latch1, true);
        TestUtils.assertIsOpen(latch2, true);
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
        TestUtils.assertIsOpen(latch, true);
    }

    public void testAddOpenedLatchToOpenedLatchGroup() {
        latchGroup.open();

        Latch latch = new CheapLatch(true);
        latchGroup.add(latch);

        assertIsOpen(true);
        assertNoLatches();
        TestUtils.assertIsOpen(latch, true);
    }

    public void testClosedLatchToClosedLatchGroup() {
        Latch latch1 = new CheapLatch();
        Latch latch2 = new CheapLatch();
        latchGroup.add(latch1);
        latchGroup.add(latch2);

        assertIsOpen(false);
        assertLatches(latch1, latch2);
        TestUtils.assertIsOpen(latch1, false);
        TestUtils.assertIsOpen(latch2, false);
    }
}
