package org.multiverse.instrumentation;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.SharedStmInstance;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import static org.multiverse.api.TransactionThreadLocal.getTransaction;
import org.multiverse.api.annotations.Atomic;
import org.multiverse.api.annotations.TmEntity;

/**
 * todo: array types moeten er nog bij
 */
public class AtomicTransformer_ReturnTypesTest {

    private static int value = 123456;
    private static Handle<IntValue> testHandle;

    @Before
    public void setUp() {
        testHandle = commit(SharedStmInstance.getInstance(), new IntValue(value));
    }

    @TmEntity
    static class IntValue {
        private int value;

        IntValue(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static void assertTransactionWorking() {
        IntValue intValue = getTransaction().read(testHandle);
        assertEquals(value, intValue.getValue());
    }

    @Test
    public void voidReturningMethod() {
        VoidReturningMethod m = new VoidReturningMethod();
        m.doIt();
    }

    public class VoidReturningMethod {

        @Atomic
        public void doIt() {
            assertTransactionWorking();
        }
    }


    @Test
    public void objectReturningMethod() {
        String s = new ObjectReturningMethod().doIt();
        assertEquals("foo", s);
    }

    public class ObjectReturningMethod {

        @Atomic
        public String doIt() {
            assertTransactionWorking();
            return "foo";
        }
    }

    // =================== primitives =========================

    @Test
    public void booleanReturningMethod() {
        BooleanReturningMethod m = new BooleanReturningMethod();
        boolean result = m.doIt();
        assertTrue(result);
    }

    public static class BooleanReturningMethod {
        @Atomic
        public boolean doIt() {
            assertTransactionWorking();
            return true;
        }
    }

    @Test
    public void byteReturningMethod() {
        ByteReturningMethod m = new ByteReturningMethod();
        byte result = m.doIt();
        assertEquals(11, result);
    }

    public static class ByteReturningMethod {
        @Atomic
        public byte doIt() {
            assertTransactionWorking();
            return 11;
        }
    }

    @Test
    public void shortReturningMethod() {
        ShortReturningMethod m = new ShortReturningMethod();
        short result = m.doIt();
        assertEquals(11, result);
    }

    public static class ShortReturningMethod {
        @Atomic
        public short doIt() {
            assertTransactionWorking();
            return 11;
        }
    }

    @Test
    public void charReturningMethod() {
        CharReturningMethod m = new CharReturningMethod();
        char result = m.doIt();
        assertEquals('p', result);
    }

    public static class CharReturningMethod {
        @Atomic
        public char doIt() {
            assertTransactionWorking();
            return 'p';
        }
    }

    @Test
    public void intReturningMethod() {
        IntReturningMethod m = new IntReturningMethod();
        int result = m.doIt();
        assertEquals(111, result);
    }

    public static class IntReturningMethod {
        @Atomic
        public int doIt() {
            assertTransactionWorking();
            return 111;
        }
    }

    @Test
    public void floatReturningMehod() {
        FloatReturningMethod m = new FloatReturningMethod();
        float result = m.doIt();
        assertEquals(11.4f, result, 0.0001);
    }

    public static class FloatReturningMethod {
        @Atomic
        public float doIt() {
            assertTransactionWorking();
            return 11.4f;
        }
    }

    @Test
    public void longReturningMethod() {
        LongReturningMethod m = new LongReturningMethod();
        long result = m.doIt();
        assertEquals(111, result);
    }

    public static class LongReturningMethod {
        @Atomic
        public long doIt() {
            assertTransactionWorking();
            return 111L;
        }
    }

    @Test
    public void doubleReturningMethod() {
        DoubleReturningMethod m = new DoubleReturningMethod();
        double result = m.doIt();
        assertEquals(11.4, result, 0.0001);
    }

    public static class DoubleReturningMethod {
        @Atomic
        public double doIt() {
            assertTransactionWorking();
            return 11.4;
        }
    }

    // ================== arrays ======================

    @Test
    public void primitiveArrayReturningMethod() {
        PrimitiveArrayReturningMethod m = new PrimitiveArrayReturningMethod();
        boolean[] result = m.doIt();
        assertEquals(2, result.length);
        assertTrue(result[0]);
        assertFalse(result[1]);
    }

    public static class PrimitiveArrayReturningMethod {
        @Atomic
        public boolean[] doIt() {
            assertTransactionWorking();
            return new boolean[]{true, false};
        }
    }

    @Test
    public void objectArrayReturningMethod() {
        String[] array = new String[]{"foo", "bar"};
        ObjectArrayReturningMethod m = new ObjectArrayReturningMethod();
        m.result = array;
        Object[] result = m.doIt();
        assertSame(array, result);
    }

    public static class ObjectArrayReturningMethod {
        Object[] result;

        @Atomic
        public Object[] doIt() {
            assertTransactionWorking();
            return result;
        }
    }
}
