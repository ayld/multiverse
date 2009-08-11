package org.multiverse.stms.alpha.instrumentation.asm;

import org.multiverse.stms.alpha.AlphaStmUtils;
import org.multiverse.stms.alpha.Tranlocal;
import static org.multiverse.stms.alpha.instrumentation.asm.AsmUtils.*;
import org.multiverse.utils.TodoException;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Type.*;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.*;

import static java.lang.String.format;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * An object responsible for enhancing AtomicObjects. It makes sure that an
 * AtomicObject implements the {@link org.multiverse.stms.alpha.AlphaAtomicObject}
 * interface.
 * <p/>
 * It does the following things:
 * <ol>
 * <li>All managed fields are removed (copied to the tranlocal).</li>
 * <li>All methods become atomic.</li>
 * <li>All method content is moved to the tranlocal</li>
 * </ol>
 * <p/>
 * An instance should not be reused.
 *
 * @author Peter Veentjer
 */
public class AtomicObjectTransformer implements Opcodes {

    private final ClassNode atomicObject;
    private final ClassNode mixin;
    private String tranlocalName;
    private MetadataService metadataService;

    public AtomicObjectTransformer(ClassNode atomicObject, ClassNode mixin) {
        this.atomicObject = atomicObject;
        this.mixin = mixin;
        this.metadataService = MetadataService.INSTANCE;
    }

    public ClassNode transform() {
        if (!metadataService.hasManagedInstanceFields(atomicObject)) {
            return null;
        }

        if (metadataService.isRealAtomicObject(atomicObject.superName)) {
            String message = format(
                    "Subclassing an another atomicobject is not allowed. Subclass is %s and the superclass is %s", atomicObject.name, atomicObject.superName);
            throw new TodoException(message);
        }

        tranlocalName = metadataService.getTranlocalName(atomicObject);

        removeManagedFields();

        fixUnmanagedFields();

        //removePrivateAtomicMethods();

        fixMethods();

        mergeMixin();

        addPrivatizeMethod();

        return atomicObject;
    }


    /**
     * All unmanaged fiels are fixed so that the final access modifier is removed and
     * they are made public (so the tranlocals can access them). The final also needs
     * to be removed because the assignment to the final is done in the tranlocal.
     */
    private void fixUnmanagedFields() {
        for (FieldNode field : (List<FieldNode>) atomicObject.fields) {
            if (!metadataService.isManagedInstanceField(atomicObject.name, field.name)) {
                field.access = AsmUtils.upgradeToPublic(field.access);
                if (isFinal(field.access)) {
                    field.access -= ACC_FINAL;
                }
            }
        }
    }

    private void removeManagedFields() {
        atomicObject.fields.removeAll(metadataService.getManagedInstanceFields(atomicObject));
    }

    private void fixMethods() {
        for (MethodNode atomicMethod : (List<MethodNode>) atomicObject.methods) {
            if (metadataService.isAtomicMethod(atomicObject, atomicMethod)) {
                if (isConstructor(atomicMethod)) {
                    fixConstructor(atomicMethod);
                } else if (isStaticConstructor(atomicMethod)) {
                    fixStaticInitializer(atomicMethod);
                } else if (isStatic(atomicMethod)) {
                    fixStaticMethod(atomicMethod);
                } else {
                    fixInstanceMethod(atomicMethod);
                }
            }
        }
    }

    private boolean isStaticConstructor(MethodNode m) {
        return m.name.equals("<clinit>");
    }

    private boolean isConstructor(MethodNode method) {
        return method.name.equals("<init>");
    }

    private void fixInstanceMethod(MethodNode m) {
        m.instructions.clear();
        m.visitVarInsn(ALOAD, 0);

        String argDesc = getDescriptor(Object.class);
        String returnDesc = getDescriptor(Tranlocal.class);
        String loadDesc = format("(%s)%s", argDesc, returnDesc);
        m.visitMethodInsn(
                INVOKESTATIC,
                getInternalName(AlphaStmUtils.class),
                "privatize",
                loadDesc);
        m.visitTypeInsn(CHECKCAST, tranlocalName);

        Type[] argTypes = getArgumentTypes(m.desc);
        int index = 1;
        for (Type argType : argTypes) {
            int loadCode = getLoadOpcode(argType);
            m.visitVarInsn(loadCode, index);
            index += argType.getSize();
        }

        m.visitMethodInsn(INVOKEVIRTUAL, tranlocalName, m.name, m.desc);

        Type returnType = getReturnType(m.desc);
        m.visitInsn(getReturnOpcode(returnType));
        m.visitMaxs(0, 0);
        m.visitEnd();
    }

