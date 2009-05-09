package org.multiverse.instrumentation.utils;

import static org.multiverse.instrumentation.utils.InstrumentationUtils.getConstructor;
import static org.multiverse.instrumentation.utils.InstrumentationUtils.getField;
import org.objectweb.asm.Opcodes;
import static org.objectweb.asm.Type.*;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedList;

public abstract class MethodBuilder implements Opcodes {

    protected MethodNode methodNode = new MethodNode();

    public MethodBuilder() {
        setAccess(ACC_PUBLIC | ACC_SYNTHETIC);
        methodNode.exceptions = new LinkedList();
        methodNode.tryCatchBlocks = new LinkedList();
    }

    public void setName(String name) {
        methodNode.name = name;
    }

    public void setAccess(int access) {
        methodNode.access = access;
    }

    public void setDescriptor(String desc) {
        methodNode.desc = desc;
    }

    public void initWithConstructor(Constructor constructor) {
        methodNode.access = constructor.getModifiers();
        methodNode.name = constructor.getName();
        methodNode.desc = getConstructorDescriptor(constructor);
    }

    public void initWithInterfaceMethod(Method method) {
        methodNode.name = method.getName();
        methodNode.desc = getMethodDescriptor(method);
    }

    public void codeForThrowRuntimeException() {
        NEW(RuntimeException.class);
        DUP();
        INVOKESPECIAL(getConstructor(RuntimeException.class));
        ATHROW();
    }

    public void ALOAD(int index) {
        VarInsnNode instr = new VarInsnNode(ALOAD, index);
        methodNode.instructions.add(instr);
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
        methodNode.instructions.add(ins);
    }

    public void INVOKEINTERFACE(Method method) {
        MethodInsnNode instr = new MethodInsnNode(
                INVOKEINTERFACE,
                getInternalName(method.getDeclaringClass()),
                method.getName(),
                getMethodDescriptor(method));
        methodNode.instructions.add(instr);
    }

    public void INVOKESPECIAL(ClassNode clazzNode, String name, String descriptor) {
        INVOKESPECIAL(clazzNode.name, name, descriptor);
    }

    public void INVOKESPECIAL(Class clazz, String name, String descriptor) {
        INVOKESPECIAL(getInternalName(clazz), name, descriptor);
    }

    public void INVOKESPECIAL(String owner, String name, String descriptor) {
        MethodInsnNode instr = new MethodInsnNode(
                INVOKESPECIAL,
                owner,
                name,
                descriptor);
        methodNode.instructions.add(instr);
    }

    public void INVOKESPECIAL(Constructor constructor) {
        MethodInsnNode instr = new MethodInsnNode(
                INVOKESPECIAL,
                getInternalName(constructor.getDeclaringClass()),
                "<init>",
                getConstructorDescriptor(constructor));
        methodNode.instructions.add(instr);
    }

    public void INVOKESPECIAL(Method method) {
        MethodInsnNode instr = new MethodInsnNode(
                INVOKESPECIAL,
                getInternalName(method.getDeclaringClass()),
                method.getName(),
                getMethodDescriptor(method));
        methodNode.instructions.add(instr);
    }

    public void INVOKESTATIC(Method method) {
        MethodInsnNode instr = new MethodInsnNode(
                INVOKESTATIC,
                getInternalName(method.getDeclaringClass()),
                method.getName(),
                getMethodDescriptor(method));
        methodNode.instructions.add(instr);
    }

    public void PUTFIELD(ClassNode owner, String name, ClassNode fieldType) {
        PUTFIELD(owner.name, name, InstrumentationUtils.internalFormToDescriptor(fieldType.name));
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
        methodNode.instructions.add(instr);
    }


    public void GETFIELD(ClassNode owner, String name, String desc) {
        GETFIELD(owner.name, name, desc);
    }

    public void GETFIELD(ClassNode owner, String name, Class fieldType) {
        GETFIELD(owner.name, name, getDescriptor(fieldType));
    }

    public void GETFIELD(Class owner, String name) {
        GETFIELD(getInternalName(owner), name, getField(owner, name).getType());
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
        methodNode.instructions.add(instr);
    }


    public void DUP() {
        methodNode.instructions.add(new InsnNode(DUP));
    }

    public void POP() {
        methodNode.instructions.add(new InsnNode(POP));
    }

    public void DUP_X1() {
        methodNode.instructions.add(new InsnNode(DUP_X1));
    }

    public void ATHROW() {
        methodNode.instructions.add(new InsnNode(ATHROW));
    }

    public void ACONST_NULL() {
        methodNode.instructions.add(new InsnNode(ACONST_NULL));
    }

    public void ARETURN() {
        methodNode.instructions.add(new InsnNode(ARETURN));
    }

    public void RETURN() {
        methodNode.instructions.add(new InsnNode(RETURN));
    }

    public void IRETURN() {
        methodNode.instructions.add(new InsnNode(IRETURN));
    }

    public void IFNONNULL(LabelNode label) {
        methodNode.instructions.add(new JumpInsnNode(IFNONNULL, label));
    }

    public void ICONST_0() {
        methodNode.instructions.add(new InsnNode(ICONST_0));
    }

    public void ICONST_FALSE() {
        ICONST_0();
    }

    public void ICONST_TRUE() {
        ICONST_1();
    }

    public void ICONST_1() {
        methodNode.instructions.add(new InsnNode(ICONST_1));
    }

    public void IF_ICMPEQ(LabelNode success) {
        methodNode.instructions.add(new JumpInsnNode(IF_ICMPEQ, success));
    }

    public void IFNULL(LabelNode success) {
        methodNode.instructions.add(new JumpInsnNode(IFNULL, success));
    }

    public void IF_ACMPEQ(LabelNode success) {
        methodNode.instructions.add(new JumpInsnNode(IF_ACMPEQ, success));
    }

    public void placeLabelNode(LabelNode labelNode) {
        methodNode.instructions.add(labelNode);
    }

    public void CHECKCAST(Class required) {
        TypeInsnNode in = new TypeInsnNode(CHECKCAST, getDescriptor(required));
        methodNode.instructions.add(in);
    }

    public MethodNode create() {
        return methodNode;
    }
}
