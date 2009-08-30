package org.multiverse.datastructures.collections;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.assertNoInstrumentationProblems;
import org.multiverse.api.Stm;
import org.multiverse.utils.GlobalStmInstance;

/**
 * @author Peter Veentjer
 */
public class DoubleLinkedListTest {
    private Stm stm;

    @Before
    public void setUp() {
        stm = GlobalStmInstance.get();

        //todo: force load of 
        new DoubleLinkedList.IteratorImpl(null);
        new DoubleLinkedList.LinkedNode(null);
    }

    @After
    public void tearDown() {
        assertNoInstrumentationProblems();
    }

    @Test
    public void constructionNoArg() {
        long version = stm.getClockVersion();

        DoubleLinkedList<String> l = new DoubleLinkedList<String>();

        assertEquals(version+1, stm.getClockVersion());
        assertEquals(0, l.size());
        assertEquals(Integer.MAX_VALUE, l.getMaxCapacity());
    }

    @Test
    public void constructionWithMaxCapacity(){
        long version = stm.getClockVersion();

        DoubleLinkedList<String> l = new DoubleLinkedList<String>(10);

        assertEquals(version+1, stm.getClockVersion());
        assertEquals(0, l.size());
        assertEquals(10, l.getMaxCapacity());
    }
}
