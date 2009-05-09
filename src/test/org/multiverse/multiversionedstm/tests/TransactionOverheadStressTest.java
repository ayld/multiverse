package org.multiverse.multiversionedstm.tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.multiversionedstm.examples.Stack;

public class TransactionOverheadStressTest {
    private MultiversionedStm stm;
    private long itemCount = 10000000;
    private long startMs;
    private long endMs;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @After
    public void tearDown() {
        System.out.println(stm.getStatistics());

        long timeMs = (endMs - startMs) + 1;
        System.out.println(String.format("%s items took %s ms", itemCount, timeMs));
        double performance = itemCount / (timeMs / 1000.0);
        System.out.println(String.format("%s items/second", performance));

    }

    @Test
    public void testWithTransaction() {
        Handle<Stack> handle = commit(stm, new Stack());

        startMs = System.currentTimeMillis();
        for (int k = 0; k < itemCount; k++) {
            Transaction t1 = stm.startTransaction();
            Stack s1 = t1.read(handle);
            s1.push("item");
            s1.pop();
            t1.commit();
        }
        endMs = System.currentTimeMillis();
    }

    @Test
    public void testWithAtomicPushAndPop() {
        Handle<Stack> handle = commit(stm, new Stack());

        startMs = System.currentTimeMillis();
        for (int k = 0; k < itemCount; k++) {
            Transaction t1 = stm.startTransaction();
            Stack s1 = t1.read(handle);
            s1.push("item");
            t1.commit();

            Transaction t2 = stm.startTransaction();
            Stack s2 = t2.read(handle);
            s2.pop();
            t2.commit();
        }
        endMs = System.currentTimeMillis();
    }

    @Test
    public void testWithoutTransaction() {
        Stack stack = new Stack();

        startMs = System.currentTimeMillis();
        for (int k = 0; k < itemCount; k++) {
            stack.push("item");
            stack.pop();
        }
        endMs = System.currentTimeMillis();
    }
}
