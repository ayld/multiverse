package org.multiverse.multiversionedstm.tests;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.multiversionedstm.manualinstrumented.ManualIntValue;
import org.multiverse.multiversionedstm.manualinstrumented.ManualPair;

/**
 * Checks if the system is able to deal with long chains of objects. If for example recursion uses
 * uses to process the chain somehow, long chains would lead to stackoverflow problems. So this is a
 * test to make sure that the system is not suffering from those problems.
 * <p/>
 * The chain is a sequence of pairs:
 * - the left is always a integervalue
 * - the right is an integervalue (end of the chain) or also a pair.
 * so a chain of 10 elements, contains 11 values.
 *
 * @author Peter Veentjer.
 */
public class DeepMemberChainLongTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @After
    public void after() {
        System.out.println(stm.getStatistics());
    }

    @Test
    public void chainOf_1() {
        test(1);
    }

    @Test
    public void chainOf_10() {
        test(10);
    }

    @Test
    public void chainOf_100() {
        test(100);
    }

    @Test
    public void chainOf_1000() {
        test(1000);
    }

    @Test
    public void chainOf_10000() {
        test(10000);
    }

    @Test
    public void chainOf_100000() {
        test(100000);
    }

    //@Test
    //public void chainOf_1000000() {
    //    test(1000000);
    //}

    public void test(int length) {
        ManualPair pair = createValueChain(length);
        Handle<ManualPair> handle = commit(stm, pair);

        int updateCount = 10;
        for (int k = 0; k < updateCount; k++) {
            increaseAllValuesInChain(handle);
        }

        int total = totalValuesInChain(handle);
        assertEquals(updateCount * (length + 1), total);
    }

    public void increaseAllValuesInChain(Handle<ManualPair> handle) {
        Transaction t = stm.startTransaction();

        ManualPair pair = t.read(handle);
        do {
            ((ManualIntValue) pair.getLeft()).inc();

            Object right = pair.getRight();
            if (right instanceof ManualPair) {
                pair = (ManualPair) right;
            } else {
                ((ManualIntValue) right).inc();
                pair = null;
            }
        } while (pair != null);

        t.commit();
    }

    public int totalValuesInChain(Handle<ManualPair> handle) {
        Transaction t = stm.startTransaction();

        int total = 0;

        ManualPair pair = t.read(handle);
        do {
            total += ((ManualIntValue) pair.getLeft()).get();

            Object right = pair.getRight();
            if (right instanceof ManualPair) {
                pair = (ManualPair) right;
            } else {
                total += ((ManualIntValue) right).get();
                pair = null;
            }
        } while (pair != null);

        t.commit();

        return total;
    }

    public ManualPair createValueChain(int length) {
        if (length <= 0) throw new IllegalArgumentException();

        ManualPair pair = new ManualPair(new ManualIntValue(0), new ManualIntValue(0));

        for (int k = 0; k < length - 1; k++) {
            pair = new ManualPair(new ManualIntValue(0), pair);
        }

        return pair;
    }
}
