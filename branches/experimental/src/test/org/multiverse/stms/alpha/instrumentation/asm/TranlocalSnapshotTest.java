package org.multiverse.stms.alpha.instrumentation.asm;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Transaction;
import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.datastructures.refs.IntRef;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.templates.AtomicTemplate;
import org.multiverse.templates.OrElseTemplate;
import org.multiverse.utils.GlobalStmInstance;

/**
 * Integration test for TranlocalSnapshot functionality. This behavior
 * is needed for e.g. the orelse mechanism so that state on objects can be restored
 * after a rollback within a transaction is done.
 *
 * @author Peter Veentjer
 */
public class TranlocalSnapshotTest {

    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
    }


    @After
    public void tearDown() {
        //assertNoInstrumentationProblems();
    }

    @Test
    public void intObjectTest() {
        final int oldValue = 10;
        final int newValue = 100;
        final intObject i = new intObject(oldValue);

        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                new OrElseTemplate() {
                    @Override
                    public Object orelserun(Transaction t) {
                        i.setValue(newValue);
                        return null;
                    }

                    @Override
                    public Object run(Transaction t) {
                        assertEquals(oldValue, i.getValue());
                        return null;
                    }
                }.execute();
                return null;
            }
        }.execute();
    }

    @AtomicObject
    private static class intObject {
        int value;

        intObject(int value) {
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
    public void booleanObjectTest() {

        final boolean oldValue = false;
        final boolean newValue = true;
        final booleanObject i = new booleanObject(oldValue);

        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                new OrElseTemplate() {
                    @Override
                    public Object orelserun(Transaction t) {
                        i.setValue(newValue);
                        return null;
                    }

                    @Override
                    public Object run(Transaction t) {
                        assertEquals(oldValue, i.getValue());
                        return null;
                    }
                }.execute();
                return null;
            }
        }.execute();
    }

    @AtomicObject
    private static class booleanObject {
        boolean value;

        booleanObject(boolean value) {
            this.value = value;
        }

        public boolean getValue() {
            return value;
        }

        public void setValue(boolean value) {
            this.value = value;
        }
    }

    @Test
    public void shortObjectTest() {
        final short oldValue = 10;
        final short newValue = 100;
        final shortObject i = new shortObject(oldValue);

        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                new OrElseTemplate() {
                    @Override
                    public Object orelserun(Transaction t) {
                        i.setValue(newValue);
                        return null;
                    }

                    @Override
                    public Object run(Transaction t) {
                        assertEquals(oldValue, i.getValue());
                        return null;
                    }
                }.execute();
                return null;
            }
        }.execute();
    }

    @AtomicObject
    private static class shortObject {
        short value;

        shortObject(short value) {
            this.value = value;
        }

        public short getValue() {
            return value;
        }

        public void setValue(short value) {
            this.value = value;
        }
    }

    @Test
    public void charObjectTest() {
        final char oldValue = 10;
        final char newValue = 100;
        final charObject i = new charObject(oldValue);

        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                new OrElseTemplate() {
                    @Override
                    public Object orelserun(Transaction t) {
                        i.setValue(newValue);
                        return null;
                    }

                    @Override
                    public Object run(Transaction t) {
                        assertEquals(oldValue, i.getValue());
                        return null;
                    }
                }.execute();
                return null;
            }
        }.execute();
    }

    @AtomicObject
    private static class charObject {
        char value;

        charObject(char value) {
            this.value = value;
        }

        public char getValue() {
            return value;
        }

        public void setValue(char value) {
            this.value = value;
        }
    }

    @Test
    public void longObjectTest() {
        final long oldValue = 10;
        final long newValue = 100;
        final longObject i = new longObject(oldValue);

        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                new OrElseTemplate() {
                    @Override
                    public Object orelserun(Transaction t) {
                        i.setValue(newValue);
                        return null;
                    }

                    @Override
                    public Object run(Transaction t) {
                        assertEquals(oldValue, i.getValue());
                        return null;
                    }
                }.execute();
                return null;
            }
        }.execute();
    }

    @AtomicObject
    private static class longObject {
        long value;

        longObject(long value) {
            this.value = value;
        }

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }
    }

    @Test
    public void doubleObjectTest() {
        final double oldValue = 10;
        final double newValue = 100;
        final doubleObject i = new doubleObject(oldValue);

        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                new OrElseTemplate() {
                    @Override
                    public Object orelserun(Transaction t) {
                        i.setValue(newValue);
                        return null;
                    }

                    @Override
                    public Object run(Transaction t) {
                        assertEquals(oldValue, i.getValue(), 0.0001);
                        return null;
                    }
                }.execute();
                return null;
            }
        }.execute();
    }

    @AtomicObject
    private static class doubleObject {
        double value;

        doubleObject(double value) {
            this.value = value;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }
    }


    @Test
    public void floatObjectTest() {
        final float oldValue = 10;
        final float newValue = 100;
        final floatObject i = new floatObject(oldValue);

        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                new OrElseTemplate() {
                    @Override
                    public Object orelserun(Transaction t) {
                        i.setValue(newValue);
                        return null;
                    }

                    @Override
                    public Object run(Transaction t) {
                        assertEquals(oldValue, i.getValue(), 0.000001);
                        return null;
                    }
                }.execute();
                return null;
            }
        }.execute();
    }

    @AtomicObject
    private static class floatObject {
        float value;

        floatObject(float value) {
            this.value = value;
        }

        public float getValue() {
            return value;
        }

        public void setValue(float value) {
            this.value = value;
        }
    }

    @Test
    public void arrayObjectTest() {
        final int[] oldValue = new int[]{10};
        final int[] newValue = new int[]{100};
        final arrayObject i = new arrayObject(oldValue);

        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                new OrElseTemplate() {
                    @Override
                    public Object orelserun(Transaction t) {
                        i.setValue(newValue);
                        return null;
                    }

                    @Override
                    public Object run(Transaction t) {
                        assertSame(oldValue, i.getValue());
                        return null;
                    }
                }.execute();
                return null;
            }
        }.execute();
    }

    @AtomicObject
    private static class arrayObject {
        int[] value;

        arrayObject(int[] value) {
            this.value = value;
        }

        public int[] getValue() {
            return value;
        }

        public void setValue(int[] value) {
            this.value = value;
        }
    }

    @Test
    public void nonAtomicObjectField() {
        final String oldValue = "foo";
        final String newValue = "bar";
        final StringObject i = new StringObject(oldValue);

        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                new OrElseTemplate() {
                    @Override
                    public Object orelserun(Transaction t) {
                        i.setValue(newValue);
                        return null;
                    }

                    @Override
                    public Object run(Transaction t) {
                        assertSame(oldValue, i.getValue());
                        return null;
                    }
                }.execute();
                return null;
            }
        }.execute();
    }

    @AtomicObject
    private static class StringObject {
        String value;

        StringObject(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @Test
    public void AtomicObjectField() {
        /*final IntValue oldValue = "foo";
        final String newValue = "bar";
        final AtomicObjectFieldObject i = new AtomicObjectFieldObject(oldValue);

        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                new OrElseTemplate() {
                    @Override
                    public Object orelserun(Transaction t) {
                        i.setValue(newValue);
                        return null;
                    }

                    @Override
                    public Object run(Transaction t) {
                        assertSame(oldValue, i.getValue());
                        return null;
                    }
                }.execute();
                return null;
            }
        }.execute();*/
    }

    @AtomicObject
    private static class AtomicObjectFieldObject {
        IntRef value;

        AtomicObjectFieldObject(IntRef value) {
            this.value = value;
        }

        public IntRef getValue() {
            return value;
        }

        public void setValue(IntRef value) {
            this.value = value;
        }
    }

    @Test
    public void RuntimeKnownAtomicObjectField() {
        //todo
    }

    @Test
    public void testMultipleFieldsObjectTest() {
        final MultipleFieldsObject m = new MultipleFieldsObject(1, 2, 3);

        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                new OrElseTemplate() {
                    @Override
                    public Object orelserun(Transaction t) {
                        m.setValue1(100);
                        m.setValue2(200);
                        m.setValue3(300);
                        return null;
                    }

                    @Override
                    public Object run(Transaction t) {
                        assertEquals(1, m.getValue1());
                        assertEquals(2, m.getValue2());
                        assertEquals(3, m.getValue3());
                        return null;
                    }
                }.execute();
                return null;
            }
        }.execute();
    }

    @AtomicObject
    private static class MultipleFieldsObject {
        int value1;
        int value2;
        int value3;

        MultipleFieldsObject(int value1, int value2, int value3) {
            this.value1 = value1;
            this.value2 = value2;
            this.value3 = value3;
        }

        public int getValue1() {
            return value1;
        }

        public void setValue1(int value1) {
            this.value1 = value1;
        }

        public int getValue2() {
            return value2;
        }

        public void setValue2(int value2) {
            this.value2 = value2;
        }

        public int getValue3() {
            return value3;
        }

        public void setValue3(int value3) {
            this.value3 = value3;
        }
    }

    // ===================== access modifiers =========================

    @Test
    public void privateFieldTest() {
        final int oldValue = 10;
        final int newValue = 20;
        final PrivateField i = new PrivateField(oldValue);

        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                new OrElseTemplate() {
                    @Override
                    public Object orelserun(Transaction t) {
                        i.setValue(newValue);
                        return null;
                    }

                    @Override
                    public Object run(Transaction t) {
                        assertSame(oldValue, i.getValue());
                        return null;
                    }
                }.execute();
                return null;
            }
        }.execute();
    }

    @AtomicObject
    static class PrivateField {
        private int value;

        PrivateField(int value) {
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
    public void publicFieldTest() {
        final int oldValue = 10;
        final int newValue = 20;
        final PublicField i = new PublicField(oldValue);

        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                new OrElseTemplate() {
                    @Override
                    public Object orelserun(Transaction t) {
                        i.setValue(newValue);
                        return null;
                    }

                    @Override
                    public Object run(Transaction t) {
                        assertSame(oldValue, i.getValue());
                        return null;
                    }
                }.execute();
                return null;
            }
        }.execute();
    }

    class PublicField {
        public int value;

        PublicField(int value) {
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
    public void protectedFieldTest() {
        final int oldValue = 10;
        final int newValue = 20;
        final ProtectedField i = new ProtectedField(oldValue);

        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                new OrElseTemplate() {
                    @Override
                    public Object orelserun(Transaction t) {
                        i.setValue(newValue);
                        return null;
                    }

                    @Override
                    public Object run(Transaction t) {
                        assertSame(oldValue, i.getValue());
                        return null;
                    }
                }.execute();
                return null;
            }
        }.execute();
    }

    class ProtectedField {
        protected int value;

        ProtectedField(int value) {
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
    public void packageFriendlyTest() {
        final int oldValue = 10;
        final int newValue = 20;
        final PackageFriendlyField i = new PackageFriendlyField(oldValue);

        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                new OrElseTemplate() {
                    @Override
                    public Object orelserun(Transaction t) {
                        i.setValue(newValue);
                        return null;
                    }

                    @Override
                    public Object run(Transaction t) {
                        assertSame(oldValue, i.getValue());
                        return null;
                    }
                }.execute();
                return null;
            }
        }.execute();
    }

    class PackageFriendlyField {
        int value;

        PackageFriendlyField(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    // =================== other corner cases =========================

    @Test
    public void genericFieldTest() {
        final Integer oldValue = 10;
        final Integer newValue = 20;
        final GenericField<Integer> i = new GenericField<Integer>(oldValue);

        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                new OrElseTemplate() {
                    @Override
                    public Object orelserun(Transaction t) {
                        i.setValue(newValue);
                        return null;
                    }

                    @Override
                    public Object run(Transaction t) {
                        assertSame(oldValue, i.getValue());
                        return null;
                    }
                }.execute();
                return null;
            }
        }.execute();
    }

    class GenericField<E> {
        E value;

        GenericField(E value) {
            this.value = value;
        }

        public E getValue() {
            return value;
        }

        public void setValue(E value) {
            this.value = value;
        }
    }

    //final fields test

    //excluded fields test

}
