package org.multiverse.multiversionedstm.examples;

import static junit.framework.Assert.assertEquals;
import org.junit.After;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
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
        Handle<Pair<Integer, Integer>> handle = commit(stm, pair);

        Transaction t = stm.startTransaction();
        Pair<Integer, Integer> found = t.read(handle);
        assertNotNull(found);
        assertEquals(left, found.getLeft());
        assertEquals(right, found.getRight());
    }

    @Test
    public void testPersistSimpleMaterializedMembers() {
        IntegerValue left = new IntegerValue(10);
        IntegerValue right = new IntegerValue(20);
        Pair<IntegerValue, IntegerValue> pair = new Pair<IntegerValue, IntegerValue>(left, right);
        Handle<Pair<IntegerValue, IntegerValue>> handle = commit(stm, pair);

        Transaction t = stm.startTransaction();
        Pair<IntegerValue, IntegerValue> found = t.read(handle);
        assertNotNull(found);
        assertEquals(left, found.getLeft());
        assertEquals(right, found.getRight());
    }
}
