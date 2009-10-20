package org.multiverse.stms.alpha.instrumentation;

import org.multiverse.MultiverseConstants;
import org.multiverse.stms.alpha.instrumentation.asm.AbstractClassFileTransformer;
import org.multiverse.stms.alpha.instrumentation.asm.AsmUtils;
import static org.multiverse.stms.alpha.instrumentation.asm.AsmUtils.*;
import org.multiverse.stms.alpha.instrumentation.asm.AtomicMethodTransformer;
import org.multiverse.stms.alpha.instrumentation.asm.AtomicObjectFieldAccessTransformer;
import org.multiverse.stms.alpha.instrumentation.asm.AtomicObjectTransformer;
import org.multiverse.stms.alpha.instrumentation.asm.ImprovedAtomicMethodTransformer;
import org.multiverse.stms.alpha.instrumentation.asm.JSRInlineClassAdapter;
import org.multiverse.stms.alpha.instrumentation.asm.MetadataRepository;
import org.multiverse.stms.alpha.instrumentation.asm.TranlocalFactory;
import org.multiverse.stms.alpha.instrumentation.asm.TranlocalSnapshotFactory;
import org.multiverse.stms.alpha.mixins.FastAtomicObjectMixin;
import org.multiverse.utils.instrumentation.ClassUtils;
import static org.multiverse.utils.instrumentation.ClassUtils.defineClass;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getProperty;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;

/**
 * The JavaAgent that enhances classfiles specifically for the multiverse alpha stm engine.
 *
 * @author Peter Veentjer.
 */
public class MultiverseJavaAgent {

    public final static boolean DUMP_BYTECODE = parseBoolean(
            getProperty(MultiverseJavaAgent.class.getName()+".dumpBytecode","false"));

    public static void premain(String agentArgs, Instrumentation inst) throws UnmodifiableClassException {
        printInfo();
        registerTransformers(inst);
    }

    private static void registerTransformers(Instrumentation inst) {
        //it is very important that the order of these transformers is not
        //changed, unless you really know what you are doing.
        inst.addTransformer(new InitClassFileTransformer());
        inst.addTransformer(new JSRInlineClassFileTransformer());
        inst.addTransformer(new TranlocalClassFileTransformer());
        inst.addTransformer(new TranlocalSnapshotClassFileTransformer());
        inst.addTransformer(new AtomicObjectFieldAccessClassFileTransformer());
        inst.addTransformer(new AtomicObjectClassFileTransformer());
        inst.addTransformer(new AtomicMethodClassFileTransformer());
    }

    private static void printInfo() {
        System.out.println("Starting the Multiverse JavaAgent");

        if (MultiverseConstants.SANITY_CHECKS_ENABLED) {
            System.out.println("Sanity checks are enabled.");
        }
        
        if(DUMP_BYTECODE){
            System.out.printf("Bytecode will be dumped to '%s\n'", getTmpDir());
        }
    }

    /**
     * A ClassFileTransformer that does nothing
     */
    public static class InitClassFileTransformer extends AbstractClassFileTransformer {

        public InitClassFileTransformer() {
            super("InitClassFileTransformer");
        }

        @Override
        public byte[] doTransform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                  ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws IllegalClassFormatException {
            MetadataRepository.classLoader = loader;
            return null;
        }
    }

    public static class JSRInlineClassFileTransformer extends AbstractClassFileTransformer {

        public JSRInlineClassFileTransformer() {
            super("JSRInlineTransformer");
        }

        @Override
        public byte[] doTransform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                  ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws IllegalClassFormatException {
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            JSRInlineClassAdapter inlineAdapter = new JSRInlineClassAdapter(writer);
            ClassReader reader = new ClassReader(classfileBuffer);
            reader.accept(inlineAdapter, ClassReader.EXPAND_FRAMES);
            return writer.toByteArray();
        }
    }

    public static class AtomicObjectFieldAccessClassFileTransformer extends AbstractClassFileTransformer {

        public AtomicObjectFieldAccessClassFileTransformer() {
            super("AtomicObjectFieldAccessTransformer");
        }

