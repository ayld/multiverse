package org.multiverse.instrumentation.javaagent.utils;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.*;

/**
 *
 */
public class AsmUtils {

    public static String getShortClassName(ClassNode classNode) {
        String internalName = classNode.name;
        int lastIndex = internalName.lastIndexOf('/');
        if (lastIndex == -1)
            return internalName;

        return internalName.substring(lastIndex + 1);
    }

    public static String getPackagename(ClassNode classNode) {
        String internalName = classNode.name;
        int lastIndex = internalName.lastIndexOf('/');
        if (lastIndex == -1)
            return internalName;

        return internalName.substring(0, lastIndex).replace('/', '.');
    }

    public static void verify(File file) {
        verify(toBytes(file));
    }

    public static byte[] toBytes(File file) {
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
     * Checks if the bytecode is valid.
     *
     * @param bytes the bytecode to check.
     */
    public static void verify(byte[] bytes) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        CheckClassAdapter.verify(new ClassReader(bytes), false, pw);
        String msg = sw.toString();
        if (msg.length() > 0)
            throw new RuntimeException(msg);
    }

    public static void verify(ClassNode classNode) {
        verify(toBytecode(classNode));
    }

    /**
     * Checks if a type is a secondary (primitive long or double) type or not.
     *
     * @param desc a description of the type
     * @return true if it is a secondary type, false otherwise.
     */
    public static boolean isSecondaryType(String desc) {
        return desc.equals("J") || desc.equals("L");
    }

    public static ClassNode loadAsClassNode(Class clazz) {
        //String fileName = Type.getInternalName(clazz) + ".class";
        String testClassName = clazz.getName().replace('.', '/');
        String fileName = testClassName.substring(testClassName.lastIndexOf('/') + 1) + ".class";
        InputStream is = clazz.getResourceAsStream(fileName);

        try {
            ClassNode classNode = new ClassNode();
            ClassReader reader = new ClassReader(is);
            reader.accept(classNode, 0);
            return classNode;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param originalByteCode
     * @return
     */
    public static ClassNode loadAsClassNode(byte[] originalByteCode) {
        if (originalByteCode == null) throw new NullPointerException();

        ClassNode classNode = new ClassNode();
        ClassReader cr = new ClassReader(originalByteCode);
        cr.accept(classNode, 0);
        return classNode;
    }

    /**
     * Returns the bytecode for a specific Class.
     *
     * @param classNode ClassNode of the class to load the bytecode for
     * @return the loaded bytecode
     * @throws NullPointerException if classNode is null.
     */
    public static byte[] toBytecode(ClassNode classNode) {
        if (classNode == null) throw new NullPointerException();

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
        if (classname == null) throw new NullPointerException();

        ClassReader reader = new ClassReader(classname);
        ClassWriter writer = new ClassWriter(reader, 0);
        reader.accept(writer, 0);
        return writer.toByteArray();
    }

    //we don't want instances.
    private AsmUtils() {
    }
}
