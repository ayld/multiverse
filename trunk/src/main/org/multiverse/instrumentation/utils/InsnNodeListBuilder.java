package org.multiverse.instrumentation.utils;

import static org.multiverse.instrumentation.utils.AsmUtils.internalFormToDescriptor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Type.*;
import org.objectweb.asm.tree.*;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class InsnNodeListBuilder implements Opcodes {

    private final InsnList instructions = new InsnList();

    public InsnNodeListBuilder() {
    }

    public void add(InsnList insnList) {
        instructions.add(insnList);
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

    public void ASTORE(int index) {
        VarInsnNode instr = new VarInsnNode(ASTORE, index);
        instructions.add(instr);
    }

    public void ALOAD(int index) {
        VarInsnNode instr = new VarInsnNode(ALOAD, index);
        instructions.add(instr);
    }

    public void DLOAD(int index) {
        VarInsnNode instr = new VarInsnNode(DLOAD, index);
        instructions.add(instr);
    }

    public void LLOAD(int index) {
        VarInsnNode instr = new VarInsnNode(LLOAD, index);
        instructions.add(instr);
    }

    public void ILOAD(int index) {
        VarInsnNode instr = new VarInsnNode(ILOAD, index);
        instructions.add(instr);
    }

    public void FLOAD(int index) {
        VarInsnNode instr = new VarInsnNode(FLOAD, index);
        instructions.add(instr);
    }

    public void LOAD(Type type, int index) {
        switch (type.getSort()) {
            case Type.VOID:
                throw new RuntimeException("LOAD for type VOID is not possible");
            case Type.BOOLEAN:
            case Type.BYTE:
            case Type.CHAR:
            case Type.SHORT:
            case Type.INT:
                ILOAD(index);
                break;
            case Type.LONG:
                LLOAD(index);
                break;
            case Type.FLOAT:
                FLOAD(index);
                break;
            case Type.DOUBLE:
                DLOAD(index);
                break;
            case Type.ARRAY:
            case Type.OBJECT:
                ALOAD(index);
                break;
            default:
                throw new RuntimeException("Unhandled type for LOAD: " + type);
        }
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

    public void INVOKEVIRTUAL(Method method) {
        MethodInsnNode instr = new MethodInsnNode(
                INVOKEVIRTUAL,
                getInternalName(method.getDeclaringClass()),
                method.getName(),
                getMethodDescriptor(method));
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

    public void INVOKESTATIC(String owner, String name, String descriptor) {
        MethodInsnNode instr = new MethodInsnNode(
                INVOKESTATIC,
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

    public void PUTFIELD(ClassNode owner, String name, Type fieldType) {
        PUTFIELD(owner.name, name, fieldType.getDescriptor());
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

    public void GETFIELD(ClassNode owner, String name, Type fieldType) {
        GETFIELD(owner, name, fieldType.getDescriptor());
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

    public void GETFIELD(ClassNode owner, String name, ClassNode type) {
        GETFIELD(owner, name, internalFormToDescriptor(type.name));
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

    public void RETURN(Type type) {
        switch (type.getSort()) {
            case Type.VOID:
                RETURN();
                break;
            case Type.BOOLEAN:
            case Type.BYTE:
            case Type.CHAR:
            case Type.SHORT:
            case Type.INT:
                IRETURN();
                break;
            case Type.LONG:
                LRETURN();
                break;
            case Type.FLOAT:
                FRETURN();
                break;
            case Type.DOUBLE:
                DRETURN();
                break;
            case Type.ARRAY:
            case Type.OBJECT:
                ARETURN();
                break;
            default:
                throw new RuntimeException("No RETURN found for type: " + type);
        }
    }

    public void RETURN(int returnOpcode) {
        instructions.add(new InsnNode(returnOpcode));
    }

    public void ARETURN() {
        instructions.add(new InsnNode(ARETURN));
    }

    public void DRETURN() {
        instructions.add(new InsnNode(DRETURN));
    }

    public void FRETURN() {
        instructions.add(new InsnNode(FRETURN));
    }

    public void LRETURN() {
        instructions.add(new InsnNode(LRETURN));
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

    public void LCMP() {
        instructions.add(new InsnNode(LCMP));
    }

    public void DCMPL() {
        instructions.add(new InsnNode(DCMPL));
    }

    public void FCMPL() {
        instructions.add(new InsnNode(FCMPL));
    }

    public void IFEQ(LabelNode success) {
        instructions.add(new JumpInsnNode(IFEQ, success));
    }

    public void IFNE(LabelNode success) {
        instructions.add(new JumpInsnNode(IFNE, success));
    }

    public void add(LabelNode labelNode) {
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
