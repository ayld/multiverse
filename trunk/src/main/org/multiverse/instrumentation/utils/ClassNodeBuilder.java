package org.multiverse.instrumentation.utils;

import org.objectweb.asm.Opcodes;
import static org.objectweb.asm.Type.getDescriptor;
import static org.objectweb.asm.Type.getInternalName;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

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

    public void addPublicSyntheticField(String name, Class theType) {
        addPublicSyntheticField(name, getDescriptor(theType));
    }

    public void addPublicSyntheticField(String name, String descriptor) {
        FieldNode field = new FieldNode(
                ACC_PUBLIC | ACC_SYNTHETIC,
                name,
                descriptor,
                null,
                null);
        classNode.fields.add(field);
    }

    public void addPublicFinalSyntheticField(String name, Class theType) {
        addPublicFinalSyntheticField(name, getDescriptor(theType));
    }

    public void addPublicFinalSyntheticField(String name, String typeDescriptor) {
        FieldNode field = new FieldNode(
                ACC_PUBLIC | ACC_FINAL,
                name,
                typeDescriptor,
                null,
                null);
        classNode.fields.add(field);
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
