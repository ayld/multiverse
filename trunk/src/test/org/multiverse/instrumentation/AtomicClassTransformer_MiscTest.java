package org.multiverse.instrumentation;

import static junit.framework.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import static org.multiverse.api.TransactionThreadLocal.getTransaction;
import org.multiverse.api.annotations.Atomic;

public class AtomicClassTransformer_MiscTest {

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

    public void abstractMethodIsIgnored() {
        //    AbstractMethod abstractMethod = new AbstractMethod();
    }

    public static abstract class AbstractMethod {

        @Atomic
        abstract void doIt();
    }

    //@Test
    public void nativeMethodIsIgnored() {
        NativeMethod nativeMethod = new NativeMethod();
        nativeMethod.doIt();
    }

    public static class NativeMethod {
        native void doIt();
    }

    /**
     * Tests if the system is able to deal with method that have the same name, but different
     * signatures.
     */
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
