package org.multiverse.instrumentation;

import static junit.framework.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import static org.multiverse.api.TransactionThreadLocal.getTransaction;
import org.multiverse.api.annotations.Atomic;

public class AtomicTransformer_MiscTest {

    private static int originalValue;
    private static Handle<IntValue> handle;

    @Before
    public void setUp() {
        originalValue = 10000;
        handle = commit(new IntValue(originalValue));
    }

    public static void assertTransactionWorking() {
        IntValue value = getTransaction().read(handle);
        assertEquals(originalValue, value.getValue());
    }

    @Test
    public void clashingAtomicMethodNames() {
        ClashingAtomicMethodNames clashingMethodNames = new ClashingAtomicMethodNames();
        clashingMethodNames.doIt(1);
        clashingMethodNames.doIt(true);
    }

    public class ClashingAtomicMethodNames {

        @Atomic
        public void doIt(boolean b) {
            assertTransactionWorking();
        }

        @Atomic
        public void doIt(int i) {
            assertTransactionWorking();
        }
    }
}
