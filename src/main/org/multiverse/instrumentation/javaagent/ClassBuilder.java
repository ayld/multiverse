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
        addPublicFinalSynthethicField(name, getDescriptor(theType));
    }

    public void addPublicFinalSynthethicField(String name, String typeDescriptor) {
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
