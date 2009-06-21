package org.multiverse.multiversionedstm.manualinstrumented;

import static junit.framework.Assert.assertEquals;
import org.junit.After;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;

public class ManualPairTest {
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
        ManualPair<Integer, Integer> pair = new ManualPair<Integer, Integer>(left, right);
        Handle<ManualPair<Integer, Integer>> handle = commit(stm, pair);

        Transaction t = stm.startTransaction();
        ManualPair<Integer, Integer> found = t.read(handle);
        assertNotNull(found);
        assertEquals(left, found.getLeft());
        assertEquals(right, found.getRight());
    }

    @Test
    public void testPersistSimpleMaterializedMembers() {
        ManualIntValue left = new ManualIntValue(10);
        ManualIntValue right = new ManualIntValue(20);
        ManualPair<ManualIntValue, ManualIntValue> pair = new ManualPair<ManualIntValue, ManualIntValue>(left, right);
        Handle<ManualPair<ManualIntValue, ManualIntValue>> handle = commit(stm, pair);

        Transaction t = stm.startTransaction();
        ManualPair<ManualIntValue, ManualIntValue> found = t.read(handle);
        assertNotNull(found);
        assertEquals(left, found.getLeft());
        assertEquals(right, found.getRight());
    }
}
