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

public class ExamplePairTest {
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
        ExamplePair<Integer, Integer> pair = new ExamplePair<Integer, Integer>(left, right);
        Handle<ExamplePair<Integer, Integer>> handle = commit(stm, pair);

        Transaction t = stm.startTransaction();
        ExamplePair<Integer, Integer> found = t.read(handle);
        assertNotNull(found);
        assertEquals(left, found.getLeft());
        assertEquals(right, found.getRight());
    }

    @Test
    public void testPersistSimpleMaterializedMembers() {
        ExampleIntegerValue left = new ExampleIntegerValue(10);
        ExampleIntegerValue right = new ExampleIntegerValue(20);
        ExamplePair<ExampleIntegerValue, ExampleIntegerValue> pair = new ExamplePair<ExampleIntegerValue, ExampleIntegerValue>(left, right);
        Handle<ExamplePair<ExampleIntegerValue, ExampleIntegerValue>> handle = commit(stm, pair);

        Transaction t = stm.startTransaction();
        ExamplePair<ExampleIntegerValue, ExampleIntegerValue> found = t.read(handle);
        assertNotNull(found);
        assertEquals(left, found.getLeft());
        assertEquals(right, found.getRight());
    }
}
