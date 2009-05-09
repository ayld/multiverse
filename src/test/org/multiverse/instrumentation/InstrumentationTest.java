package org.multiverse.instrumentation;

import org.junit.Before;
import org.multiverse.instrumentation.utils.AsmUtils;
import static org.multiverse.instrumentation.utils.AsmUtils.toBytecode;

import java.lang.instrument.ClassFileTransformer;

public class InstrumentationTest {

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

    public static class MyClassLoader extends ClassLoader {
        public Class defineClass(String name, byte[] b) {
            return defineClass(name, b, 0, b.length);
        }
    }
}
