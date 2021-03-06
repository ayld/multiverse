package org.multiverse.stms.alpha.instrumentation.asm;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.assertIsActive;
import static org.multiverse.api.GlobalStmInstance.setGlobalStmInstance;
import static org.multiverse.api.ThreadLocalTransaction.getThreadLocalTransaction;
import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.datastructures.refs.IntRef;
import org.multiverse.stms.alpha.AlphaStm;

/**
 * An integration test for the AtomicTransformer that checks if it can deal with all the possible argument types.
 *
 * @author Peter Veentjer.
 */
public class AtomicMethod_ArgumentsTest {

    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        setGlobalStmInstance(stm);
    }


    @After
    public void tearDown() {
        //assertNoInstrumentationProblems();
    }

    public static void assertTransactionWorking() {
        assertIsActive(getThreadLocalTransaction());
    }

    // =================== tests =======================

    @Test
    public void testNoArguments() {
        new NoArguments().doIt();
    }

    public static class NoArguments {

        @AtomicMethod void doIt() {
            assertTransactionWorking();
        }
    }

    @Test
    public void objectArgument() {
        String arg = "foo";
        ObjectArgument a = new ObjectArgument();
        a.expectedArg1 = arg;
        a.doIt(arg);
    }

    @Test
    public void nullArgument() {
        String arg = null;
        ObjectArgument a = new ObjectArgument();
        a.expectedArg1 = arg;
        a.doIt(arg);
    }

    public static class ObjectArgument {

        private Object expectedArg1;

        @AtomicMethod void doIt(Object arg1) {
            assertSame(expectedArg1, arg1);
            assertTransactionWorking();
        }
    }

    @Test
    public void booleanArgument() {
        boolean arg = true;
        booleanArgument a = new booleanArgument();
        a.expectedArg1 = arg;
        a.doIt(arg);
    }

    public static class booleanArgument {

        private boolean expectedArg1;

        @AtomicMethod void doIt(boolean arg1) {
            assertEquals(expectedArg1, arg1);
            assertTransactionWorking();
        }
    }

    @Test
    public void shortArgument() {
        short arg = 114;
        ShortArgument a = new ShortArgument();
        a.expectedArg1 = arg;
        a.doIt(arg);
    }

    public static class ShortArgument {

        private short expectedArg1;

        @AtomicMethod void doIt(short arg1) {
            assertEquals(expectedArg1, arg1);
            assertTransactionWorking();
        }
    }

    @Test
    public void byteArgument() {
        byte arg = 114;
        ByteArgument a = new ByteArgument();
        a.expectedArg1 = arg;
        a.doIt(arg);
    }

    public static class ByteArgument {

        private byte expectedArg1;

        @AtomicMethod void doIt(byte arg1) {
            assertEquals(expectedArg1, arg1);
            assertTransactionWorking();
        }
    }

    @Test
    public void intArgument() {
        int arg = 114;
        IntArgument a = new IntArgument();
        a.expectedArg1 = arg;
        a.doIt(arg);
    }

    public static class IntArgument {

        private int expectedArg1;

        @AtomicMethod void doIt(int arg1) {
            assertEquals(expectedArg1, arg1);
            assertTransactionWorking();
        }
    }

    @Test
    public void multipleIntArguments() {
        int arg1 = 114;
        int arg2 = 13243414;
        int arg3 = -2345114;
        MultipleIntArguments a = new MultipleIntArguments();
        a.expectedArg1 = arg1;
        a.expectedArg2 = arg2;
        a.expectedArg3 = arg3;
        a.doIt(arg1, arg2, arg3);
    }

    public static class MultipleIntArguments {

        private int expectedArg1;
        private int expectedArg2;
        private int expectedArg3;

        @AtomicMethod void doIt(int arg1, int arg2, int arg3) {
            assertEquals(expectedArg1, arg1);
            assertEquals(expectedArg2, arg2);
            assertEquals(expectedArg3, arg3);
            assertTransactionWorking();
        }
    }

    @Test
    public void longArgument() {
        long arg = 114L;
        LongArgument a = new LongArgument();
        a.expectedArg1 = arg;
        a.doIt(arg);
    }

    public static class LongArgument {

        private long expectedArg1;

        @AtomicMethod void doIt(long arg1) {
            assertEquals(expectedArg1, arg1);
            assertTransactionWorking();
        }
    }

    @Test
    public void multipleLongArguments() {
        long arg1 = 114L;
        long arg2 = 3423434324L;
        long arg3 = -29384734L;

        MultipleLongArguments a = new MultipleLongArguments();
        a.expectedArg1 = arg1;
        a.expectedArg2 = arg2;
        a.expectedArg3 = arg3;
        a.doIt(arg1, arg2, arg3);
    }

    public static class MultipleLongArguments {

        private long expectedArg1;
        private long expectedArg2;
        private long expectedArg3;

        @AtomicMethod void doIt(long arg1, long arg2, long arg3) {
            assertEquals(expectedArg1, arg1);
            assertEquals(expectedArg2, arg2);
            assertEquals(expectedArg3, arg3);
            assertTransactionWorking();
        }
    }


    @Test
    public void floatArgument() {
        float arg = 114;
        FloatArgument a = new FloatArgument();
        a.expectedArg1 = arg;
        a.doIt(arg);
    }

    public static class FloatArgument {

        private float expectedArg1;

        @AtomicMethod void doIt(float arg1) {
            assertEquals(expectedArg1, arg1, 0.000001);
            assertTransactionWorking();
        }
    }

    @Test
    public void doubleArgument() {
        double arg = 114.0d;
        DoubleArgument a = new DoubleArgument();
        a.expectedArg1 = arg;
        a.doIt(arg);

    }

    public static class DoubleArgument {

        private double expectedArg1;

        @AtomicMethod void doIt(double arg1) {
            assertEquals(expectedArg1, arg1, 0.000001);
            assertTransactionWorking();
        }
    }

    @Test
    public void multipleDoubleArguments() {
        double arg1 = 114.0d;
        double arg2 = 334334114.0d;
        double arg3 = -3837262114.0d;
        MultipleDoubleArguments a = new MultipleDoubleArguments();
        a.expectedArg1 = arg1;
        a.expectedArg2 = arg2;
        a.expectedArg3 = arg3;
        a.doIt(arg1, arg2, arg3);
    }

    public static class MultipleDoubleArguments {

        private double expectedArg1;
        private double expectedArg2;
        private double expectedArg3;


        @AtomicMethod void doIt(double arg1, double arg2, double arg3) {
            assertEquals(expectedArg1, arg1, 0.000001);
            assertEquals(expectedArg2, arg2, 0.000001);
            assertEquals(expectedArg3, arg3, 0.000001);
            assertTransactionWorking();
        }
    }

    @Test
    public void complexSetOfArguments() {
        boolean arg1 = true;
        byte arg2 = 10;
        short arg3 = 20;
        char arg4 = 'a';
        int arg5 = 1245;
        long arg6 = 34945;
        float arg7 = 3.14f;
        double arg8 = 6.2;
        Object arg9 = "foobar";

        ComplexSetOfArguments a = new ComplexSetOfArguments();
        a.expectedArg1 = arg1;
        a.expectedArg2 = arg2;
        a.expectedArg3 = arg3;
        a.expectedArg4 = arg4;
        a.expectedArg5 = arg5;
        a.expectedArg6 = arg6;
        a.expectedArg7 = arg7;
        a.expectedArg8 = arg8;
        a.expectedArg9 = arg9;
        a.doIt(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
    }

    public class ComplexSetOfArguments {

        boolean expectedArg1;
        byte expectedArg2;
        short expectedArg3;
        char expectedArg4;
        int expectedArg5;
        long expectedArg6;
        float expectedArg7;
        double expectedArg8;
        Object expectedArg9;

        @AtomicMethod void doIt(boolean arg1, byte arg2, short arg3, char arg4, int arg5, long arg6, float arg7,
                                Object arg8, Object arg9) {
            assertEquals(expectedArg1, arg1);
            assertEquals(expectedArg2, arg2);
            assertEquals(expectedArg3, arg3);
            assertEquals(expectedArg4, arg4);
            assertEquals(expectedArg5, arg5);
            assertEquals(expectedArg6, arg6);
            assertEquals(expectedArg7, arg7, 0.000001);
            //assertEquals(expectedArg8, arg8, 0.000001);
            assertEquals(expectedArg9, arg9);
            assertTransactionWorking();
        }
    }

    // ================== arrays =====================

    @Test
    public void primitiveArray() {
        PrimitiveArrayArgument a = new PrimitiveArrayArgument();
        a.expectedArg = new int[]{1, 2, 3};
        a.doIt(a.expectedArg);
    }

    public static class PrimitiveArrayArgument {

        private int[] expectedArg;

        @AtomicMethod void doIt(int[] args) {
            assertEquals(args.length, expectedArg.length);
            for (int k = 0; k < args.length; k++) {
                assertEquals(expectedArg[k], args[k]);
            }
            assertTransactionWorking();
        }
    }

    @Test
    public void objectArray() {
        ObjectArrayArgument a = new ObjectArrayArgument();
        a.expectedArg = new Object[]{1, "foo", new Integer(20)};
        a.doIt(a.expectedArg);
    }

    public static class ObjectArrayArgument {

        private Object[] expectedArg;

        @AtomicMethod void doIt(Object[] args) {
            assertEquals(args.length, expectedArg.length);
            for (int k = 0; k < args.length; k++) {
                assertSame(expectedArg[k], args[k]);
            }
            assertTransactionWorking();
        }
    }

    @Test
    public void varArgsArgument() {
        VarArgsArgument a = new VarArgsArgument();
        a.expectedArg = new int[]{1, 2, 3};
        a.doIt(1, 2, 3);
    }

    public static class VarArgsArgument {

        private int[] expectedArg;

        @AtomicMethod void doIt(int... args) {
            assertEquals(args.length, expectedArg.length);
            for (int k = 0; k < args.length; k++) {
                assertEquals(expectedArg[k], args[k]);
            }
            assertTransactionWorking();
        }
    }

    @Test
    public void testStatic() {
        IntRef intRef = new IntRef();
        long version = stm.getTime();
        inc(intRef);

        assertEquals(version + 1, stm.getTime());
        assertEquals(1, intRef.get());
    }

    @AtomicMethod
    public static void inc(IntRef intRef) {
        intRef.inc();
    }

}
