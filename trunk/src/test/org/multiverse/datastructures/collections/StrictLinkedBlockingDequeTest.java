package org.multiverse.datastructures.collections;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.utils.GlobalStmInstance;

import java.util.Collection;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingDeque;

public class StrictLinkedBlockingDequeTest {
    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
    }

    // ============ getFirst ===========================

    @Test
    public void getFirstFromEmptyDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();

        long version = stm.getClockVersion();

        try {
            deque.getFirst();
            fail();
        } catch (NoSuchElementException expected) {
        }

        assertEquals(version, stm.getClockVersion());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void getFirstFromSingleElementDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");
        long version = stm.getClockVersion();

        String result = deque.getFirst();

        assertEquals("1", result);
        assertEquals(version, stm.getClockVersion());
        assertEquals("[1]", deque.toString());
    }

    @Test
    public void getFirstFromNonEmptyDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");
        deque.add("2");
        long version = stm.getClockVersion();

        String result = deque.getFirst();

        assertEquals("1", result);
        assertEquals(version, stm.getClockVersion());
        assertEquals("[1, 2]", deque.toString());
    }

    // ============ getLast ===========================

    @Test
    public void getLastFromEmptyDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();

        long version = stm.getClockVersion();

        try {
            deque.getLast();
            fail();
        } catch (NoSuchElementException expected) {
        }

        assertEquals(version, stm.getClockVersion());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void getLastFromSingleElementDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");
        long version = stm.getClockVersion();

        String result = deque.getLast();

        assertEquals("1", result);
        assertEquals(version, stm.getClockVersion());
        assertEquals("[1]", deque.toString());
    }

    @Test
    public void getLastFromNonEmptyDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");
        deque.add("2");
        long version = stm.getClockVersion();

        String result = deque.getLast();

        assertEquals("2", result);
        assertEquals(version, stm.getClockVersion());
        assertEquals("[1, 2]", deque.toString());
    }

    // ============= peekFirst ==========================

    @Test
    public void peekFirstOnEmptyDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();

        long version = stm.getClockVersion();
        String result = deque.peekFirst();
        assertNull(result);
        assertEquals(version, stm.getClockVersion());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void peekFirstOnNonEmptyDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");
        deque.add("2");

        long version = stm.getClockVersion();
        String result = deque.peekFirst();
        assertEquals("1", result);
        assertEquals(version, stm.getClockVersion());
        assertEquals("[1, 2]", deque.toString());
    }

    // ============= peekLast ===========================

    @Test
    public void peekLastOnEmptyDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();

        long version = stm.getClockVersion();
        String result = deque.peekLast();
        assertNull(result);
        assertEquals(version, stm.getClockVersion());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void peekLastOnNonEmptyDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");
        deque.add("2");

        long version = stm.getClockVersion();
        String result = deque.peekLast();
        assertEquals("2", result);
        assertEquals(version, stm.getClockVersion());
        assertEquals("[1, 2]", deque.toString());
    }

    // ============ addFirst =============================

    @Test
    public void addFirstWithNullFails() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();

        long version = stm.getClockVersion();
        try {
            deque.addFirst(null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertEquals(version, stm.getClockVersion());
        assertEquals(0, deque.size());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void addFirstOnEmptyDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();

        long version = stm.getClockVersion();
        deque.addFirst("1");

        assertEquals(1, deque.size());
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[1]", deque.toString());
    }

    @Test
    public void addFirstOnNonEmptyDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.addFirst("1");

        long version = stm.getClockVersion();

        deque.addFirst("2");
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(2, deque.size());
        assertEquals("[2, 1]", deque.toString());
    }

    @Test
    public void addFirstWithFullDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>(1);
        deque.addFirst("1");

        long version = stm.getClockVersion();
        try {
            deque.addFirst("2");
            fail();
        } catch (IllegalStateException expected) {
        }

        assertEquals(version, stm.getClockVersion());
        assertEquals(1, deque.size());
        assertEquals("[1]", deque.toString());
    }

    // =====================addLast ====================

    @Test
    public void addLastWithNullFails() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();

        long version = stm.getClockVersion();
        try {
            deque.addLast(null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertEquals(0, deque.size());
        assertEquals(version, stm.getClockVersion());
    }

    @Test
    public void addLastOnEmptyDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();

        long version = stm.getClockVersion();
        deque.addLast("1");
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[1]", deque.toString());
    }

    @Test
    public void addLastOnNonEmptyDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.addLast("1");

        long version = stm.getClockVersion();
        deque.addLast("2");
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[1, 2]", deque.toString());
    }

    @Test
    public void addLastOnFullDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>(1);
        deque.add("1");

        long version = stm.getClockVersion();

        try {
            deque.addLast("2");
            fail();
        } catch (IllegalStateException ex) {
        }

        assertEquals(version, stm.getClockVersion());
        assertEquals("[1]", deque.toString());
        assertEquals(1, deque.size());
    }

    // ============= add =================================

    @Test
    public void addWithNullFails() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();

        long version = stm.getClockVersion();
        try {
            deque.add(null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertEquals(0, deque.size());
        assertEquals(version, stm.getClockVersion());
    }

    @Test
    public void addOnEmptyDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();

        long version = stm.getClockVersion();
        boolean result = deque.add("1");
        assertTrue(result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[1]", deque.toString());
    }

    @Test
    public void addOnNonEmptyDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");

        long version = stm.getClockVersion();
        boolean result = deque.add("2");
        assertTrue(result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[1, 2]", deque.toString());
    }

    @Test
    public void addOnFullDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>(1);
        deque.add("1");

        long version = stm.getClockVersion();

        try {
            deque.add("2");
            fail();
        } catch (IllegalStateException ex) {
        }

        assertEquals(version, stm.getClockVersion());
        assertEquals("[1]", deque.toString());
        assertEquals(1, deque.size());
    }

    // =========== offerFirst ==================

    @Test
    public void offerFirstNullFails() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        long version = stm.getClockVersion();

        try {
            deque.offerFirst(null);
            fail();
        } catch (NullPointerException ex) {

        }
        assertEquals(version, stm.getClockVersion());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void offerFirstOnEmptyDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        long version = stm.getClockVersion();

        boolean result = deque.offerFirst("1");
        assertTrue(result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[1]", deque.toString());
    }

    @Test
    public void offerFirstOnNonEmptyDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");
        deque.add("2");

        long version = stm.getClockVersion();
        boolean result = deque.offerFirst("3");
        assertTrue(result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[3, 1, 2]", deque.toString());
    }

    @Test
    public void offerFirstOnFullDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>(1);
        deque.add("1");

        long version = stm.getClockVersion();
        boolean result = deque.offerFirst("2");
        assertFalse(result);
        assertEquals(version, stm.getClockVersion());
        assertEquals("[1]", deque.toString());
    }
    // ====================== offerLast =================

    @Test
    public void offerLastNullFails() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();

        long version = stm.getClockVersion();

        try {
            deque.offerLast(null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertEquals(version, stm.getClockVersion());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void offerLastOnEmptyQueue() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        long version = stm.getClockVersion();

        boolean result = deque.offerLast("1");
        assertTrue(result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[1]", deque.toString());
    }

    @Test
    public void offerLastOnNonEmptyQueue() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.offerLast("1");
        long version = stm.getClockVersion();

        boolean result = deque.offerLast("2");

        assertTrue(result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[1, 2]", deque.toString());
    }

    @Test
    public void offerLastOnFullQueue() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>(1);
        deque.offerLast("1");
        long version = stm.getClockVersion();

        boolean result = deque.offerLast("2");
        assertFalse(result);
        assertEquals(version, stm.getClockVersion());
        assertEquals("[1]", deque.toString());
    }

    // =================== putLast ======================

    @Test
    public void putLastNullFails() throws InterruptedException {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();

        long version = stm.getClockVersion();

        try {
            deque.putLast(null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertEquals(version, stm.getClockVersion());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void putLastOnEmptyDeque() throws InterruptedException {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        long version = stm.getClockVersion();

        deque.putLast("1");
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[1]", deque.toString());
    }

    @Test
    public void putLastOnNonEmptyDeque() throws InterruptedException {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");
        long version = stm.getClockVersion();

        deque.put("2");

        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[1, 2]", deque.toString());
    }

    @Test
    public void putLastOnFullDeque() {

    }

    // ==================== putfirst ====================

    @Test
    public void putFirstNullFails() throws InterruptedException {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();

        long version = stm.getClockVersion();

        try {
            deque.putFirst(null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertEquals(version, stm.getClockVersion());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void putFirstOnEmptyDeque() throws InterruptedException {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        long version = stm.getClockVersion();

        deque.putFirst("1");
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[1]", deque.toString());
    }

    @Test
    public void putFirstOnNonEmptyDeque() throws InterruptedException {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");
        long version = stm.getClockVersion();

        deque.putFirst("2");

        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[2, 1]", deque.toString());
    }

    @Test
    public void putFirstOnFullDeque() {

    }
    // ===================== takeFirst ===============

    @Test
    public void takeFirstFromEmptyDeque() {

    }

    @Test
    public void takeFirstFromDequeWithSingleItem() throws InterruptedException {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");

        long version = stm.getClockVersion();
        String result = deque.takeFirst();
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("1", result);
        assertEquals(0, deque.size());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void takeFirstFromDequeWithMultipleItems() throws InterruptedException {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");
        deque.add("2");

        long version = stm.getClockVersion();
        String result = deque.takeFirst();
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("1", result);
        assertEquals(1, deque.size());
        assertEquals("[2]", deque.toString());
    }

    // ====================== takeLast ===============

    @Test
    public void takeLastFromEmptyDeque() {

    }

    @Test
    public void takeLastFromDequeWithSingleItem() throws InterruptedException {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");

        long version = stm.getClockVersion();
        String result = deque.takeLast();
        assertEquals("1", result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(0, deque.size());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void takeLastFromDequeWithMultipleItems() throws InterruptedException {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");
        deque.add("2");

        long version = stm.getClockVersion();
        String result = deque.takeLast();
        assertEquals("2", result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(1, deque.size());
        assertEquals("[1]", deque.toString());
    }

    // ====================== removeFirst ============

    @Test
    public void removeFirstFromEmptyDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();

        long version = stm.getClockVersion();
        try {
            deque.removeFirst();
            fail();
        } catch (NoSuchElementException expected) {

        }
        assertEquals(0, deque.size());
        assertEquals("[]", deque.toString());
        assertEquals(version + 1, stm.getClockVersion());
    }

    @Test
    public void removeFirstFromDequeWithSingleItem() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");

        long version = stm.getClockVersion();
        String result = deque.removeFirst();
        assertEquals("1", result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(0, deque.size());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void removeFirstFromDequeWithMultipleItems() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");
        deque.add("2");

        long version = stm.getClockVersion();
        String result = deque.removeFirst();
        assertEquals("1", result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(1, deque.size());
        assertEquals("[2]", deque.toString());
    }

    // ====================== removeLast =============

    @Test
    public void removeLastFromEmptyDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();

        long version = stm.getClockVersion();
        try {
            deque.removeLast();
            fail();
        } catch (NoSuchElementException expected) {
        }
        assertEquals(0, deque.size());
        assertEquals("[]", deque.toString());
        assertEquals(version + 1, stm.getClockVersion());
    }

    @Test
    public void removeLastFromDequeWithSingleItem() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");

        long version = stm.getClockVersion();
        String result = deque.removeLast();
        assertEquals("1", result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(0, deque.size());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void removeLastFromDequeWithMultipleItems() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");
        deque.add("2");

        long version = stm.getClockVersion();
        String result = deque.removeLast();
        assertEquals("2", result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(1, deque.size());
        assertEquals("[1]", deque.toString());
    }


    // ====================== poll ===================

    @Test
    public void pollFromEmptyDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();

        long version = stm.getClockVersion();
        String result = deque.poll();
        assertNull(result);
        assertEquals(version, stm.getClockVersion());
        assertEquals(0, deque.size());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void pollFromDequeWithSingleItem() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");

        long version = stm.getClockVersion();
        String result = deque.poll();
        assertEquals("1", result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(0, deque.size());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void pollFromDequeWithMultipleItems() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");
        deque.add("2");

        long version = stm.getClockVersion();
        String result = deque.poll();
        assertEquals("1", result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(1, deque.size());
        assertEquals("[2]", deque.toString());
    }

    // ====================== pollFirst ==============

    @Test
    public void pollFirstFromEmptyDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();

        long version = stm.getClockVersion();
        String result = deque.pollFirst();
        assertEquals(version, stm.getClockVersion());
        assertNull(result);
        assertEquals(0, deque.size());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void pollFirstFromDequeWithSingleItem() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");

        long version = stm.getClockVersion();
        String result = deque.pollFirst();
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("1", result);
        assertEquals(0, deque.size());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void pollFirstFromDequeWithMultipleItems() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");
        deque.add("2");

        long version = stm.getClockVersion();
        String result = deque.pollFirst();
        assertEquals("1", result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(1, deque.size());
        assertEquals("[2]", deque.toString());
    }

    // ====================== pollLast ===============

    @Test
    public void pollLastFromEmptyDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();

        long version = stm.getClockVersion();
        String result = deque.pollLast();
        assertNull(result);
        assertEquals(version, stm.getClockVersion());
        assertEquals(0, deque.size());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void pollLastFromDequeWithSingleItem() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");

        long version = stm.getClockVersion();
        String result = deque.pollLast();
        assertEquals("1", result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(0, deque.size());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void pollLastFromDequeWithMultipleItems() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");
        deque.add("2");

        long version = stm.getClockVersion();
        String result = deque.pollLast();
        assertEquals("2", result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(1, deque.size());
        assertEquals("[1]", deque.toString());
    }

    // ==================== clear ====================

    @Test
    public void clearEmptyDeque() {
        StrictLinkedBlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        long version = stm.getClockVersion();

        deque.clear();
        assertEquals(version, stm.getClockVersion());
        assertTrue(deque.isEmpty());
        assertEquals("[]", deque.toString());
    }

    @Test
    public void clearNonEmptyDeque() {
        StrictLinkedBlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");
        deque.add("2");
        deque.add("3");

        long version = stm.getClockVersion();
        deque.clear();
        assertEquals(version + 1, stm.getClockVersion());
        assertTrue(deque.isEmpty());
        assertEquals("[]", deque.toString());
    }

    // ============== drainTo(Collection)=======

    //@Test
    public void drainToWithEmptyDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();

        Collection<String> c = new LinkedList<String>();
        long version = stm.getClockVersion();
        int result = deque.drainTo(c);
        assertEquals(0, result);
        assertEquals(version, stm.getClockVersion());
        assertTrue(c.isEmpty());
        assertEquals(0, deque.size());
    }

    //@Test
    public void drainToWithNonEmptyDeque() {
        BlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        deque.add("1");
        deque.add("2");
        deque.add("3");

        Collection<String> c = new LinkedList<String>();
        long version = stm.getClockVersion();
        int result = deque.drainTo(c);
        assertEquals(3, result);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("[1, 2, 3]", c.toString());
        assertEquals(0, deque.size());
    }

    // ========================================

    @Test
    public void equals() {

    }

    @Test
    public void hash() {

    }

    @Test
    public void testToString() {
        StrictLinkedBlockingDeque<String> deque = new StrictLinkedBlockingDeque<String>();
        assertEquals("[]", deque.toString());

        deque.add("1");
        assertEquals("[1]", deque.toString());

        deque.add("2");
        deque.add("3");
        assertEquals("[1, 2, 3]", deque.toString());
    }
}
