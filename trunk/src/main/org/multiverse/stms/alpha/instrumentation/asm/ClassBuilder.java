package org.multiverse.stms.alpha.instrumentation.asm;

import static org.multiverse.stms.alpha.instrumentation.asm.AsmUtils.internalFormToDescriptor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Type.getInternalName;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

/**
 * Todo:
 * only is used in the
 *
 * @author Peter Veentjer.
 */
@Deprecated
public final class ClassBuilder implements Opcodes {

    private ClassNode classNode = new ClassNode();

    public ClassBuilder() {
    }

    public ClassBuilder(ClassNode base) {
        this.classNode = base;
    }

    public ClassNode build() {
        return classNode;
    }

    public void addInterface(Class theInterface) {
        classNode.interfaces.add(Type.getInternalName(theInterface));
    }

    public void setName(String name) {
        classNode.name = name;
    }

    public FieldNode addPublicFinalSyntheticField(String name, Type theType) {
        return addPublicFinalSyntheticField(name, theType.getDescriptor());
    }

    public FieldNode addPublicFinalSyntheticField(String name, ClassNode type) {
        return addPublicFinalSyntheticField(name, internalFormToDescriptor(type.name));
    }

    public FieldNode addPublicFinalSyntheticField(String name, String typeDescriptor) {
        FieldNode field = new FieldNode(
                ACC_PUBLIC + ACC_FINAL,
                name,
                typeDescriptor,
                null,
                null);
        classNode.fields.add(field);
        return field;
    }

    public FieldNode addSyntheticPrimitiveLongField(String name, long value) {
        FieldNode field = new FieldNode(
                ACC_PUBLIC + ACC_SYNTHETIC,
                name,
                Type.LONG_TYPE.getDescriptor(),
                null,
                null);
        classNode.fields.add(field);
        return field;
    }

    public FieldNode addSyntheticVolatileField(String name, Class type) {
        FieldNode field = new FieldNode(
                ACC_PUBLIC + ACC_SYNTHETIC | ACC_VOLATILE,
                name,
                Type.getDescriptor(type),
                null,
                null);
        classNode.fields.add(field);
        return field;
    }

    public FieldNode addSyntheticPrimitiveBooleanField(String name) {
        FieldNode field = new FieldNode(
                ACC_PUBLIC + ACC_SYNTHETIC,
                name,
                Type.BOOLEAN_TYPE.getDescriptor(),
                null,
                null);
        classNode.fields.add(field);
        return field;
    }

    public FieldNode addSyntheticField(String name, String descriptor) {
        FieldNode field = new FieldNode(
                ACC_PUBLIC + ACC_SYNTHETIC,
                name,
                descriptor,
                null,
                null);
        classNode.fields.add(field);
        return field;
    }

    public void addField(FieldNode field) {
        classNode.fields.add(field);
    }

    public void addAllFields(List<FieldNode> fields) {
        classNode.fields.addAll(fields);
    }

    public void addMethod(MethodNode methodNode) {
        classNode.methods.add(methodNode);
    }

    public void setVersion(int version) {
        classNode.version = version;
    }

    public void setAccess(int access) {
        classNode.access = access;
    }

    public void setSuperclass(Class superClass) {
        classNode.superName = getInternalName(superClass);
    }
}
