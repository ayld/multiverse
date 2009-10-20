package org.multiverse.stms.alpha.instrumentation.asm;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.testIncomplete;
import static org.multiverse.api.GlobalStmInstance.setGlobalStmInstance;
import org.multiverse.api.Stm;
import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.stms.alpha.AlphaStm;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

/**
 * @author Peter Veentjer
 */
public class AtomicObject_MethodAccessModifiersTest {
    private Stm stm;

    @Before
    public void setUp() {
        setThreadLocalTransaction(null);
        stm = new AlphaStm();
        setGlobalStmInstance(stm);
    }

    @After
    public void tearDown() {
        //assertNoInstrumentationProblems();
    }

    @Test
    public void testAtomicObjectCallingProtectedMethod() {
        AtomicObjectCallingProtectedMethod o = new AtomicObjectCallingProtectedMethod();

        long version = stm.getClockVersion();
        o.callDoIt();

        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(1, o.getValue());
    }

    @AtomicObject
    static class AtomicObjectCallingProtectedMethod {
        private int value;

        protected void doIt() {
            value++;
        }

        public int getValue() {
            return value;
        }

        public void callDoIt() {
            doIt();
        }
    }

    @Test
    public void testAtomicObjectCallingPublicMethod() {
        AtomicObjectCallingPublicMethod o = new AtomicObjectCallingPublicMethod();

        long version = stm.getClockVersion();
        o.callDoIt();

        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(1, o.getValue());
    }

    @AtomicObject
    static class AtomicObjectCallingPublicMethod {
        private int value;

        public void doIt() {
            value++;
        }

        public int getValue() {
            return value;
        }

        public void callDoIt() {
            doIt();
        }
    }

    @Test
    public void testAtomicObjectCallingPackageFriendlyMethod() {
        AtomicObjectCallingPackageFriendlyMethod o = new AtomicObjectCallingPackageFriendlyMethod();

        long version = stm.getClockVersion();
        o.callDoIt();

        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(1, o.getValue());
    }

    @AtomicObject
    static class AtomicObjectCallingPackageFriendlyMethod {

        private int value;

        void doIt() {
            value++;
        }

        public int getValue() {
            return value;
        }

        public void callDoIt() {
            doIt();
        }
    }

    @Test
    public void testAtomicObjectCallingPrivateMethod() {
        AtomicObjectCallingPrivateMethod privateMethod = new AtomicObjectCallingPrivateMethod();

        long version = stm.getClockVersion();
        privateMethod.callDoIt();

        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(1, privateMethod.getValue());
    }

    @AtomicObject
    static class AtomicObjectCallingPrivateMethod {

        private int value;

        private void doIt() {
            value++;
        }

        public int getValue() {
            return value;
        }

        public void callDoIt() {
            doIt();
        }
    }

    @Test
    public void testStaticPublicMethod() {
        AtomicObjectCallingPublicStaticMethod o = new AtomicObjectCallingPublicStaticMethod();

        long version = stm.getClockVersion();
        o.doIt();

        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(1, o.getValue());
    }

    @AtomicObject
    static class AtomicObjectCallingPublicStaticMethod {

        public static int inc(int number) {
            return ++number;
        }

        private int value;

        public void doIt() {
            value = inc(value);
        }

        public int getValue() {
            return value;
        }
    }

    @Test
    public void testStaticProtectedMethod() {
        AtomicObjectCallingProtectedStaticMethod o = new AtomicObjectCallingProtectedStaticMethod();

        long version = stm.getClockVersion();
        o.doIt();

        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(1, o.getValue());
    }

    @AtomicObject
    static class AtomicObjectCallingProtectedStaticMethod {

        protected static int inc(int number) {
            return ++number;
        }

        private int value;

        public void doIt() {
            value = inc(value);
        }

        public int getValue() {
            return value;
        }
    }

    @Test
    public void testStaticPackageFriendlyMethod() {
        AtomicObjectCallingPackageFriendlyStaticMethod o = new AtomicObjectCallingPackageFriendlyStaticMethod();

        long version = stm.getClockVersion();
        o.doIt();

        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(1, o.getValue());
    }

    @AtomicObject
    static class AtomicObjectCallingPackageFriendlyStaticMethod {

        static int inc(int number) {
            return ++number;
        }

        private int value;

        void doIt() {
            value = inc(value);
        }

        public int getValue() {
            return value;
        }
    }

    @Test
    public void testStaticPrivateMethod() {
        AtomicObjectCallingPrivateStaticMethod o = new AtomicObjectCallingPrivateStaticMethod();

        long version = stm.getClockVersion();
        o.doIt();

        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(1, o.getValue());
    }

    @AtomicObject
    static class AtomicObjectCallingPrivateStaticMethod {

        private static int inc(int number) {
            return ++number;
        }

        private int value;

        public void doIt() {
            value = inc(value);
        }

        public int getValue() {
            return value;
        }
    }

    @Test
    public void testExternalStaticMethod() {
        AtomicObjectCallingExternalStaticMethod o = new AtomicObjectCallingExternalStaticMethod();

        long version = stm.getClockVersion();
        o.doIt();

        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(20, o.getValue());
    }

    @AtomicObject
    static class AtomicObjectCallingExternalStaticMethod {

        private int value;

        public void doIt() {
            value = Math.max(10, 20);
        }

        public int getValue() {
            return value;
        }
    }

    @Test
    public void testConstructor(){
         testIncomplete();
    }

    @Test
    public void testSynchronizedMethod(){
         testIncomplete();
    }
}
