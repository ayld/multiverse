package org.multiverse.datastructures.collections;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.assertNoInstrumentationProblems;
import org.multiverse.utils.GlobalStmInstance;
import org.multiverse.api.Stm;

/**
 * @author Peter Veentjer
 */
public class SetTest {
    private Stm stm;

    @Before
    public void setUp() {
       stm = GlobalStmInstance.get();
    }

    @After
    public void tearDown() {
        assertNoInstrumentationProblems();
    }

    @Test
    public void test() {
        Set set = new Set();
        assertEquals(0, set.size());
        assertTrue(set.isEmpty());
    }

    @Test
    public void add() {
        Set<String> set = new Set<String>();
        assertTrue(set.add("a"));
        assertFalse(set.add("a"));
        assertTrue(set.add("b"));
        assertEquals(2, set.size());
    }

    @Test
    public void toStringEmptySet() {
        Set<String> set = new org.multiverse.datastructures.collections.Set<String>();
        assertEquals("[]", set.toString());
    }

    @Test
    public void toStringSingleton() {
        Set<String> set = new Set<String>();
        set.add("foo");
        assertEquals("[foo]", set.toString());
    }

    @Test
    public void toStringFilledSet() {
        Set<String> set = new Set<String>();
        set.add("a");
        set.add("b");

        String s = set.toString();
        assertTrue(s.equals("[a,b]") || s.equals("[b,a]"));
    }
}
