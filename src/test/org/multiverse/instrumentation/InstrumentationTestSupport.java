package org.multiverse.instrumentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.multiverse.instrumentation.utils.AsmUtils;
import static org.multiverse.instrumentation.utils.AsmUtils.toBytecode;
import org.multiverse.multiversionedstm.MaterializedObject;

import java.lang.instrument.ClassFileTransformer;

public class InstrumentationTestSupport {

    protected MyClassLoader classLoader;

    @Before
    public void initClassloader() throws Exception {
        classLoader = new MyClassLoader();
    }

    public void instrument(ClassFileTransformer transformer, Class clazz) throws Exception {
        String classname = clazz.getName();

        byte[] newBytecode = transformer.transform(
                classLoader,
                clazz.getName(),
                clazz,
                null,
                toBytecode(classname));

        AsmUtils.writeToFixedTmpFile(newBytecode);
        classLoader.defineClass(classname, newBytecode);
    }

    public static void assertDirty(Object object) {
        assertDirty(object, true);
    }

    public static void assertNotDirty(Object object) {
        assertDirty(object, false);
    }

    public static void assertDirty(Object object, boolean expected) {
        assertTrue(object instanceof MaterializedObject);
        assertEquals(expected, ((MaterializedObject) object).isDirty());
    }

    public static class MyClassLoader extends ClassLoader {
        public Class defineClass(String name, byte[] b) {
            return defineClass(name, b, 0, b.length);
        }
    }
}
