package org.multiverse.instrumentation.javaagent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class DematerializedClassGeneratorClassFileTransformer implements ClassFileTransformer{
    
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        return new byte[0];  //To change body of implemented methods use File | Settings | File Templates.
    }
}
