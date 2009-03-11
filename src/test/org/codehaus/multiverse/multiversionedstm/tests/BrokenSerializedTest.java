package org.codehaus.multiverse.multiversionedstm.tests;

import static org.codehaus.multiverse.TestUtils.commit;
import org.codehaus.multiverse.api.Transaction;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import org.codehaus.multiverse.multiversionedstm.examples.Stack;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

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
    private long stackHandle1;
    private long stackHandle2;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
        stackHandle1 = commit(stm, new Stack());
        stackHandle2 = commit(stm, new Stack());
    }

    @Test
    public void test() {
        Transaction t1 = stm.startTransaction();
        Transaction t2 = stm.startTransaction();

        Stack t1Stack1 = (Stack) t1.read(stackHandle1);
        Stack t1Stack2 = (Stack) t1.read(stackHandle2);
        t1Stack1.push(t1Stack2.size());

        Stack t2Stack1 = (Stack) t2.read(stackHandle1);
        Stack t2Stack2 = (Stack) t2.read(stackHandle2);
        t2Stack2.push(t2Stack1.size());

        t1.commit();
        t2.commit();

        assertStackContainZero(stackHandle1);
        assertStackContainZero(stackHandle2);
    }

    public void assertStackContainZero(long stackHandle) {
        Transaction t = stm.startTransaction();
        Stack stack = (Stack) t.read(stackHandle);
        //only one element on the list
        assertEquals(1, stack.size());
        //the top element is zero
        assertEquals(0, stack.pop());
        t.commit();
    }
}
