package org.multiverse.instrumentation.javaagent.utils;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.io.IOException;

/**
 *
 */
public class AsmUtils {

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

        instructions.add(method.instructions);
        method.instructions = instructions;
    }

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

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
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
