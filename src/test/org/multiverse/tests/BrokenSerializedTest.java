package org.multiverse.tests;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Originator;
import org.multiverse.api.Transaction;
import org.multiverse.examples.Stack;
import org.multiverse.multiversionedstm.MultiversionedStm;

/**
 * MVCC suffers from a problem that serialized transactions are not completely serialized.
 * For more information check the following blog.
 * http://pveentjer.wordpress.com/2008/10/04/breaking-oracle-serializable/
 * This tests makes sure that the error is still in there and that the stm behaves like 'designed'.
 * <p/>
 * The test:
 * There are 2 empty stacks. In 2 parallel transactions, the size of the one is pushed as element
 * on the second, and the size of the second is pushed as element on the first. Afer both transactions
 * have committed, the stm contains 2 stacks with both the element 0 on them. So not a 1 and 0, or 0
 * and 1 as true serialized execution of transactions would do.
 *
 * @author Peter Veentjer.
 */
public class BrokenSerializedTest {
    private MultiversionedStm stm;
    private Originator<Stack> stackOriginator1;
    private Originator<Stack> stackOriginator2;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
        stackOriginator1 = commit(stm, new Stack());
        stackOriginator2 = commit(stm, new Stack());
    }

    @Test
    public void test() {
        Transaction t1 = stm.startTransaction();
        Transaction t2 = stm.startTransaction();

        Stack t1Stack1 = t1.read(stackOriginator1);
        Stack t1Stack2 = t1.read(stackOriginator2);
        t1Stack1.push(t1Stack2.size());

        Stack t2Stack1 = t2.read(stackOriginator1);
        Stack t2Stack2 = t2.read(stackOriginator2);
        t2Stack2.push(t2Stack1.size());

        t1.commit();
        t2.commit();

        assertStackContainZero(stackOriginator1);
        assertStackContainZero(stackOriginator2);
    }

    public void assertStackContainZero(Originator<Stack> stackOriginator) {
        Transaction t = stm.startTransaction();
        Stack stack = (Stack) t.read(stackOriginator);
        //only one element on the list
        assertEquals(1, stack.size());
        //the top element is zero
        assertEquals(0, stack.pop());
        t.commit();
    }
}
