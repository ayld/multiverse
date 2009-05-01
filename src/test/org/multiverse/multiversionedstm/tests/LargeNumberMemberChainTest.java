package org.multiverse.multiversionedstm.tests;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Originator;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.multiversionedstm.examples.Pair;

public class LargeNumberMemberChainTest {
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
        Pair pair = createTree(5);
        Originator<Pair> originator = commit(stm, pair);

        Transaction t = stm.startTransaction();
        Pair found = t.read(originator);
        assertEquals(pair, found);
    }

    @Test
    public void testTree_10() {
        Pair pair = createTree(10);
        Originator<Pair> originator = commit(stm, pair);

        Transaction t = stm.startTransaction();
        Pair found = t.read(originator);
        assertEquals(pair, found);
    }


    @Test
    public void testTree_15() {
        Pair pair = createTree(15);
        Originator<Pair> originator = commit(stm, pair);

        Transaction t = stm.startTransaction();
        Pair found = t.read(originator);
        assertEquals(pair, found);
    }

    @Test
    public void testTree_20() {
        Pair pair = createTree(15);
        Originator<Pair> originator = commit(stm, pair);

        Transaction t = stm.startTransaction();
        Pair found = t.read(originator);
        assertEquals(pair, found);
    }


    public Pair createTree(int todo) {
        if (todo == 0) {
            return new Pair(1, 1);
        } else {
            Pair left = createTree(todo - 1);
            Pair right = createTree(todo - 1);
            return new Pair(left, right);
        }
    }

}
