package org.multiverse.instrumentation.utils;

import org.multiverse.api.annotations.Exclude;
import org.multiverse.api.annotations.NonEscaping;
import org.multiverse.api.annotations.TmEntity;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Type.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.*;
import static java.lang.String.format;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Peter Veentjer.
 */
public final class AsmUtils {

    public static final AnnotationNode EXCLUDE = new AnnotationNode(Type.getDescriptor(Exclude.class));

    public static void exclude(FieldNode field) {
        if (field.visibleAnnotations == null) {
            field.visibleAnnotations = new LinkedList();
        }

        field.visibleAnnotations.add(EXCLUDE);
    }

    public static Constructor getConstructor(Class clazz, Class<?>... parameterTypes) {
        try {
            return clazz.getConstructor(parameterTypes);
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

    public static Method getMethod(Class clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getInternalNameOfDematerializedClass(ClassNode materializedClass) {
        if (materializedClass.outerClass == null) {
            return InternalFormClassnameUtil.getPackagename(materializedClass.name) +
                    "/" + InternalFormClassnameUtil.getBaseClassname(materializedClass.name) +
                    "$Dematerialized" + InternalFormClassnameUtil.getBaseClassname(materializedClass.name);
        } else {
            return InternalFormClassnameUtil.getPackagename(materializedClass.name) +
                    "/" + InternalFormClassnameUtil.getBaseClassname(materializedClass.outerClass) +
                    "$Dematerialized" + InternalFormClassnameUtil.getBaseClassname(materializedClass.name);
        }

    }

    public static String getInternalNameOfInnerClass(ClassNode outerclass, String basename) {
        if (outerclass.outerClass == null) {
            return InternalFormClassnameUtil.getPackagename(outerclass.name) +
                    "/" + InternalFormClassnameUtil.getBaseClassname(outerclass.name) +
                    basename + InternalFormClassnameUtil.getBaseClassname(outerclass.name);
        } else {
            return InternalFormClassnameUtil.getPackagename(outerclass.name) +
                    "/" + InternalFormClassnameUtil.getBaseClassname(outerclass.outerClass) +
                    basename + InternalFormClassnameUtil.getBaseClassname(outerclass.name);
        }

    }


    public static String getInnerInternalNameOfDematerializedClass(ClassNode materializedClass) {
        return "Dematerialized" + InternalFormClassnameUtil.getBaseClassname(materializedClass.name);
    }

    public static String getVoidMethodDescriptor(ClassNode... parameterTypes) {
        Type[] args = new Type[parameterTypes.length];
        for (int k = 0; k < parameterTypes.length; k++) {
            args[k] = getType(internalFormToDescriptor(parameterTypes[k].name));
        }

        return getMethodDescriptor(getType(Void.TYPE), args);
    }

    public static String getVoidMethodDescriptor(Type... parameterTypes) {
        return getMethodDescriptor(getType(Void.TYPE), parameterTypes);
    }

    public static String internalFormToDescriptor(String internalForm) {
        return format("L%s;", internalForm);
    }

    public static boolean isInterface(ClassNode classNode) {
        return (classNode.access & Opcodes.ACC_INTERFACE) != 0;
    }

    public static boolean isNative(MethodNode methodNode) {
        return (methodNode.access & Opcodes.ACC_NATIVE) != 0;
    }

    public static boolean isAbstract(MethodNode methodNode) {
        return (methodNode.access & Opcodes.ACC_ABSTRACT) != 0;
    }

    public static boolean isStatic(MethodNode methodNode) {
        return (methodNode.access & Opcodes.ACC_STATIC) != 0;
    }

    public static boolean isSynthetic(FieldNode fieldNode) {
        return (fieldNode.access & Opcodes.ACC_SYNTHETIC) != 0;
    }

    public static boolean isVolatile(FieldNode fieldNode) {
        return (fieldNode.access & Opcodes.ACC_VOLATILE) != 0;
    }

    public static boolean isStatic(FieldNode fieldNode) {
        return (fieldNode.access & Opcodes.ACC_STATIC) != 0;
    }

    public static boolean isObjectType(Type type) {
        return type.getSort() == Type.OBJECT;
    }

    public static boolean isTmEntity(String typeDescriptor, ClassLoader classLoader) {
        return hasVisibleAnnotation(typeDescriptor, TmEntity.class, classLoader);
    }

    public static boolean isExcluded(FieldNode fieldNode) {
        return hasVisibleAnnotation(fieldNode, Exclude.class);
    }

    public static boolean isNonEscaping(FieldNode fieldNode) {
        return hasVisibleAnnotation(fieldNode, NonEscaping.class);
    }

    public static void printFields(ClassNode classNode) {
        System.out.println("Printing fields for classNode: " + classNode.name);
        for (FieldNode fieldNode : (List<FieldNode>) classNode.fields) {
            System.out.println("fieldNode.name: " + fieldNode.name);
        }
    }

    private static boolean hasVisibleAnnotation(String typeDescriptor, Class annotationClass, ClassLoader classLoader) {
        if (typeDescriptor == null) {
            throw new NullPointerException();
        }

        if (annotationClass == null) {
            throw new NullPointerException();
        }

        if (classLoader == null) {
            throw new NullPointerException();
        }

        Type fieldType = getType(typeDescriptor);

        if (!isObjectType(fieldType)) {
            return false;
        }

        ClassNode classNode = loadAsClassNode(classLoader, fieldType.getInternalName());
        return hasVisibleAnnotation(classNode, annotationClass);
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

    public static void verify(ClassNode classNode) {
        verify(toBytecode(classNode));
    }

    public static void verify(File file) {
        verify(loadAsBytes(file));
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
            reader.accept(classNode, 0);
            return classNode;
        } catch (IOException e) {
            throw new RuntimeException("A problem ocurred while loading class" + fileName, e);
        }
    }

    /**
     * Loads bytecode as a ClassNode.
     *
     * @param bytecode the bytecode to load.
     * @return the created ClassNode.
     */
    public static ClassNode toClassNode(byte[] bytecode) {
        if (bytecode == null) {
            throw new NullPointerException();
        }

        ClassNode classNode = new ClassNode();
        ClassReader cr = new ClassReader(bytecode);
        cr.accept(classNode, 0);
        return classNode;
    }


    /**
     * Loads a file as a byte array.
     *
     * @param file the File to load.
     * @return the loaded bytearray.
     * @throws RuntimeException if an io error occurs.
     */
    public static byte[] loadAsBytes(File file) {
        try {
            InputStream in = new FileInputStream(file);
            // Get the size of the file
            long length = file.length();

            // You cannot create an array using a long type.
            // It needs to be an int type.
            // Before converting to an int type, check
            // to ensure that file is not larger than Integer.MAX_VALUE.
            if (length > Integer.MAX_VALUE) {
                // File is too large
                throw new RuntimeException("file too large");
            }

            // Create the byte array to hold the data
            byte[] bytes = new byte[(int) length];

            // Read in the bytes
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length
                    && (numRead = in.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }

            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file " + file.getName());
            }

            in.close();
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Transforms a ClassNode to bytecode.
     *
     * @param classNode the ClassNode to transform to bytecode.
     * @return the transformed bytecode.
     * @throws NullPointerException if classNode is null.
     */
    public static byte[] toBytecode(ClassNode classNode) {
        if (classNode == null) {
            throw new NullPointerException();
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classNode.accept(cw);
        return cw.toByteArray();
    }

    /**
     * Returns the bytecode for a specific Class.
     *
     * @param classname the name of the Class to load the bytecode for
     * @return the loaded bytecode
     * @throws IOException
     * @throws NullPointerException if classname is null.
     */
    public static byte[] toBytecode(String classname) throws IOException {
        if (classname == null) {
            throw new NullPointerException();
        }

        ClassReader reader = new ClassReader(classname);
        ClassWriter writer = new ClassWriter(reader, 0);
        reader.accept(writer, 0);
        return writer.toByteArray();
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

    public static File writeToFileInTmpDirectory(String filename, byte[] bytecode) throws IOException {
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

    /**
     * Creates a shallow clone of the insnList. So you get a new list, but the nodes are
     * the same.
     *
     * @param insnList the InsnList to clone
     * @return the cloned InsnList.
     * @throws NullPointerException if insnList is null.
     */
    public static InsnList cloneShallow(InsnList insnList) {
        if (insnList == null) {
            throw new NullPointerException();
        }

        InsnList cloned = new InsnList();
        for (int k = 0; k < insnList.size(); k++) {
            cloned.add(insnList.get(k));
        }
        return cloned;
    }

    //we don't want instances.
    private AsmUtils() {
    }

    public static boolean isArrayType(FieldNode field) {
        return isArrayType(field.desc);
    }

    public static boolean isArrayType(String desc) {
        return getObjectType(desc).getSort() == ARRAY;
    }
}
