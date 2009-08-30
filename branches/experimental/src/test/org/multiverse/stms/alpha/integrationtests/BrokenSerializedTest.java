package org.multiverse.stms.alpha.integrationtests;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.stms.alpha.AlphaTransaction;
import org.multiverse.stms.alpha.manualinstrumentation.IntStack;
import org.multiverse.stms.alpha.manualinstrumentation.IntStackTranlocal;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

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
    private IntStack stack1;
    private IntStack stack2;
    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
        setThreadLocalTransaction(null);
        stack1 = new IntStack();
        stack2 = new IntStack();
    }

    @After
    public void tearDown() {
        setThreadLocalTransaction(null);
    }

    @Test
    public void test() {
        AlphaTransaction t1 = (AlphaTransaction) stm.startUpdateTransaction(null);
        AlphaTransaction t2 = (AlphaTransaction) stm.startUpdateTransaction(null);

        IntStackTranlocal t1Stack1 = (IntStackTranlocal) t1.privatize(stack1);
        IntStackTranlocal t1Stack2 = (IntStackTranlocal) t1.privatize(stack2);
        t1Stack1.push(t1Stack2.size());

        IntStackTranlocal t2Stack1 = (IntStackTranlocal) t2.privatize(stack1);
        IntStackTranlocal t2Stack2 = (IntStackTranlocal) t2.privatize(stack2);
        t2Stack2.push(t2Stack1.size());

        t1.commit();
        t2.commit();

        assertStackContainZero(stack1);
        assertStackContainZero(stack2);
    }

    public void assertStackContainZero(IntStack stack) {
        assertEquals(1, stack.size());
        assertEquals(0, stack.pop());
    }
}
