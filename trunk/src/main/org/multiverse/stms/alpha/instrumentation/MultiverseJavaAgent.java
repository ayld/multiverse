package org.multiverse.stms.alpha.instrumentation;

import org.multiverse.stms.alpha.instrumentation.asm.*;
import static org.multiverse.stms.alpha.instrumentation.asm.AsmUtils.loadAsClassNode;
import static org.multiverse.stms.alpha.instrumentation.asm.AsmUtils.toBytecode;
import org.multiverse.stms.alpha.mixins.FastAtomicObjectMixin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;

/**
 * @author Peter Veentjer.
 */
public class MultiverseJavaAgent {

    public static void premain(String agentArgs, Instrumentation inst) throws UnmodifiableClassException {
        System.out.println("Starting the Multiverse JavaAgent");

        //it is very important that the order of these transformers is not
        //changed, unless you really know what you are doing.
        inst.addTransformer(new JSRInlineClassFileTransformer());
        inst.addTransformer(new MetadataExtractorClassFileTransformer());
        inst.addTransformer(new TranlocalClassFileTransformer());
        inst.addTransformer(new TranlocalSnapshotClassFileTransformer());
        inst.addTransformer(new AtomicObjectClassFileTransformer());
        inst.addTransformer(new AtomicMethodClassFileTransformer());
    }

    public static class JSRInlineClassFileTransformer extends AbstractClassFileTransformer {

        public JSRInlineClassFileTransformer() {
            super("JSRInlineTransformer");
        }

        @Override
        public byte[] doTransform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            JSRInlineClassAdapter inlineAdapter = new JSRInlineClassAdapter(writer);
            ClassReader reader = new ClassReader(classfileBuffer);
            reader.accept(inlineAdapter, ClassReader.EXPAND_FRAMES);
            return writer.toByteArray();
        }
    }

    public static class MetadataExtractorClassFileTransformer extends AbstractClassFileTransformer {

        public MetadataExtractorClassFileTransformer() {
            super("MetdadataExtractor");
        }


        @Override
        public byte[] doTransform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] originalBytecode) throws IllegalClassFormatException {
            //System.out.println("Extracting metadata from class: " + className);
            ClassNode original = loadAsClassNode(originalBytecode);
            MetadataExtractor transformer = new MetadataExtractor(original);
            transformer.extract();
//            AsmUtils.writeToFileInTmpDirectory(className + "__Original" + ".class", originalBytecode);
            return null;
        }
    }

    public static class AtomicObjectClassFileTransformer extends AbstractClassFileTransformer {

        public AtomicObjectClassFileTransformer() {
            super("AtomicObjectTransformer");
        }

        @Override
        public byte[] doTransform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] bytecode) throws IllegalClassFormatException {
            if (metadataService.isRealAtomicObject(className)) {
                ClassNode mixin = loadAsClassNode(FastAtomicObjectMixin.class);
                ClassNode original = loadAsClassNode(bytecode);
                AtomicObjectTransformer transformer = new AtomicObjectTransformer(original, mixin);
                ClassNode result = transformer.transform();
                byte[] resultCode = toBytecode(result);
//                AsmUtils.writeToFileInTmpDirectory(result.name + "__AtomicObject.class", resultCode);
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
        public byte[] doTransform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] bytecode) throws IllegalClassFormatException {
            if (metadataService.isRealAtomicObject(className)) {
                ClassNode original = loadAsClassNode(bytecode);
                TranlocalSnapshotFactory factory = new TranlocalSnapshotFactory(original);
                ClassNode result = factory.create();
                byte[] resultBytecode = toBytecode(result);
//                AsmUtils.writeToFileInTmpDirectory(result.name + ".class", resultBytecode);
                MultiverseClassLoader.INSTANCE.defineClass(result.name, resultBytecode);

            }

            return null;
        }
    }

    public static class TranlocalClassFileTransformer extends AbstractClassFileTransformer {
        public TranlocalClassFileTransformer() {
            super("TranslocalFactory");
        }

        @Override
        public byte[] doTransform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] bytecode) throws IllegalClassFormatException {
            if (metadataService.isRealAtomicObject(className)) {
                ClassNode original = loadAsClassNode(bytecode);
                TranlocalFactory transformer = new TranlocalFactory(original);
                ClassNode result = transformer.create();

                byte[] resultBytecode = toBytecode(result);
//                AsmUtils.writeToFileInTmpDirectory(result.name + ".class", resultBytecode);
                MultiverseClassLoader.INSTANCE.defineClass(result.name, resultBytecode);
            }

            return null;
        }
    }

    public static class AtomicMethodClassFileTransformer extends AbstractClassFileTransformer {

        public AtomicMethodClassFileTransformer() {
            super("AtomicMethodTransformer");
        }

        @Override
        public byte[] doTransform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

            if (metadataService.hasAtomicMethods(className)) {
                ClassNode original = loadAsClassNode(classfileBuffer);
                AtomicMethodTransformer transformer = new AtomicMethodTransformer(original);
                ClassNode result = transformer.transform();
                byte[] resultBytecode = toBytecode(result);
//                AsmUtils.writeToFileInTmpDirectory(result.name + "__WithTransaction.class", resultBytecode);
                for (ClassNode innerClass : transformer.getInnerClasses()) {
                    byte[] templateBytecode = toBytecode(innerClass);
                    //                  AsmUtils.writeToFileInTmpDirectory(innerClass.name + ".class", templateBytecode);
                    MultiverseClassLoader.INSTANCE.defineClass(innerClass.name, templateBytecode);
                }

                return resultBytecode;
            }

            return null;
        }
    }
}