package org.multiverse.multiversionedstm.tests;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.multiversionedstm.examples.ExamplePair;

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
        ExamplePair pair = createTree(5);
        Handle<ExamplePair> handle = commit(stm, pair);

        Transaction t = stm.startTransaction();
        ExamplePair found = t.read(handle);
        assertEquals(pair, found);
    }

    @Test
    public void testTree_10() {
        ExamplePair pair = createTree(10);
        Handle<ExamplePair> handle = commit(stm, pair);

        Transaction t = stm.startTransaction();
        ExamplePair found = t.read(handle);
        assertEquals(pair, found);
    }


    @Test
    public void testTree_15() {
        ExamplePair pair = createTree(15);
        Handle<ExamplePair> handle = commit(stm, pair);

        Transaction t = stm.startTransaction();
        ExamplePair found = t.read(handle);
        assertEquals(pair, found);
    }

    @Test
    public void testTree_20() {
        ExamplePair pair = createTree(15);
        Handle<ExamplePair> handle = commit(stm, pair);

        Transaction t = stm.startTransaction();
        ExamplePair found = t.read(handle);
        assertEquals(pair, found);
    }


    public ExamplePair createTree(int todo) {
        if (todo == 0) {
            return new ExamplePair(1, 1);
        } else {
            ExamplePair left = createTree(todo - 1);
            ExamplePair right = createTree(todo - 1);
            return new ExamplePair(left, right);
        }
    }

}
