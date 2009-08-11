package org.multiverse.stms.alpha.instrumentation.asm;

import org.junit.After;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.alpha.Tranlocal;
import org.multiverse.api.Transaction;
import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.getThreadLocalTransaction;

/**
 * @author Peter Veentjer
 */
public class AtomicObject_AttachAsNewTest {

    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
    }

    @After
    public void tearDown() {
        //assertNoInstrumentationProblems();
    }

    @Test
    public void test() {
        CheckingConstructor foo = new CheckingConstructor();
    }

    @AtomicObject
    public static class CheckingConstructor {
        private int field;

        public CheckingConstructor() {
            Transaction t = getThreadLocalTransaction();
            assertNotNull(t);
            Tranlocal tranlocal = t.load(this);
            assertNotNull(tranlocal);
            assertSame(this, tranlocal.getAtomicObject());
        }
    }
}
