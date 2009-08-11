package org.multiverse.stms.alpha.instrumentation.asm;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.multiverse.api.annotations.AtomicObject;

/**
 * @author Peter Veentjer
 */
public class TransactionalObjectWithSelfCallTest {

    @After
    public void tearDown() {
        //assertNoInstrumentationProblems();
    }


    @Test
    public void test() {
        Value value = new Value(20);
        value.inc();
        assertEquals(21, value.get());
    }


    @AtomicObject
    static class Value {

        private int value;

        Value(int value) {
            this.value = value;
        }

        public int get() {
            return value;
        }

        public void inc() {
            set(get() + 1);
        }

        public void set(int value) {
            this.value = value;
        }
    }
}
