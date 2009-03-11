package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.api.Transaction;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class HerlihyIntegerSetTest {
    private HerlihyIntegerSet set;
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void testEquals() {
        assertEquals(new HerlihyIntegerSet(), new HerlihyIntegerSet());
        assertEquals(new HerlihyIntegerSet(1, 2, 3), new HerlihyIntegerSet(1, 2, 3));
        assertFalse(new HerlihyIntegerSet(1, 2, 3).equals(new HerlihyIntegerSet(1, 2)));
    }

    @Test
    public void test() {
        set = new HerlihyIntegerSet();
        assertEquals(0, set.size());

        //add first
        assertTrue(set.add(1));
        assertEquals(1, set.size());

        //add duplicate
        assertFalse(set.add(1));
        assertEquals(1, set.size());

        //add at the end
        assertTrue(set.add(10));
        assertEquals(2, set.size());

        //add at the end
        assertTrue(set.add(20));
        assertEquals(3, set.size());

        //add in the middle
        assertTrue(set.add(15));
        assertEquals(4, set.size());

        //add in front
        assertTrue(set.add(-5));
        assertEquals(5, set.size());
    }

    @Test
    public void testPersistEmptySet() {
        testPersist();
    }

    @Test
    public void testPersistSingletonSet() {
        testPersist(1);
    }

    @Test
    public void testPersistSeriousSet() {
        testPersist(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    public void testPersist(int... values) {
        set = new HerlihyIntegerSet(values);

        Transaction t1 = stm.startTransaction();
        long handle = t1.attachAsRoot(set);
        t1.commit();

        Transaction t2 = stm.startTransaction();
        HerlihyIntegerSet loadedSet = (HerlihyIntegerSet) t2.read(handle);
        t2.commit();

        assertEquals(set, loadedSet);
    }
}
