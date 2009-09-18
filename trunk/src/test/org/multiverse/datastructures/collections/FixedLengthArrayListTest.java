package org.multiverse.datastructures.collections;

import static junit.framework.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.utils.GlobalStmInstance;

import java.util.List;

public class FixedLengthArrayListTest {
    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
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

    }

    @Test
    public void indexOfOnNonEmptyListAndNotFound() {

    }

    @Test
    public void indexOfNonEmptyListAndFound() {

    }

    // ===========lastIndexOf=======================

    @Test
    public void lastIndexOfOnEmptyList() {

    }

    @Test
    public void lastIndexfOnNonEmptyListAndNotFound() {

    }

    @Test
    public void lastIndexOfNonEmptyListAndFound() {

    }

    // ================= set =========================

    @Test
    public void setIndexTooSmall() {

    }

    @Test
    public void setIndexTooLarge() {

    }

    @Test
    public void setNull() {

    }

    @Test
    public void setFirstTime() {

    }

    @Test
    public void setOverwrite() {

    }
}
