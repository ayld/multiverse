package org.multiverse.datastructures.collections;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.testIncomplete;
import org.multiverse.api.GlobalStmInstance;
import org.multiverse.api.Stm;

import java.util.List;

public class FixedLengthArrayListTest {
    private Stm stm;

    @Before
    public void setUp() {
        stm = GlobalStmInstance.get();
    }

    // ============ add ======================

    @Test
    public void addOnEmptyList() {
        List<String> list = new FixedLengthArrayList<String>(10);
        long version = stm.getClockVersion();

        list.add("foo");

        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(1, list.size());
        assertEquals("foo", list.get(0));
    }

    @Test
    public void addOnNonEmptyList() {
        List<String> list = new FixedLengthArrayList<String>(10);
        list.add("1");
        list.add("2");

        long version = stm.getClockVersion();

        list.add("3");

        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(3, list.size());
        assertEquals("1", list.get(0));
        assertEquals("2", list.get(1));
        assertEquals("3", list.get(2));
    }

    // ==============indexOf====================

    @Test
    public void indexOfOnEmptyList() {
        List<String> list = new FixedLengthArrayList<String>(10);
        int result = list.indexOf("foo");
        assertEquals(-1, result);
    }

    @Test
    public void indexOfOnNonEmptyListAndNotFound() {
        List<String> list = new FixedLengthArrayList<String>(10);
        list.add("1");
        list.add("2");
        list.add("3");

        int result = list.indexOf("foo");
        assertEquals(-1, result);
    }

    @Test
    public void indexOfNonEmptyListAndFound() {
        List<String> list = new FixedLengthArrayList<String>(10);
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("2");
        list.add("4");

        int result = list.indexOf("2");
        assertEquals(1, result);
    }

    // ===========lastIndexOf=======================

    @Test
    public void lastIndexOfOnEmptyList() {
        List<String> list = new FixedLengthArrayList<String>(10);
        int result = list.lastIndexOf("foo");
        assertEquals(-1, result);
    }

    @Test
    public void lastIndexfOnNonEmptyListAndNotFound() {
        List<String> list = new FixedLengthArrayList<String>(10);
        list.add("1");
        list.add("2");
        list.add("3");

        int result = list.lastIndexOf("foo");
        assertEquals(-1, result);
    }

    @Test
    public void lastIndexOfNonEmptyListAndFound() {
        List<String> list = new FixedLengthArrayList<String>(10);
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("2");
        list.add("4");

        int result = list.lastIndexOf("2");
        assertEquals(3, result);
    }

    // ================= set =========================

    @Test
    public void setIndexTooSmall() {
        List<String> list = new FixedLengthArrayList<String>(10);
        list.add("1");

        long version = stm.getClockVersion();
        try {
            list.set(-1, "");
            fail();
        } catch (IllegalArgumentException ex) {
        }

        assertEquals(version, stm.getClockVersion());
        //assertEquals("[1]",list.toString());
    }

    @Test
    public void setIndexTooLarge() {
        testIncomplete();
    }

    @Test
    public void setNull() {
        testIncomplete();
    }

    @Test
    public void setFirstTime() {
        testIncomplete();
    }

    @Test
    public void setOverwrite() {
        testIncomplete();
    }

    // ================== remove ====================

    @Test
    public void remove() {
        testIncomplete();
    }

    // ================= clear ======================

    @Test
    public void clearEmptyList() {
        FixedLengthArrayList<String> list = new FixedLengthArrayList<String>(10);

        long version = stm.getClockVersion();
        list.clear();

        assertEquals(version, stm.getClockVersion());
        assertEquals(0, list.size());
    }

    @Test
    public void clearNonEmptyList() {
        FixedLengthArrayList<String> list = new FixedLengthArrayList<String>(10);
        list.add("foo");
        list.add("bar");

        long version = stm.getClockVersion();
        list.clear();

        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(0, list.size());
    }

    // ====================== iterator ==============

    @Test
    public void testIterator() {
        testIncomplete();
    }

    // ========================== list iterator

    @Test
    public void testListIterator() {
        testIncomplete();
    }

    @Test
    public void testSublist() {
        testIncomplete();
    }
}
