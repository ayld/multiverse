package org.multiverse.multiversionedstm.tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.multiversionedstm.examples.ExamplePair;

/**
 * A Test to see how well the MultiversionedStm deals with cycles
 *
 * @author Peter Veentjer.
 */
public class CycleHandlingTest {
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
    public void testDirectCycle() {
        ExamplePair pair = new ExamplePair();
        pair.setLeft(pair);

        Handle<ExamplePair> handle = commit(stm, pair);
        Transaction t = stm.startTransaction();
        ExamplePair found = t.read(handle);
        //todo
    }

    @Test
    public void testShortIndirectCycle() {
        //todo
    }

    @Test
    public void testLongIndirectCycle() {
        //todo
    }

    @Test
    public void testNuttyObjectGraphWithLoadsOfCycles() {
        //todo
    }
}
