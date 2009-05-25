package org.multiverse.instrumentation;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.multiverse.api.annotations.Atomic;

import java.io.FileNotFoundException;

public class AtomicTransformer_ExceptionsTest {

    //@Test
    public void checkedExceptionIsPropagated() {
        Exception ex = new FileNotFoundException();

        CheckedExceptionIsPropagated r = new CheckedExceptionIsPropagated();
        r.exception = ex;

        try {
            r.doIt();
            fail();
        } catch (Exception found) {
            found.printStackTrace();
            assertSame(ex, found);
        }

        //todo: inhoudelijke controles over aantal transacties etc.        
    }

    public static class CheckedExceptionIsPropagated {
        Exception exception;

        @Atomic
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
            found.printStackTrace();
            assertSame(ex, found);
        }

        //todo: inhoudelijke controles over aantal transacties etc.
    }

    public static class RuntimeExceptionIsPropagated {
        RuntimeException exception;

        @Atomic
        public void doIt() {
            throw exception;
        }
    }
}

