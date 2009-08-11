package org.multiverse.stms.alpha.instrumentation.asm;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.stms.alpha.AlphaAtomicObject;
import org.multiverse.utils.GlobalStmInstance;

/**
 * @author Peter Veentjer
 */
public class AtomicObject_WithFinalsTest {
    private Stm stm;

    @Before
    public void setUp() {
        stm = GlobalStmInstance.get();
    }

    @After
    public void tearDown() {
        //assertNoInstrumentationProblems();
    }

    public static class NoFinals {
        int field;
    }

    @Test
    public void atomicObjectWithNoFieldsIsNotManaged() {
        long version = stm.getClockVersion();

        NoFields noFields = new NoFields();

        assertEquals(version, stm.getClockVersion());
        assertFalse(noFields instanceof AlphaAtomicObject);
    }

    @AtomicObject
    public static class NoFields {

        public NoFields() {
        }
    }

    @Test
    public void atomicObjectWithOneFinalFieldIsNotManaged() {
        long version = stm.getClockVersion();

        OneFinalField o = new OneFinalField(20);

        assertEquals(version, stm.getClockVersion());
        assertFalse(o instanceof AlphaAtomicObject);
        assertEquals(20, o.getValue());
    }

    @AtomicObject
    public static class OneFinalField {

        final int value;

        public OneFinalField(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    @Test
    public void atomicObjectWithSomeFinalFieldsIsManaged() {
        long version = stm.getClockVersion();

        SomeFinalFields o = new SomeFinalFields(10, 20);

        assertTrue(o instanceof AlphaAtomicObject);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(10, o.getFinalValue());
        assertEquals(10, o.finalValue);
        assertEquals(20, o.getNonFinalValue());

    }

    @AtomicObject
    public static class SomeFinalFields {
        private final int finalValue;
        private int nonFinalValue;

        public SomeFinalFields(int finalValue, int nonFinalValue) {
            this.finalValue = finalValue;
            this.nonFinalValue = nonFinalValue;
        }

        public int getFinalValue() {
            return finalValue;
        }

        public int getNonFinalValue() {
            return nonFinalValue;
        }
    }

    @Test
    public void atomicObjectWithAllFinalFieldsIsNotManaged() {
        long version = stm.getClockVersion();

        AllFinalFields o = new AllFinalFields(10, 20, 30);

        assertEquals(version, stm.getClockVersion());
        assertEquals(10, o.getValue1());
        assertEquals(20, o.getValue2());
        assertEquals(30, o.getValue3());
        assertFalse(o instanceof AlphaAtomicObject);
    }

    @AtomicObject
    public static class AllFinalFields {
        final int value1;
        final int value2;
        final int value3;

        public AllFinalFields(int value1, int value2, int value3) {
            this.value1 = value1;
            this.value2 = value2;
            this.value3 = value3;
        }

        public int getValue1() {
            return value1;
        }

        public int getValue2() {
            return value2;
        }

        public int getValue3() {
            return value3;
        }
    }

    @Test
    public void testChainedReferences() {
        ChainedRef ref1 = new ChainedRef(null, 1);
        ChainedRef ref2 = new ChainedRef(ref1, 2);
        ChainedRef ref3 = new ChainedRef(ref2, 3);

        assertSame(ref2, ref3.getNext());
        assertSame(ref1, ref3.getIndirectNext());
        assertNull(ref3.getLongIndirectNext());
        assertSame(ref1, ref2.getNext());
        assertNull(ref2.getIndirectNext());
    }

    @AtomicObject
    public static class ChainedRef {
        int someValue;
        final ChainedRef next;

        public ChainedRef(ChainedRef next, int someValue) {
            this.next = next;
            this.someValue = someValue;
        }

        public ChainedRef getNext() {
            return next;
        }

        public int getSomeValue() {
            return someValue;
        }

        public ChainedRef getIndirectNext() {
            return next.next;
        }

        public ChainedRef getLongIndirectNext() {
            return next.next.next;
        }
    }

    //todo: test with multiple refs.
    //a.b.c.d of the same type and finals.
    //a.b.c of different types and final
}
