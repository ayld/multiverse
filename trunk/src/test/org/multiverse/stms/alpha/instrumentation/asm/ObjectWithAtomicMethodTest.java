package org.multiverse.stms.alpha.instrumentation.asm;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.annotations.AtomicMethod;
import static org.multiverse.utils.TransactionThreadLocal.getThreadLocalTransaction;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;
import org.multiverse.datastructures.refs.IntRef;

/**
 * @author Peter Veentjer
 */
public class ObjectWithAtomicMethodTest {

    @Before
    public void setUp() {
        setThreadLocalTransaction(null);
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
