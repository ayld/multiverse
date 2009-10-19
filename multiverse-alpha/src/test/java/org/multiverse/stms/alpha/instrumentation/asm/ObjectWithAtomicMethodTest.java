package org.multiverse.stms.alpha.instrumentation.asm;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.GlobalStmInstance;
import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.datastructures.refs.IntRef;
import org.multiverse.stms.alpha.AlphaStm;
import static org.multiverse.utils.TransactionThreadLocal.getThreadLocalTransaction;

/**
 * @author Peter Veentjer
 */
public class ObjectWithAtomicMethodTest {
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
        ObjectWithAtomicMethod o = new ObjectWithAtomicMethod();
        o.theMethod();

        assertEquals(1, o.intValue.get());
    }

    public class ObjectWithAtomicMethod {
        private final IntRef intValue = new IntRef(0);

        @AtomicMethod
        public void theMethod() {
            assertNotNull(getThreadLocalTransaction());
            intValue.inc();
        }
    }
}
