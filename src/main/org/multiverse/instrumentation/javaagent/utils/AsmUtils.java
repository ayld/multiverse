package org.multiverse.instrumentation.javaagent.utils;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.*;

/**
 *
 */
public class AsmUtils {

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

    /**
     * Checks if a type is a secondary (primitive long or double) type or not.
     *
     * @param desc a description of the type
     * @return true if it is a secondary type, false otherwise.
     */
    public static boolean isSecondaryType(String desc) {
        return desc.equals("J") || desc.equals("L");
    }

    public static void insertInFront(MethodNode method, InsnList instructions) {
        if (method == null || instructions == null) throw new NullPointerException();

        //instructions.add(method.instructions);
        //method.instructions = instructions;
        throw new RuntimeException();
    }

    /*
    public static boolean applyAndStopAfterFirstSuccess(InsnList insnList, FieldInsnNodeVisitor<Boolean> f) {
        if (insnList == null || f == null) throw new NullPointerException();

        AbstractInsnNode insn = insnList.getFirst();
        while (insn != null) {
            if (insn instanceof FieldInsnNode) {
                FieldInsnNode fieldInsnNode = (FieldInsnNode) insn;
                if (f.visit(fieldInsnNode)) {
                    return true;
                }
            }
            insn = insn.getNext();
        }

        return false;
    }

    public static void applyAndInsertBeforeEachField(InsnList insnList, FieldInsnNodeVisitor<InsnList> generator) {
        if (insnList == null || generator == null) throw new NullPointerException();

        AbstractInsnNode insn = insnList.getFirst();
        while (insn != null) {
            if (insn instanceof FieldInsnNode) {
                FieldInsnNode fieldInsnNode = (FieldInsnNode) insn;
                InsnList result = generator.visit(fieldInsnNode);
                insnList.insertBefore(fieldInsnNode, result);
            }
            insn = insn.getNext();
        }
    }

    public static void applyAndInsertAfterEachField(InsnList insnList, FieldInsnNodeVisitor<InsnList> generator) {
        if (insnList == null || generator == null) throw new NullPointerException();

        AbstractInsnNode insn = insnList.getFirst();
        while (insn != null) {
            if (insn instanceof FieldInsnNode) {
                FieldInsnNode fieldInsnNode = (FieldInsnNode) insn;
                InsnList result = generator.visit(fieldInsnNode);
                insnList.insert(insn, result);
            }
            insn = insn.getNext();
        }
    } */

    public static ClassNode loadAsClassNode(Class clazz) {
        String fileName = Type.getType(clazz).getInternalName() + ".class";
        InputStream is = clazz.getResourceAsStream(fileName);

        try {
            ClassNode classNode = new ClassNode();
            ClassReader reader = null;
            reader = new ClassReader(is);
            //reader.accept(classNode, 0);//todo
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
        //cr.accept(classNode, 0);   todo
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

        //ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        //classNode.accept(cw);
        //return cw.toByteArray();
        throw new RuntimeException();
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
        //ClassWriter writer = new ClassWriter(reader, 0);
        //reader.accept(writer, 0);  todo
        //return writer.toByteArray();
        throw new RuntimeException();
    }

    //we don't want instances.
    private AsmUtils() {
    }
}
