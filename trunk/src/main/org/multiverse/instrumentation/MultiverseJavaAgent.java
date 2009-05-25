package org.multiverse.instrumentation;

import org.multiverse.api.annotations.TmEntity;
import static org.multiverse.instrumentation.utils.AsmUtils.*;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.List;

public class MultiverseJavaAgent {

    //method that a javaagent must implement.
    public static void premain(String agentArgs, Instrumentation inst) throws UnmodifiableClassException {
        System.out.println("Starting the Multiverse JavaAgent");

        inst.addTransformer(new ExcludeUnmapablesClassFileTransformer());
        inst.addTransformer(new Phase1ClassFileTransformer());
        inst.addTransformer(new Phase2ClassFileTransformer());
        inst.addTransformer(new Phase3ClassFileTransformer());
    }

    public static class ExcludeUnmapablesClassFileTransformer implements ClassFileTransformer {

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

            if (className.startsWith("java")) {
                return null;
            }

            ClassNode classNode = toClassNode(classfileBuffer);

            //a hack for performance.. atm we only check tmentities.. but object that change fields
            //on tm entities need to be changed in the future as well.
            if (!hasVisibleAnnotation(classNode, TmEntity.class)) {
                return null;
            }

            for (FieldNode field : (List<FieldNode>) classNode.fields) {
                if (isExcluded(field)) {
                    //excluded fields are completely ignored.
                } else if (isVolatile(field)) {
                    System.err.printf(
                            "Warning: field '%s.%s' is volatile and is excluded from Multiverse." +
                                    " volatile fields don't make sense to use in an STM.\n",
                            className,
                            field.name);
                    exclude(field);
                } else if (isSynthetic(field)) {
                    //exclude all syntethic fields
                    exclude(field);
                } else if (isStatic(field)) {
                    System.err.printf(
                            "Warning: field '%s.%s' is static and is excluded from Multiverse." +
                                    " static fields are not supported yet.\n",
                            className,
                            field.name);
                    exclude(field);
                } else if (isArrayType(field.desc)) {
                    System.err.printf(
                            "Warning: field '%s.%s' is an array and is excluded from Multiverse." +
                                    " Arrays are not supported yet.\n",
                            className,
                            field.name);
                    exclude(field);
                }
            }

            return toBytecode(classNode);
        }
    }

    /**
     * responsible for transforming access to fields of TmEntities objects.
     */
    public static class Phase1ClassFileTransformer implements ClassFileTransformer {

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            try {
                if (className.startsWith("java")) {
                    return null;
                }

                ClassNode classNode = toClassNode(classfileBuffer);

                //a hack for performance.. atm we only check tmentities.. but object that change fields
                //on tm entities need to be changed in the future as well.
                if (!hasVisibleAnnotation(classNode, TmEntity.class)) {
                    return null;
                }

                LazyAccessTransformer transformer = new LazyAccessTransformer(classNode, loader);
                ClassNode transformedClassNode = transformer.create();
                return toBytecode(transformedClassNode);
            } catch (RuntimeException ex) {
                ex.printStackTrace();
                throw ex;
            } catch (Error e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    /**
     * responsible for transforming and generated the dematerializable/dematerialized classes
     */
    public static class Phase2ClassFileTransformer implements ClassFileTransformer {

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            try {
                if (className.startsWith("java")) {
                    return null;
                }

                ClassNode classNode = toClassNode(classfileBuffer);

                if (!hasVisibleAnnotation(classNode, TmEntity.class)) {
                    return null;
                }

                System.out.printf("Transforming class %s\n", className);

                DematerializedClassBuilder dematerializedClassBuilder = new DematerializedClassBuilder(classNode, loader);
                ClassNode dematerialized = dematerializedClassBuilder.create();

                MultiverseClassLoader.INSTANCE.defineClass(dematerialized);

                TmEntityClassTransformer t = new TmEntityClassTransformer(classNode, dematerialized, loader);
                ClassNode materialized = t.create();

                return toBytecode(materialized);
            } catch (RuntimeException ex) {
                ex.printStackTrace();
                throw ex;
            } catch (Error e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    /**
     * Responsible for @atomic method transformation.
     */
    public static class Phase3ClassFileTransformer implements ClassFileTransformer {

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            try {
                if (className.startsWith("java") || className.startsWith("org/apache") || className.startsWith("sun/")) {
                    return null;
                }

                ClassNode classNode = toClassNode(classfileBuffer);

                AtomicTransformer transformer = new AtomicTransformer(classNode, loader);
                ClassNode transformedClassNode = transformer.create();

                for (ClassNode inner : transformer.getInnerClasses()) {
                    MultiverseClassLoader.INSTANCE.defineClass(inner);
                }

                return toBytecode(transformedClassNode);
            } catch (RuntimeException ex) {
                System.out.println("class: " + className);
                ex.printStackTrace();
                throw ex;
            } catch (Error e) {
                e.printStackTrace();
                throw e;
            }
        }
    }
}
