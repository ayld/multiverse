package org.multiverse.instrumentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import static org.multiverse.api.TransactionThreadLocal.getTransaction;
import org.multiverse.api.annotations.Atomic;

public class AtomicClassTransformer_StaticMethodTest {
    private static Handle<IntValue> handle;
    private static int originalValue;

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
    public void testSimpleStaticMethod() {
        StaticNoArgMethod.doIt();
    }

    public static class StaticNoArgMethod {

        @Atomic
        static void doIt() {
            assertTransactionWorking();
        }
    }

    @Test
    public void testComplexStaticMethod() {
        StaticComplexMethod.aExpected = 10;
        StaticComplexMethod.bExpected = 1000L;
        StaticComplexMethod.cExpected = "";
        StaticComplexMethod.result = 400;

        int result = StaticComplexMethod.doIt(
                StaticComplexMethod.aExpected,
                StaticComplexMethod.bExpected,
                StaticComplexMethod.cExpected);
        assertEquals(StaticComplexMethod.result, result);
    }

    public static class StaticComplexMethod {
        static int aExpected;
        static long bExpected;
        static String cExpected;
        static int result;

        @Atomic
        static int doIt(int a, long b, String c) {
            assertTransactionWorking();
            assertEquals(aExpected, a);
            assertEquals(bExpected, b);
            assertSame(cExpected, c);
            return result;
        }
    }
}
