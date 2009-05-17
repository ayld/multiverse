package org.multiverse.instrumentation.utils;

import org.objectweb.asm.Opcodes;
import static org.objectweb.asm.Type.*;
import org.objectweb.asm.tree.*;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class InstructionsBuilder implements Opcodes {

    private final InsnList instructions = new InsnList();

    public InstructionsBuilder() {
    }

    public void codeForThrowRuntimeException() {
        NEW(RuntimeException.class);
        DUP();
        INVOKESPECIAL(AsmUtils.getConstructor(RuntimeException.class));
        ATHROW();
    }

    public void codeForPrintClassTopItem() {
        //[.., ref]
        DUP();
        //[.., ref, ref ]
        GETSTATIC(getInternalName(System.class), "out", getDescriptor(PrintStream.class));
        //[.., ref, ref, printstream]
        SWAP();
        //[.., ref, printstream, ref]
        Method getClassMethod = AsmUtils.getMethod(Object.class, "getClass");
        INVOKEVIRTUAL(getInternalName(Object.class), getClassMethod.getName(), getMethodDescriptor(getClassMethod));
        //[.., ref, printstream, class]                    
        Method printlnMethod = AsmUtils.getMethod(PrintStream.class, "println", Object.class);
        INVOKEVIRTUAL(getInternalName(PrintStream.class), printlnMethod.getName(), getMethodDescriptor(printlnMethod));
        //[.., ref]        
    }

    public void ALOAD(int index) {
        VarInsnNode instr = new VarInsnNode(ALOAD, index);
        instructions.add(instr);
    }

    public void NEW(ClassNode classNode) {
        NEW(classNode.name);
    }

    public void NEW(Class clazz) {
        NEW(getInternalName(clazz));
    }

    public void NEW(String desc) {
        TypeInsnNode ins = new TypeInsnNode(
                NEW,
                desc);
        instructions.add(ins);
    }

    public void INVOKEINTERFACE(Method method) {
        MethodInsnNode instr = new MethodInsnNode(
                INVOKEINTERFACE,
                getInternalName(method.getDeclaringClass()),
                method.getName(),
                getMethodDescriptor(method));
        instructions.add(instr);
    }

    public void INVOKESPECIAL(ClassNode clazzNode, String name, String descriptor) {
        INVOKESPECIAL(clazzNode.name, name, descriptor);
    }

    public void INVOKESPECIAL(Class clazz, String name, String descriptor) {
        INVOKESPECIAL(getInternalName(clazz), name, descriptor);
    }

    public void INVOKESPECIAL(Constructor constructor) {
        INVOKESPECIAL(getInternalName(
                constructor.getDeclaringClass()),
                "<init>",
                getConstructorDescriptor(constructor));
    }

    public void INVOKESPECIAL(Method method) {
        INVOKESPECIAL(
                getInternalName(method.getDeclaringClass()),
                method.getName(),
                getMethodDescriptor(method));
    }


    public void INVOKESPECIAL(String owner, String name, String descriptor) {
        MethodInsnNode instr = new MethodInsnNode(
                INVOKESPECIAL,
                owner,
                name,
                descriptor);
        instructions.add(instr);
    }

    public void INVOKEVIRTUAL(String owner, String name, String descriptor) {
        MethodInsnNode instr = new MethodInsnNode(
                INVOKEVIRTUAL,
                owner,
                name,
                descriptor);
        instructions.add(instr);
    }

    public void INVOKESTATIC(Method method) {
        MethodInsnNode instr = new MethodInsnNode(
                INVOKESTATIC,
                getInternalName(method.getDeclaringClass()),
                method.getName(),
                getMethodDescriptor(method));
        instructions.add(instr);
    }

    public void PUTFIELD(ClassNode owner, String name, ClassNode fieldType) {
        PUTFIELD(owner.name, name, AsmUtils.internalFormToDescriptor(fieldType.name));
    }

    public void PUTFIELD(ClassNode owner, String name, String fieldType) {
        PUTFIELD(owner.name, name, fieldType);
    }

    public void PUTFIELD(ClassNode owner, String name, Class fieldType) {
        PUTFIELD(owner.name, name, getDescriptor(fieldType));
    }

    public void PUTFIELD(Class owner, String name, Class fieldType) {
        PUTFIELD(getInternalName(owner), name, fieldType);
    }

    public void PUTFIELD(String owner, String name, Class fieldType) {
        PUTFIELD(owner, name, getDescriptor(fieldType));
    }

    public void PUTFIELD(String owner, String name, String fieldType) {
        FieldInsnNode instr = new FieldInsnNode(
                PUTFIELD,
                owner,
                name,
                fieldType
        );
        instructions.add(instr);
    }

    public void GETFIELD(ClassNode owner, FieldNode field) {
        GETFIELD(owner, field.name, field.desc);
    }

    public void GETFIELD(ClassNode owner, String name, String desc) {
        GETFIELD(owner.name, name, desc);
    }

    public void GETFIELD(ClassNode owner, String name, Class fieldType) {
        GETFIELD(owner.name, name, getDescriptor(fieldType));
    }

    public void GETFIELD(Class owner, String name) {
        GETFIELD(getInternalName(owner), name, AsmUtils.getField(owner, name).getType());
    }

    public void GETFIELD(Class owner, String name, Class fieldType) {
        GETFIELD(getInternalName(owner), name, fieldType);
    }

    public void GETFIELD(String owner, String name, Class fieldType) {
        GETFIELD(owner, name, getDescriptor(fieldType));
    }

    public void GETFIELD(String owner, String name, String fieldDesc) {
        FieldInsnNode instr = new FieldInsnNode(
                GETFIELD,
                owner,
                name,
                fieldDesc
        );
        instructions.add(instr);
    }

    public void GETSTATIC(String owner, String name, String fieldDesc) {
        FieldInsnNode instr = new FieldInsnNode(
                GETSTATIC,
                owner,
                name,
                fieldDesc
        );
        instructions.add(instr);
    }

    public void DUP() {
        instructions.add(new InsnNode(DUP));
    }

    public void DUP2() {
        instructions.add(new InsnNode(DUP2));
    }

    public void DUP_X1() {
        instructions.add(new InsnNode(DUP_X1));
    }

    public void POP() {
        instructions.add(new InsnNode(POP));
    }

    public void ATHROW() {
        instructions.add(new InsnNode(ATHROW));
    }

    public void ACONST_NULL() {
        instructions.add(new InsnNode(ACONST_NULL));
    }

    public void ARETURN() {
        instructions.add(new InsnNode(ARETURN));
    }

    public void RETURN() {
        instructions.add(new InsnNode(RETURN));
    }

    public void IRETURN() {
        instructions.add(new InsnNode(IRETURN));
    }

    public void SWAP() {
        instructions.add(new InsnNode(SWAP));
    }

    public void IFNONNULL(LabelNode label) {
        instructions.add(new JumpInsnNode(IFNONNULL, label));
    }

    public void ICONST_0() {
        instructions.add(new InsnNode(ICONST_0));
    }

    public void ICONST_FALSE() {
        ICONST_0();
    }

    public void ICONST_TRUE() {
        ICONST_1();
    }

    public void ICONST_1() {
        instructions.add(new InsnNode(ICONST_1));
    }

    public void IF_ICMPEQ(LabelNode success) {
        instructions.add(new JumpInsnNode(IF_ICMPEQ, success));
    }

    public void IFNULL(LabelNode success) {
        instructions.add(new JumpInsnNode(IFNULL, success));
    }

    public void IF_ACMPEQ(LabelNode success) {
        instructions.add(new JumpInsnNode(IF_ACMPEQ, success));
    }

    public void placeLabelNode(LabelNode labelNode) {
        instructions.add(labelNode);
    }

    public void CHECKCAST(Class required) {
        CHECKCAST(getDescriptor(required));
    }

    public void CHECKCAST(String requiredInternalName) {
        TypeInsnNode in = new TypeInsnNode(CHECKCAST, requiredInternalName);
        instructions.add(in);
    }

    public InsnList createInstructions() {
        return instructions;
    }
}
