package org.multiverse.instrumentation.javaagent;

import static org.multiverse.instrumentation.javaagent.InstrumentationUtils.getConstructor;
import org.objectweb.asm.Opcodes;
import static org.objectweb.asm.Type.*;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedList;

public abstract class MethodBuilder implements Opcodes {

    protected MethodNode methodNode = new MethodNode();

    public MethodBuilder() {
        setAccess(ACC_PUBLIC);
        methodNode.exceptions = new LinkedList();
        methodNode.tryCatchBlocks = new LinkedList();
    }

    public void setName(String name) {
        methodNode.name = name;
    }

    public void setAccess(int access) {
        methodNode.access = access;
    }

    public void setDescription(String desc) {
        methodNode.desc = desc;
    }

    public void initWithConstructor(Constructor constructor) {
        methodNode.access = constructor.getModifiers();
        methodNode.name = constructor.getName();
        methodNode.desc = getConstructorDescriptor(constructor);
    }

    public void initWithInterfaceMethod(Method method) {
        methodNode.access = method.getModifiers();
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

    public void NEW(Class clazz) {
        NEW(getInternalName(clazz));
    }

    public void NEW(String desc) {
        TypeInsnNode ins = new TypeInsnNode(
                NEW,
                desc);
        methodNode.instructions.add(ins);
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
                getInternalName(method.getClass()),
                method.getName(),
                getMethodDescriptor(method));
        methodNode.instructions.add(instr);
    }

    public void INVOKESTATIC(Method method) {
        MethodInsnNode instr = new MethodInsnNode(
                INVOKESTATIC,
                getInternalName(method.getClass()),
                method.getName(),
                getMethodDescriptor(method));
        methodNode.instructions.add(instr);
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

    public void GETFIELD(Class owner, String name, Class fieldType) {
        GETFIELD(getInternalName(owner), name, fieldType);
    }

    public void GETFIELD(String owner, String name, Class fieldType) {
        FieldInsnNode instr = new FieldInsnNode(
                GETFIELD,
                owner,
                name,
                getDescriptor(fieldType)
        );
        methodNode.instructions.add(instr);
    }


    public void DUP() {
        methodNode.instructions.add(new InsnNode(DUP));
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


    public MethodNode create() {
        return methodNode;
    }
}
