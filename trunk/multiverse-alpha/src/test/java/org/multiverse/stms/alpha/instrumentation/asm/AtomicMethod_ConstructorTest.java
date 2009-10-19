package org.multiverse.stms.alpha.instrumentation.asm;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.GlobalStmInstance;
import org.multiverse.api.Stm;
import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.stms.alpha.AlphaStm;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

public class AtomicMethod_ConstructorTest {
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

    @Test
    public void noArgAtomicConstructor() {
        long version = stm.getClockVersion();

        NoArgAtomicConstructor o = new NoArgAtomicConstructor();
        assertEquals(version, stm.getClockVersion());
        assertEquals(20, o.value);
    }

    static class NoArgAtomicConstructor {
        int value;

        @AtomicMethod
        NoArgAtomicConstructor() {
            this.value = 20;
        }
    }

    @Test
    public void singleArgAtomicConstructor() {
        long version = stm.getClockVersion();

        SingleArgAtomicConstructor o = new SingleArgAtomicConstructor(20);

        assertEquals(version, stm.getClockVersion());
        assertEquals(20, o.arg);
    }

    static class SingleArgAtomicConstructor {
        int arg;

        @AtomicMethod
        SingleArgAtomicConstructor(int value) {
            this.arg = value;
        }
    }

    @Test
    public void multiArgAtomicConstructor() {
        long version = stm.getClockVersion();

        MultiArgAtomicConstructor o = new MultiArgAtomicConstructor(6, 7, 8, 9);

        assertEquals(version, stm.getClockVersion());
        assertEquals(6, o.arg1);
        assertEquals(7, o.arg2);
        assertEquals(8, o.arg3);
        assertEquals(9, o.arg4);
    }

    static class MultiArgAtomicConstructor {
        int arg1, arg2, arg3, arg4;

        @AtomicMethod
        MultiArgAtomicConstructor(int arg1, int arg2, int arg3, int arg4) {
            this.arg1 = arg1;
            this.arg2 = arg2;
            this.arg3 = arg3;
            this.arg4 = arg4;
        }
    }

    @Test
    public void constructorWithCheckedException() {
        long version = stm.getClockVersion();
        try {
            new ConstructorWithCheckedException(true);
        } catch (Exception e) {
        }

        assertEquals(version, stm.getClockVersion());
    }

    @Test
    public void constructorWithCheckedExceptionThatIsNotThrown() throws Exception {
        long version = stm.getClockVersion();
        ConstructorWithCheckedException o = new ConstructorWithCheckedException(false);

        assertEquals(version, stm.getClockVersion());
    }

    static class ConstructorWithCheckedException {
        @AtomicMethod
        ConstructorWithCheckedException(boolean throwIt) throws Exception {
            if (throwIt) {
                throw new Exception();
            }
        }
    }

    @Test
    public void testPublicConstructor() {
        long version = stm.getClockVersion();

        PublicConstructor o = new PublicConstructor();

        assertEquals(10, o.value);
        assertEquals(version, stm.getClockVersion());
    }

    static class PublicConstructor {
        private int value;

        @AtomicMethod
        public PublicConstructor() {
            value = 10;
        }
    }

    @Test
    public void testFinalField() {
        long version = stm.getClockVersion();

        FinalField o = new FinalField(1);

        assertEquals(version, stm.getClockVersion());
        assertEquals(1, o.value);
    }

    static class FinalField {
        private int value;

        @AtomicMethod
        FinalField(int value) {
            this.value = value;
        }
    }

    @Test
    public void testProtectedConstructor() {
        long version = stm.getClockVersion();

        ProtectedConstructor o = new ProtectedConstructor();

        assertEquals(version, stm.getClockVersion());
        assertEquals(10, o.value);
    }

    static class ProtectedConstructor {
        private int value;

        @AtomicMethod
        protected ProtectedConstructor() {
            value = 10;
        }
    }

    @Test
    public void testPackageFriendlyConstructor() {
        long version = stm.getClockVersion();

        PackageFriendlyConstructor o = new PackageFriendlyConstructor();

        assertEquals(version, stm.getClockVersion());
        assertEquals(10, o.value);

    }

    static class PackageFriendlyConstructor {
        private int value;

        @AtomicMethod
        PackageFriendlyConstructor() {
            value = 10;
        }
    }

    @Test
    public void testPrivateFriendlyConstructor() {
        long version = stm.getClockVersion();

        PrivateConstructor o = new PrivateConstructor();

        assertEquals(version, stm.getClockVersion());
        assertEquals(10, o.value);
    }

    static class PrivateConstructor {
        private int value;

        @AtomicMethod
        private PrivateConstructor() {
            value = 10;
        }
    }
}