        @Override
        public byte[] doTransform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                  ProtectionDomain protectionDomain, byte[] bytecode)
                throws IllegalClassFormatException {
            ClassNode original = loadAsClassNode(bytecode);
            AtomicObjectFieldAccessTransformer transformer = new AtomicObjectFieldAccessTransformer(original);
            ClassNode transformed = transformer.transform();
            byte[] transformedBytecode = toBytecode(transformed);
            if (DUMP_BYTECODE) {
                writeToFileInTmpDirectory(transformed.name + "_FixedFields.class", transformedBytecode);
            }
            return transformedBytecode;
        }
    }

    public static class AtomicObjectClassFileTransformer extends AbstractClassFileTransformer {

        public AtomicObjectClassFileTransformer() {
            super("AtomicObjectTransformer");
        }

        @Override
        public byte[] doTransform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                  ProtectionDomain protectionDomain, byte[] bytecode)
                throws IllegalClassFormatException {
            if (metadataRepository.isRealAtomicObject(className)) {
                ClassNode mixin = loadAsClassNode(FastAtomicObjectMixin.class);
                ClassNode original = loadAsClassNode(bytecode);
                AtomicObjectTransformer transformer = new AtomicObjectTransformer(original, mixin);
                ClassNode result = transformer.transform();
                byte[] resultCode = toBytecode(result);
                if (DUMP_BYTECODE) {
                    writeToFileInTmpDirectory(result.name + "__AtomicObject.class", resultCode);
                }
                return resultCode;
            }

            return null;
        }
    }

    public static class TranlocalSnapshotClassFileTransformer extends AbstractClassFileTransformer {

        public TranlocalSnapshotClassFileTransformer() {
            super("TranslocalSnapshotFactory");
        }

        @Override
        public byte[] doTransform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                  ProtectionDomain protectionDomain, byte[] bytecode)
                throws IllegalClassFormatException {
            if (metadataRepository.isRealAtomicObject(className)) {
                ClassNode original = loadAsClassNode(bytecode);
                TranlocalSnapshotFactory factory = new TranlocalSnapshotFactory(original);
                ClassNode result = factory.create();
                byte[] resultBytecode = toBytecode(result);
                if (DUMP_BYTECODE) {
                    writeToFileInTmpDirectory(result.name + ".class", resultBytecode);
                }
                defineClass(loader, result.name, resultBytecode);
            }

            return null;
        }
    }

    public static class TranlocalClassFileTransformer extends AbstractClassFileTransformer {

        public TranlocalClassFileTransformer() {
            super("TranslocalFactory");
        }

        @Override
        public byte[] doTransform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                  ProtectionDomain protectionDomain, byte[] bytecode)
                throws IllegalClassFormatException {
            if (metadataRepository.isRealAtomicObject(className)) {
                ClassNode original = loadAsClassNode(bytecode);
                TranlocalFactory transformer = new TranlocalFactory(original);
                ClassNode result = transformer.create();

                byte[] resultBytecode = toBytecode(result);
                if (DUMP_BYTECODE) {
                    writeToFileInTmpDirectory(result.name + ".class", resultBytecode);
                }
                defineClass(loader, result.name, resultBytecode);
            }

            return null;
        }
    }

    public static class AtomicMethodClassFileTransformer extends AbstractClassFileTransformer {

        public AtomicMethodClassFileTransformer() {
            super("AtomicMethodTransformer");
        }

        @Override
        public byte[] doTransform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                  ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws IllegalClassFormatException {
            if (metadataRepository.hasAtomicMethods(className)) {
                ClassNode original = loadAsClassNode(classfileBuffer);
                AtomicMethodTransformer transformer = new AtomicMethodTransformer(original);
                ClassNode result = transformer.transform();
                byte[] resultBytecode = toBytecode(result);

                if (DUMP_BYTECODE) {
                    writeToFileInTmpDirectory(result.name + "__WithTransaction.class", resultBytecode);
                }

                for (ClassNode innerClass : transformer.getInnerClasses()) {
                    byte[] templateBytecode = toBytecode(innerClass);
                    if (DUMP_BYTECODE) {
                        writeToFileInTmpDirectory(innerClass.name + ".class", templateBytecode);
                    }
                    defineClass(loader, innerClass.name, templateBytecode);
                }

                return resultBytecode;
            }

            return null;
        }
    }

    public static class ImprovedAtomicMethodClassFileTransformer extends AbstractClassFileTransformer {

        public ImprovedAtomicMethodClassFileTransformer() {
            super("AtomicMethodTransformer");
        }

        @Override
        public byte[] doTransform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                  ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws IllegalClassFormatException {
            if (metadataRepository.hasAtomicMethods(className)) {
                ClassNode original = loadAsClassNode(classfileBuffer);
                ImprovedAtomicMethodTransformer transformer = new ImprovedAtomicMethodTransformer(original);
                ClassNode result = transformer.transform();
                byte[] resultBytecode = toBytecode(result);

                if (DUMP_BYTECODE) {
                    AsmUtils.writeToFileInTmpDirectory(result.name + "__WithTransaction.class", resultBytecode);
                }

                for (ClassNode innerClass : transformer.getInnerClasses()) {
                    byte[] templateBytecode = toBytecode(innerClass);
                    if (DUMP_BYTECODE) {
                        AsmUtils.writeToFileInTmpDirectory(innerClass.name + ".class", templateBytecode);
                    }
                    ClassUtils.defineClass(loader, innerClass.name, templateBytecode);
                }

                return resultBytecode;
            }

            return null;
        }
    }
}