    private void fixStaticMethod(MethodNode atomicMethod) {
        //      throw new TodoException();
    }

    private void fixStaticInitializer(MethodNode atomicMethod) {
        throw new TodoException();
    }

    /**
     * The constructor is fixed so that the 'this' is placed on the stack,
     * the tranlocal constructor is called with the this, and the original
     * arguments. The original logic is completely moved to the tranlocal.
     *
     * @param m the constructor.
     */
    private void fixConstructor(MethodNode m) {
        //the original code in front of the constructor call can remain.
        int indexOf = AsmUtils.findIndexOfFirstInstructionAfterSuper(atomicObject.superName, m);
        InsnList newConstructorCode = new InsnList();
        for (int k = 0; k < indexOf; k++) {
            newConstructorCode.add(m.instructions.get(k));
        }

        m.instructions.clear();
        m.instructions.add(newConstructorCode);

        m.visitTypeInsn(NEW, tranlocalName);
        //[new

        m.visitVarInsn(ALOAD, 0);
        //[this, new
        //all the constructor values can now be handed over to the constructor of the Tranlocal
        Type[] argTypes = getArgumentTypes(m.desc);
        int index = 1;
        for (Type argType : argTypes) {
            int loadOpcode = getLoadOpcode(argType);
            m.visitVarInsn(loadOpcode, index);
            index += argType.getSize();
        }
        //[args (including this) , new....
        String newMethodDesc = createShiftedMethodDescriptor(m.desc, atomicObject.name);
        m.visitMethodInsn(INVOKESPECIAL, tranlocalName, "<init>", newMethodDesc);
        //[

        //and we are done.
        m.visitMaxs(0, 0);
        m.visitInsn(RETURN);
        m.visitEnd();
    }

    private void mergeMixin() {
        mergeMixinInterfaces();
        mergeMixinFields();
        mergeMixinMethods();
    }

    private void mergeMixinInterfaces() {
        Set<String> interfaces = new HashSet<String>();

        interfaces.addAll(atomicObject.interfaces);
        interfaces.addAll(mixin.interfaces);

        atomicObject.interfaces = new LinkedList<String>(interfaces);
    }

    private void mergeMixinFields() {
        for (FieldNode mixinField : (List<FieldNode>) mixin.fields) {
            atomicObject.fields.add(mixinField);
        }
    }

    private void mergeMixinMethods() {
        Remapper remapper = new SimpleRemapper(mixin.name, atomicObject.name);

        for (MethodNode mixinMethod : (List<MethodNode>) mixin.methods) {
            if (!isConstructor(mixinMethod)) {
                MethodNode remappedMethod = remap(mixinMethod, remapper);
                //todo: synthetic
                atomicObject.methods.add(remappedMethod);
            }
        }
    }

    private void addPrivatizeMethod() {
        String desc = "(J)" + Type.getDescriptor(Tranlocal.class);

        LabelNode start = new LabelNode();
        LabelNode end = new LabelNode();

        InsnList i = new InsnList();
        i.add(start);
        i.add(new VarInsnNode(ALOAD, 0));
        i.add(new VarInsnNode(LLOAD, 1));
        i.add(new MethodInsnNode(INVOKEVIRTUAL, atomicObject.name, "load", desc));
        i.add(new TypeInsnNode(CHECKCAST, tranlocalName));
        i.add(new VarInsnNode(ASTORE, 3));
        i.add(new TypeInsnNode(NEW, tranlocalName));
        i.add(new InsnNode(DUP));
        i.add(new VarInsnNode(ALOAD, 3));
        String constructorDesc = format("(%s)V", internalFormToDescriptor(tranlocalName));
        i.add(new MethodInsnNode(INVOKESPECIAL, tranlocalName, "<init>", constructorDesc));
        i.add(new InsnNode(ARETURN));
        i.add(end);

        MethodNode methodNode = new MethodNode(ACC_PUBLIC + ACC_SYNTHETIC, "privatize", desc, null, new String[]{});
        methodNode.localVariables.add(new LocalVariableNode("x", internalFormToDescriptor(tranlocalName), null, start, end, 3));
        methodNode.instructions = i;
        atomicObject.methods.add(methodNode);
    }
}
