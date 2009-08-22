package org.multiverse.stms.alpha.instrumentation.asm;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

/**
 * @author Peter Veentjer
 */
public class AtomicObject_FieldTypesTest {
    private Stm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
        setThreadLocalTransaction(null);
    }

    @After
    public void tearDown() {
        //assertNoInstrumentationProblems();
    }

    //volatile , transient

    @Test
    public void booleanTest() {
        booleanValue value = new booleanValue(true);
        assertTrue(value.get());

        value.set(false);
        assertFalse(value.get());
    }

    @AtomicObject
    public static class booleanValue {
        private boolean value;

        public booleanValue(boolean value) {
            this.value = value;
        }

        public boolean get() {
            return value;
        }

        public void set(boolean value) {
            this.value = value;
        }
    }

    @Test
    public void shortTest() {
        shortValue value = new shortValue((short) 10);
        assertEquals((short) 10, value.get());

        value.set((short) 20);
        assertEquals((short) 20, value.get());
    }


    @AtomicObject
    public static class shortValue {
        private short value;

        public shortValue(short value) {
            this.value = value;
        }

        public short get() {
            return value;
        }

        public void set(short value) {
            this.value = value;
        }
    }

    @Test
    public void byeTest() {
        byteValue value = new byteValue((byte) 10);
        assertEquals((byte) 10, value.get());

        value.set((byte) 20);
        assertEquals((byte) 20, value.get());
    }

    @AtomicObject
    public static class byteValue {
        private byte value;

        public byteValue(byte value) {
            this.value = value;
        }

        public byte get() {
            return value;
        }

        public void set(byte value) {
            this.value = value;
        }
    }

    @Test
    public void charTest() {
        charValue value = new charValue('a');
        assertEquals('a', value.get());

        value.set('b');
        assertEquals('b', value.get());
    }


    @AtomicObject
    public static class charValue {
        private char value;

        public charValue(char value) {
            this.value = value;
        }

        public char get() {
            return value;
        }

        public void set(char value) {
            this.value = value;
        }
    }

    @Test
    public void intTest() {
        intValue value = new intValue(10);
        assertEquals(10, value.get());

        value.set(20);
        assertEquals(20, value.get());
    }


    @AtomicObject
    public static class intValue {
        private int value;

        public intValue(int value) {
            this.value = value;
        }

        public int get() {
            return value;
        }

        public void set(int value) {
            this.value = value;
        }
    }

    @Test
    public void floatTest() {
        floatValue value = new floatValue(10);
        assertEquals(10, value.get(), 0.000001);

        value.set(20);
        assertEquals(20, value.get(), 0.000001);
    }

    @AtomicObject
    public static class floatValue {
        private float value;

        public floatValue(float value) {
            this.value = value;
        }

        public float get() {
            return value;
        }

        public void set(float value) {
            this.value = value;
        }
    }

    @Test
    public void longTest() {
        longValue value = new longValue(10);
        assertEquals(10, value.get());

        value.set(20);
        assertEquals(20, value.get());
    }

    @AtomicObject
    public static class longValue {
        private long value;

        public longValue(long value) {
            this.value = value;
        }

        public long get() {
            return value;
        }

        public void set(long value) {
            this.value = value;
        }
    }

    @Test
    public void doubleTest() {
        doubleValue value = new doubleValue(10);
        assertEquals(10, value.get(), 0.000001);

        value.set(20);
        assertEquals(20, value.get(), 0.000001);
    }

    @AtomicObject
    public static class doubleValue {
        private double value;

        public doubleValue(double value) {
            this.value = value;
        }

        public double get() {
            return value;
        }

        public void set(double value) {
            this.value = value;
        }
    }

    @Test
    public void nonTransactionalObjectRefTest() {
        String value1 = "foo";
        NonTransactionalObjectRef ref = new NonTransactionalObjectRef(value1);
        assertEquals(value1, ref.get());

        String value2 = "bar";
        ref.set(value2);
        assertEquals(value2, ref.get());
    }

    @Test
    public void nonTransactionalObjectRefWithNullTest() {
        NonTransactionalObjectRef ref = new NonTransactionalObjectRef(null);
        assertEquals(null, ref.get());

        String value = "foo";
        ref.set(value);
        assertEquals(value, ref.get());
    }

    @AtomicObject
    public static class NonTransactionalObjectRef {
        private String ref;

        public NonTransactionalObjectRef(String value) {
            this.ref = value;
        }

        public String get() {
            return ref;
        }

        public void set(String value) {
            this.ref = value;
        }
    }

    @Test
    public void transactionalObjectRefTest() {
        intValue value1 = new intValue(10);
        TransactionalObjectRef ref = new TransactionalObjectRef(value1);
        assertSame(value1, ref.get());

        intValue value2 = new intValue(20);
        ref.set(value2);
        assertEquals(value2, ref.get());
    }

    @Test
    public void transactionalObjectRefWithNullTest() {
        TransactionalObjectRef ref = new TransactionalObjectRef(null);
        assertEquals(null, ref.get());

        intValue value = new intValue(10);
        ref.set(value);
        assertEquals(value, ref.get());
    }

    @AtomicObject
    public static class TransactionalObjectRef {
        private intValue ref;

        public TransactionalObjectRef(intValue ref) {
            this.ref = ref;
        }

        public intValue get() {
            return ref;
        }

        public void set(intValue value) {
            this.ref = value;
        }
    }

    //todo: runtime known types.
}
