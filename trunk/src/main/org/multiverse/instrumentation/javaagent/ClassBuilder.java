package org.multiverse.instrumentation.javaagent;

import org.multiverse.instrumentation.javaagent.utils.AsmUtils;
import org.objectweb.asm.Opcodes;
import static org.objectweb.asm.Type.getDescriptor;
import static org.objectweb.asm.Type.getInternalName;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

public class ClassBuilder implements Opcodes {

    protected ClassNode classNode = new ClassNode();

    public ClassBuilder() {
        setSuperclass(Object.class);
        this.classNode.access = ACC_PUBLIC;
    }

    public ClassBuilder(Class clazz) {
        classNode = AsmUtils.loadAsClassNode(clazz);
    }

    public String getClassInternalName() {
        return classNode.name;
    }

    public void addPublicFinalField(String name, Class theType) {
        addPublicFinalField(name, getDescriptor(theType));
    }

    public void addPublicField(String name, Class theType) {
        FieldNode field = new FieldNode(
                ACC_PUBLIC,
                name,
                getDescriptor(theType),
                null,
                null);
        classNode.fields.add(field);
    }

    public void addPublicFinalField(String name, String typeDescriptor) {
        FieldNode field = new FieldNode(
                ACC_PUBLIC | ACC_FINAL,
                name,
                typeDescriptor,
                null,
                null);
        classNode.fields.add(field);
    }

    public void addInterface(Class theInterface) {
        classNode.interfaces.add(getInternalName(theInterface));
    }

    public void addMethod(MethodBuilder methodBuilder) {
        classNode.methods.add(methodBuilder.create());
    }

    public void setSuperclass(Class superClass) {
        classNode.superName = getInternalName(superClass);
    }

    public ClassNode create() {
        return classNode;
    }
}
