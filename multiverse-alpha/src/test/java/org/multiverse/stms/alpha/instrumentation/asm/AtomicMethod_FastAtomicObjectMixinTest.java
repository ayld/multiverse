package org.multiverse.stms.alpha.instrumentation.asm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.testIncomplete;
import static org.multiverse.api.GlobalStmInstance.setGlobalStmInstance;
import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.stms.alpha.AlphaAtomicObject;
import org.multiverse.stms.alpha.AlphaStm;
import static org.multiverse.stms.alpha.instrumentation.AlphaReflectionUtils.existsField;
import org.multiverse.utils.instrumentation.ClassUtils;

/**
 * A unit tests that checks if all fields/methods/interfaces of the FastAtomicObject are copied
 * when the code is instrumented (each atomicobject will receive the complete content of the
 * fastatomicobject).
 *
 * This tests depends on the fact that the FastAtomicObjectMixin is used as donor. So when this
 * is changed, this test needs to be changed.
 *
 * @author Peter Veentjer.
 */
public class AtomicMethod_FastAtomicObjectMixinTest {

    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        setGlobalStmInstance(stm);
    }

    @Test
    public void test() {
        Class clazz = Bla.class;
        assertEquals(Object.class,clazz.getSuperclass());
        assertAllInstanceFieldsAreCopied(clazz);
        assertAllStaticFieldsAreCopied(clazz);
        assertAllMethodsAreCopied(clazz);
        assertAlphaAtomicObjectInterfaceIsCopied(clazz);
    }

    private void assertAllStaticFieldsAreCopied(Class clazz) {
        assertTrue(existsField(clazz,"lockOwnerUpdater"));
        assertTrue(existsField(clazz,"tranlocalUpdater"));
        assertTrue(existsField(clazz,"listenersUpdater"));
    }

    private void assertAlphaAtomicObjectInterfaceIsCopied(Class clazz) {
        assertTrue(hasInterface(clazz, AlphaAtomicObject.class));
    }

    private boolean hasInterface(Class clazz, Class theInterface){
        for(Class anInterface: clazz.getInterfaces()){
            if(anInterface.equals(theInterface)){
                return true;
            }
        }

          return false;
    }

    private void assertAllMethodsAreCopied(Class clazz) {
        testIncomplete();
    }

    private void assertAllInstanceFieldsAreCopied(Class clazz) {
        ClassUtils.printClassInfo(clazz);
        assertTrue(existsField(clazz,"lockOwner"));
        assertTrue(existsField(clazz,"tranlocal"));
        assertTrue(existsField(clazz,"listeners"));
    }

    @AtomicObject
    public static class Bla {
        int x;

        Bla(int x) {
            this.x = x;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }
    }
}
