package org.multiverse.instrumentation.utils;

import org.objectweb.asm.Opcodes;
import static org.objectweb.asm.Type.getDescriptor;
import static org.objectweb.asm.Type.getInternalName;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public abstract class ClassNodeBuilder implements Opcodes {

    protected ClassNode classNode = new ClassNode();

    public ClassNodeBuilder() {
        setSuperclass(Object.class);
        this.classNode.superName = getInternalName(Object.class);
        this.classNode.access = ACC_PUBLIC;
    }

    public ClassNodeBuilder(ClassNode classNode) {
        this.classNode = classNode;
    }

    public void addInterface(Class theInterface) {
        classNode.interfaces.add(getInternalName(theInterface));
    }

    public void setAccess(int access) {
        classNode.access = access;
    }

    public String getClassInternalName() {
        return classNode.name;
    }

    public FieldNode addPublicSyntheticField(String name, Class theType) {
        return addPublicSyntheticField(name, getDescriptor(theType));
    }

    public FieldNode addPublicSyntheticField(String name, String descriptor) {
        FieldNode field = new FieldNode(
                ACC_PUBLIC | ACC_SYNTHETIC,
                name,
                descriptor,
                null,
                null);
        classNode.fields.add(field);
        return field;
    }

    public FieldNode addPublicFinalSyntheticField(String name, Class theType) {
        return addPublicFinalSyntheticField(name, getDescriptor(theType));
    }

    public FieldNode addPublicFinalSyntheticField(String name, String typeDescriptor) {
        FieldNode field = new FieldNode(
                ACC_PUBLIC | ACC_FINAL,
                name,
                typeDescriptor,
                null,
                null);
        classNode.fields.add(field);
        return field;
    }

    public void addMethod(MethodNode method) {
        classNode.methods.add(method);
    }

    public void addMethod(MethodBuilder methodBuilder) {
        classNode.methods.add(methodBuilder.createMethod());
    }

    public void setSuperclass(Class superClass) {
        classNode.superName = getInternalName(superClass);
    }

    public ClassNode create() {
        return classNode;
    }
}
