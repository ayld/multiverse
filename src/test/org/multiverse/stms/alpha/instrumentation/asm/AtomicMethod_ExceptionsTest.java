package org.multiverse.stms.alpha.instrumentation.asm;

import org.junit.After;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.multiverse.api.annotations.AtomicMethod;

import java.io.FileNotFoundException;

public class AtomicMethod_ExceptionsTest {

    @After
    public void tearDown() {
        //assertNoInstrumentationProblems();
    }

    @Test
    public void checkedExceptionIsPropagated() {
        Exception ex = new FileNotFoundException();

        CheckedExceptionIsPropagated r = new CheckedExceptionIsPropagated();
        r.exception = ex;

        try {
            r.doIt();
            fail();
        } catch (Exception found) {
            assertSame(ex, found);
        }

        //todo: inhoudelijke controles over aantal transacties etc.        
    }

    public static class CheckedExceptionIsPropagated {
        Exception exception;

        @AtomicMethod
        public void doIt() throws Exception {
            throw exception;
        }
    }

    @Test
    public void runtimeExceptionIsPropagated() {
        RuntimeException ex = new IllegalArgumentException();

        RuntimeExceptionIsPropagated r = new RuntimeExceptionIsPropagated();
        r.exception = ex;

        try {
            r.doIt();
            fail();
        } catch (RuntimeException found) {
            assertSame(ex, found);
        }

        //todo: inhoudelijke controles over aantal transacties etc. 
    }

    public static class RuntimeExceptionIsPropagated {
        RuntimeException exception;

        @AtomicMethod
        public void doIt() {
            throw exception;
        }
    }
}

