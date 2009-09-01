package org.multiverse.stms.alpha.instrumentation.asm;

import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.api.annotations.Exclude;
import org.multiverse.utils.TodoException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Type.getDescriptor;
import static org.objectweb.asm.Type.getInternalName;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingMethodAdapter;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.*;
import static java.lang.String.format;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public final class AsmUtils implements Opcodes {

    public static int upgradeToPublic(int access) {
        if (isPublic(access)) {
            return access;
        }

        if (isPrivate(access)) {
            access = access - ACC_PRIVATE;
        } else if (isProtected(access)) {
            access = access - ACC_PROTECTED;
        }

        return access + ACC_PUBLIC;
    }

    public static AbstractInsnNode findFirstInstructionAfterSuper(String superClass, MethodNode constructor) {
        int index = findIndexOfFirstInstructionAfterSuper(superClass, constructor);
        return constructor.instructions.get(index);
    }

    /**
     * Implementation is not full proof. It will also find init calls done to the
     * same owner while evaluating arguments of the constructor
     *
     * @param superClass
     * @param constructor
     * @return
     */
    public static int findIndexOfFirstInstructionAfterSuper(String superClass, MethodNode constructor) {
        if (!constructor.name.equals("<init>")) {
            throw new RuntimeException();
        }

        //System.out.println("-----------------------------------");
        //System.out.println("search: "+superClass + "." + constructor.name + constructor.desc);


        ListIterator<AbstractInsnNode> i = constructor.instructions.iterator();
        int index = 0;
        for (; i.hasNext();) {
            AbstractInsnNode insnNode = i.next();
            //System.out.println(insnNode.getOpcode()+" insn "+insnNode.getClass().getSimpleName());
            if (insnNode.getType() == AbstractInsnNode.METHOD_INSN) {

                MethodInsnNode m = (MethodInsnNode) insnNode;
                //System.out.println("at method: "+m.owner+"."+m.name+m.desc);
                if (m.owner.equals(superClass) && m.name.equals("<init>")) {

                    //it is the instruction after the <init> call we are interested in
                    index++;
                    return index;
                }
            }
            index++;
        }

        //String msg = format("Did not found the super init call in constructor %s.%s%s", constructor.
        throw new TodoException();
    }


    /**
     * A new constructor descriptor is created by adding the extraArgType
     * as the first argument (so the other arguments all shift one pos).
     *
     * @param oldDesc the old method description
     * @return the new method description.
     */
    public static String createShiftedMethodDescriptor(String oldDesc, String extraArgType) {
        Type[] oldArgTypes = Type.getArgumentTypes(oldDesc);
        Type[] newArgTypes = new Type[oldArgTypes.length + 1];
        newArgTypes[0] = Type.getObjectType(extraArgType);

        for (int k = 0; k < oldArgTypes.length; k++) {
            newArgTypes[k + 1] = oldArgTypes[k];
        }

        Type returnType = Type.getReturnType(oldDesc);
        return Type.getMethodDescriptor(returnType, newArgTypes);
    }


    public static Map<String, String> createRemapperMapForManagedObject(ClassNode classNode) {
        Map<String, String> map = new HashMap<String, String>();

        MetadataService prepareInfoMap = MetadataService.INSTANCE;

        String originalName = classNode.name;
        String tranlocalName = prepareInfoMap.getTranlocalName(classNode);

        for (FieldNode field : prepareInfoMap.getManagedInstanceFields(classNode)) {
            String oldName = originalName + "." + field.name;
            String newName = tranlocalName + "." + field.name;
            //System.out.printf("%s %s\n", oldName, newName);
            map.put(oldName, newName);
        }

        return map;
    }

    public static boolean isExcluded(FieldNode field) {
        return hasVisibleAnnotation(field, Exclude.class);
    }

    public static boolean hasAtomicMethodAnnotation(MethodNode methodNode) {
        return hasVisibleAnnotation(methodNode, AtomicMethod.class);
    }

    public static boolean hasAtomicObjectAnnotation(ClassNode classNode) {
        return hasVisibleAnnotation(classNode, AtomicObject.class);
    }

    public static MethodNode remap(MethodNode originalMethod, Remapper remapper) {
        String[] exceptions = getExceptions(originalMethod);

        MethodNode mappedMethod = new MethodNode(
                originalMethod.access,
                originalMethod.name,
                remapper.mapMethodDesc(originalMethod.desc),
                remapper.mapSignature(originalMethod.signature, false),
                remapper.mapTypes(exceptions));

        RemappingMethodAdapter remapVisitor = new RemappingMethodAdapter(
                mappedMethod.access,
                mappedMethod.desc,
                mappedMethod,
                remapper);
        originalMethod.accept(remapVisitor);
        return mappedMethod;
    }


    public static String[] getExceptions(MethodNode originalMethod) {
        if (originalMethod.exceptions == null) {
            return new String[]{};
        }

        String[] exceptions = new String[originalMethod.exceptions.size()];
        originalMethod.exceptions.toArray(exceptions);
        return exceptions;
    }

    public static void verify(ClassNode classNode) {
        verify(toBytecode(classNode));
    }

    /**
     * Checks if the bytecode is valid.
     *
     * @param bytes the bytecode to check.
     */
    public static void verify(byte[] bytes) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        CheckClassAdapter.verify(new ClassReader(bytes), false, pw);
        String msg = sw.toString();
        if (msg.length() > 0) {
            throw new RuntimeException(msg);
        }
    }

    /**
     * Loads a Class as ClassNode. The ClassLoader of the Class is used to retrieve a resource stream.
     *
     * @param clazz the Class to load as ClassNode.
     * @return the loaded ClassNode.
     */
    public static ClassNode loadAsClassNode(Class clazz) {
        return loadAsClassNode(clazz.getClassLoader(), getInternalName(clazz));
    }

    /**
     * Loads a Class as ClassNode.
     *
     * @param loader            the ClassLoader to get the resource stream of.
     * @param classInternalForm the internal name of the Class to load.
     * @return the loaded ClassNode.
     */
    public static ClassNode loadAsClassNode(ClassLoader loader, String classInternalForm) {
        if (loader == null || classInternalForm == null) {
            throw new NullPointerException();
        }

        String fileName = classInternalForm + ".class";
        InputStream is = loader.getResourceAsStream(fileName);

        try {
            ClassNode classNode = new ClassNode();
            ClassReader reader = new ClassReader(is);
            reader.accept(classNode, ClassReader.EXPAND_FRAMES);
            return classNode;
        } catch (IOException e) {
            throw new RuntimeException("A problem ocurred while loading class" + fileName, e);
        }
    }


    /**
     * Checks if a ClassNode has the specified visible annotation.
     *
     * @param memberNode     the ClassNode to check
     * @param anotationClass the Annotation class that is checked for.
     * @return true if classNode has the specified annotation, false otherwise.
     */
    public static boolean hasVisibleAnnotation(MemberNode memberNode, Class anotationClass) {
        if (memberNode == null || anotationClass == null) {
            throw new NullPointerException();
        }

        if (memberNode.visibleAnnotations == null) {
            return false;
        }

        String annotationClassDescriptor = getDescriptor(anotationClass);

        for (AnnotationNode node : (List<AnnotationNode>) memberNode.visibleAnnotations) {
            if (annotationClassDescriptor.equals(node.desc))
                return true;
        }

        return false;
    }

    public static String internalFormToDescriptor(String internalForm) {
        return format("L%s;", internalForm);
    }


    public static Method getMethod(Class clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static Field getField(Class clazz, String fieldName) {
        try {
            return clazz.getField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isAbstract(MethodNode methodNode) {
        return isAbstract(methodNode.access);
    }

    public static boolean isInterface(ClassNode classNode) {
        return (classNode.access & Opcodes.ACC_INTERFACE) != 0;
    }

    public static boolean isNative(MethodNode methodNode) {
        return isNative(methodNode.access);
    }

    public static boolean isStatic(FieldNode fieldNode) {
        return isStatic(fieldNode.access);
    }

    public static boolean isStatic(MethodNode methodNode) {
        return isStatic(methodNode.access);
    }

    public static boolean isPrivate(MethodNode methodNode) {
        return isPrivate(methodNode.access);
    }

    public static boolean isPrivate(int access) {
        return (access & Opcodes.ACC_PRIVATE) != 0;
    }

    public static boolean isPublic(int access) {
        return (access & Opcodes.ACC_PUBLIC) != 0;
    }

    public static boolean isProtected(int access) {
        return (access & Opcodes.ACC_PROTECTED) != 0;
    }

    public static boolean isFinal(int access) {
        return (access & Opcodes.ACC_FINAL) != 0;
    }

    public static boolean isSynthetic(int access) {
        return (access & Opcodes.ACC_SYNTHETIC) != 0;
    }

    public static boolean isNative(int access) {
        return (access & Opcodes.ACC_NATIVE) != 0;
    }

    public static boolean isStatic(int access) {
        return (access & Opcodes.ACC_STATIC) != 0;
    }

    public static boolean isAbstract(int access) {
        return (access & Opcodes.ACC_ABSTRACT) != 0;
    }

    public static ClassNode loadAsClassNode(byte[] bytecode) {
        if (bytecode == null) {
            throw new NullPointerException();
        }

        ClassNode classNode = new ClassNode();
        ClassReader cr = new ClassReader(bytecode);
        cr.accept(classNode, ClassReader.EXPAND_FRAMES);
        return classNode;
    }

    public static byte[] toBytecode(ClassNode classNode) {
        if (classNode == null) {
            throw new NullPointerException();
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classNode.accept(cw);
        return cw.toByteArray();
    }

    public static void writeToFixedTmpFile(Class clazz) {
        byte[] bytecode = toBytecode(loadAsClassNode(clazz));
        writeToFixedTmpFile(bytecode);
    }

    public static void writeToFixedTmpFile(ClassNode clazz) {
        byte[] bytecode = toBytecode(clazz);
        writeToFixedTmpFile(bytecode);
    }

    public static void writeToFixedTmpFile(byte[] bytecode) {
        File file = new File(getTmpDir(), "debug.class");
        writeToFile(file, bytecode);
    }

    private static String getTmpDir() {
        return System.getProperty("java.io.tmpdir");
    }

    public static File writeToFileInTmpDirectory(String filename, byte[] bytecode) {
        File file = new File(getTmpDir(), filename);
        writeToFile(file, bytecode);
        return file;
    }

    public static void writeToTmpFile(byte[] bytecode) throws IOException {
        File file = File.createTempFile("foo", ".class");
        writeToFile(file, bytecode);
    }

    public static void writeToFile(File file, byte[] bytecode) {
        if (file == null || bytecode == null) {
            throw new NullPointerException();
        }

        try {
            ensureExistingParent(file);

            OutputStream writer = new FileOutputStream(file);
            try {
                writer.write(bytecode);
            } finally {
                writer.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void ensureExistingParent(File file) throws IOException {
        File parent = file.getParentFile();
        if (parent.isDirectory()) {
            return;
        }

        if (!parent.mkdirs()) {
            throw new IOException("Failed to make parent directories for file " + file);
        }
    }

    public static int getLoadOpcode(Type type) {
        switch (type.getSort()) {
            case Type.BOOLEAN:
                //fall through
            case Type.CHAR:
                //fall through
            case Type.BYTE:
                //fall through
            case Type.SHORT:
                //fall through
            case Type.INT:
                return Opcodes.ILOAD;
            case Type.LONG:
                return Opcodes.LLOAD;
            case Type.FLOAT:
                return Opcodes.FLOAD;
            case Type.DOUBLE:
                return Opcodes.DLOAD;
            case Type.OBJECT:
                //fall through
            case Type.ARRAY:
                return Opcodes.ALOAD;
            default:
                throw new RuntimeException("unhandled returntype: " + type);
        }
    }

    public static int getReturnOpcode(Type returnType) {
        switch (returnType.getSort()) {
            case Type.VOID:
                return Opcodes.RETURN;
            case Type.BOOLEAN:
                //fall through
            case Type.CHAR:
                //fall through
            case Type.BYTE:
                //fall through
            case Type.SHORT:
                //fall through
            case Type.INT:
                return Opcodes.IRETURN;
            case Type.LONG:
                return Opcodes.LRETURN;
            case Type.FLOAT:
                return Opcodes.FRETURN;
            case Type.DOUBLE:
                return Opcodes.DRETURN;
            case Type.OBJECT:
                return Opcodes.ARETURN;
            case Type.ARRAY:
                return Opcodes.ARETURN;//tod: this correct?
            default:
                throw new RuntimeException("unhandled returntype: " + returnType);
        }
    }

    private AsmUtils() {
    }
}
