package org.multiverse.stms.alpha.instrumentation.asm;

import org.multiverse.utils.InstrumentationProblemMonitor;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * A convenience ClassFileTransformer implementation. It does the following things:
 * <ol>
 * <li>ignored uninteresting packages like 'java/'</li>
 * <li>signals the InstrumentationProblemMonitor when a problem is encountered</li>
 * <li>prints a stacktrace when a problem is encountered. If you don't catch
 * it here, it will be eaten up</li>
 * </ol>
 *
 * @author Peter Veentjer
 */
public abstract class AbstractClassFileTransformer implements ClassFileTransformer {

    public MetadataService metadataService  = MetadataService.INSTANCE;

    private final String name;

    public abstract byte[] doTransform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException;


    /**
     * Constructs a AbstractClassFileTransformer.
     *
     * @param name a descriptor logging purposes.
     */
    public AbstractClassFileTransformer(String name){
        this.name = name;
    }

    @Override
    public final byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            if (isIgnoredPackage(className)) {
                return null;
            }

            return doTransform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
        } catch (RuntimeException ex) {
            InstrumentationProblemMonitor.INSTANCE.signalProblem();
            System.out.println("Failed while instrumenting class: " + className+" in transformer: "+name);
            ex.printStackTrace();
            throw ex;
        } catch (Error e) {
            InstrumentationProblemMonitor.INSTANCE.signalProblem();
            System.out.println("Failed while instrumenting class: " + className+" in transformer: "+name);
            e.printStackTrace();
            throw e;
        }
    }

    private static boolean isIgnoredPackage(String className) {
        return className.startsWith("java/") ||
                className.startsWith("com/jprofiler/") ||
                className.startsWith("org/junit") ||
                className.startsWith("sun/") ||
                className.startsWith("org/hamcrest/") ||
                className.startsWith("com/intellij") ||
                className.startsWith("org/eclipse") ||
                className.startsWith("junit/");
    }
}
