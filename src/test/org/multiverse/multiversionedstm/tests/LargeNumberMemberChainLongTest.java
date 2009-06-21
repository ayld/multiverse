package org.multiverse.multiversionedstm.tests;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.multiversionedstm.manualinstrumented.ManualPair;

public class LargeNumberMemberChainLongTest {
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
    public void testTree_5() {
        ManualPair pair = createTree(5);
        Handle<ManualPair> handle = commit(stm, pair);

        Transaction t = stm.startTransaction();
        ManualPair found = t.read(handle);
        assertEquals(pair, found);
    }

    @Test
    public void testTree_10() {
        ManualPair pair = createTree(10);
        Handle<ManualPair> handle = commit(stm, pair);

        Transaction t = stm.startTransaction();
        ManualPair found = t.read(handle);
        assertEquals(pair, found);
    }


    @Test
    public void testTree_15() {
        ManualPair pair = createTree(15);
        Handle<ManualPair> handle = commit(stm, pair);

        Transaction t = stm.startTransaction();
        ManualPair found = t.read(handle);
        assertEquals(pair, found);
    }

    @Test
    public void testTree_20() {
        ManualPair pair = createTree(15);
        Handle<ManualPair> handle = commit(stm, pair);

        Transaction t = stm.startTransaction();
        ManualPair found = t.read(handle);
        assertEquals(pair, found);
    }


    public ManualPair createTree(int todo) {
        if (todo == 0) {
            return new ManualPair(1, 1);
        } else {
            ManualPair left = createTree(todo - 1);
            ManualPair right = createTree(todo - 1);
            return new ManualPair(left, right);
        }
    }

}
