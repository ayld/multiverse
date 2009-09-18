package org.multiverse.stms.alpha.instrumentation.asm;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.utils.GlobalStmInstance;

/**
 * @author Peter Veentjer
 */
public class AtomicObject_NonStaticInnerClassTest {

    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
    }


    @Test
    public void nonStaticAtomicObject() {
        long version = stm.getClockVersion();

        NonStaticAtomicObject innerClass = new NonStaticAtomicObject(10);

        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(10, innerClass.getValue());
    }

    @AtomicObject
    class NonStaticAtomicObject {
        int value;

        NonStaticAtomicObject(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    @Test
    public void outerWithInner() {
        long version = stm.getClockVersion();

        Outer outer = new Outer();

        assertEquals(version + 1, stm.getClockVersion());
        assertNull(outer.getInner());

        version = stm.getClockVersion();

        outer.newInner(10);

        assertEquals(version + 1, stm.getClockVersion());
        assertNotNull(outer.getInner());
        assertEquals(10, outer.getInner().getValue());
    }

    @AtomicObject
    static class Outer {
        private Inner inner;

        @AtomicMethod(readonly = true)
        public Inner getInner() {
            return inner;
        }

        public void newInner(int value) {
            inner = new Inner(value);
        }

        @AtomicObject
        class Inner {
            private int value;

            Inner(int value) {
                this.value = value;
            }

            @AtomicMethod(readonly = true)
            public int getValue() {
                return value;
            }
        }
    }
}
