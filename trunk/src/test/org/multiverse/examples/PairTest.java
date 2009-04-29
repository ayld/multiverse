package org.multiverse.examples;

import static junit.framework.Assert.assertEquals;
import org.junit.After;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Originator;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;

public class PairTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @After
    public void tearDown() {
        System.out.println(stm.getStatistics());
    }

    @Test
    public void testPersistWithoutMaterializedMembers() {
        Integer left = 10;
        Integer right = 20;
        Pair<Integer, Integer> pair = new Pair<Integer, Integer>(left, right);
        Originator<Pair<Integer, Integer>> originator = commit(stm, pair);

        Transaction t = stm.startTransaction();
        Pair<Integer, Integer> found = t.read(originator);
        assertNotNull(found);
        assertEquals(left, found.getLeft());
        assertEquals(right, found.getRight());
    }

    @Test
    public void testPersistSimpleMaterializedMembers() {
        IntegerValue left = new IntegerValue(10);
        IntegerValue right = new IntegerValue(20);
        Pair<IntegerValue, IntegerValue> pair = new Pair<IntegerValue, IntegerValue>(left, right);
        Originator<Pair<IntegerValue, IntegerValue>> originator = commit(stm, pair);

        Transaction t = stm.startTransaction();
        Pair<IntegerValue, IntegerValue> found = t.read(originator);
        assertNotNull(found);
        assertEquals(left, found.getLeft());
        assertEquals(right, found.getRight());
    }
}
