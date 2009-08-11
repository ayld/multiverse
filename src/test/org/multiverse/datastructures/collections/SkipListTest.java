package org.multiverse.datastructures.collections;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.assertNoInstrumentationProblems;
import org.multiverse.api.Stm;
import org.multiverse.utils.GlobalStmInstance;

public class SkipListTest {
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
    public void testConstruction() {
        SkipList skipList = new SkipList();
        assertEquals(0, skipList.size());
    }
}
