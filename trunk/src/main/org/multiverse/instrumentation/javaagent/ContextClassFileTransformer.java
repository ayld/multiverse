package org.multiverse.instrumentation.javaagent;

import org.multiverse.instrumentation.utils.BytecodeWriteUtil;

import java.io.IOException;
import static java.lang.ClassLoader.getSystemClassLoader;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public abstract class ContextClassFileTransformer implements ClassFileTransformer {

    public abstract ClassFileTransformerContext createContext(ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer);

    private boolean debug = false;

    public ContextClassFileTransformer(boolean debug) {
        this.debug = debug;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public byte[] transform(ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        //if the classLoader is null, the boot classloader should be used
        if (classLoader == null)
            classLoader = getSystemClassLoader();

        try {
            ClassFileTransformerContext context = createContext(
                    classLoader,
                    className,
                    classBeingRedefined,
                    protectionDomain,
                    classfileBuffer);
            byte[] transformedBytecode = context.transform();
            if (debug)
                writeDebugInformation(className, transformedBytecode);
            return transformedBytecode;
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            throw ex;
        } catch (IllegalClassFormatException ex) {
            ex.printStackTrace();
            throw ex;
        } catch (Error e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void writeDebugInformation(String classname, byte[] bytecode) {
        try {
            BytecodeWriteUtil.writeToFileInTmpDirectory(classname + ".class", bytecode);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write classfile debug information", e);
        }
    }
}
