package org.multiverse.stms.alpha.instrumentation.asm;

import org.multiverse.api.exceptions.LoadUncommittedException;
import org.multiverse.stms.alpha.AlphaTranlocal;
import static org.multiverse.stms.alpha.instrumentation.asm.AsmUtils.*;
import org.multiverse.utils.TodoException;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Type.getInternalName;
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
 * <li>All instance methods become atomic.</li>
 * <li>All method content is moved to the tranlocal version of the method</li>
 * </ol>
 * <p/>
 * An instance should not be reused.
 *
 * @author Peter Veentjer
 */
public class AtomicObjectTransformer implements Opcodes {

    private final ClassNode atomicObject;
    private final ClassNode mixin;
    private final MetadataRepository metadataService;
    private final String tranlocalName;

    public AtomicObjectTransformer(ClassNode atomicObject, ClassNode mixin) {
        this.atomicObject = atomicObject;
        this.mixin = mixin;
        this.metadataService = MetadataRepository.INSTANCE;
        this.tranlocalName = metadataService.getTranlocalName(atomicObject);
    }

    public ClassNode transform() {
        if (!metadataService.hasManagedInstanceFields(atomicObject)) {
            return null;
        }

        if (metadataService.isRealAtomicObject(atomicObject.superName)) {
            String message = format(
                    "Subclassing an atomicobject is not allowed. Subclass is %s and the superclass is %s",
                    atomicObject.name, atomicObject.superName);
            throw new TodoException(message);
        }

        removeManagedFields();

        fixUnmanagedFields();

        fixMethods();

        mergeMixin();

        atomicObject.methods.add(createPrivatizeMethod());

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
                field.access = upgradeToPublic(field.access);
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
        List<MethodNode> methods = new LinkedList<MethodNode>();

        for (MethodNode originalMethod : (List<MethodNode>) atomicObject.methods) {
            MethodNode fixedMethod = originalMethod;

            if (metadataService.isAtomicMethod(atomicObject, originalMethod)) {
                if (isConstructor(originalMethod)) {
                    fixedMethod = fixConstructor(originalMethod);
                } else if (isStaticInitializer(originalMethod)) {
                    fixedMethod = fixStaticInitializer(originalMethod);
                } else if (isStatic(originalMethod)) {
                    fixedMethod = fixStaticMethod(originalMethod);
                } else {
                    fixedMethod = fixInstanceMethod(originalMethod);
                }
            }

            methods.add(fixedMethod);
        }

        atomicObject.methods = methods;
    }

    private boolean isStaticInitializer(MethodNode m) {
        return m.name.equals("<clinit>");
    }

    private boolean isConstructor(MethodNode method) {
        return method.name.equals("<init>");
    }

    /**
     * To fix the instance method, the call must be forwarded to the 'tranlocal' method.
     * So if there is a method employee.raiseSalary(1)
     * The call is forwarded to employee.raiseSalary(employeeTranlocal, 1).
     *
     * @param m
     */
    private MethodNode fixInstanceMethod(MethodNode m) {
        MethodNode enhancedMethod = new MethodNode();
        enhancedMethod.access = m.access;
        enhancedMethod.localVariables = new LinkedList();
        enhancedMethod.name = m.name;
        enhancedMethod.desc = m.desc;
        enhancedMethod.exceptions = m.exceptions;
        enhancedMethod.tryCatchBlocks = m.tryCatchBlocks;

        m.accept(new AtomicObjectRemappingMethodAdapter(enhancedMethod, atomicObject, m));

        return enhancedMethod;
    }

    private MethodNode fixStaticMethod(MethodNode atomicMethod) {
        return atomicMethod;
    }

    private MethodNode fixStaticInitializer(MethodNode atomicMethod) {
        throw new TodoException();
    }

    /**
     * The constructor is fixed so that the 'this' is placed on the stack,
     * the tranlocal constructor is called with the this, and the original
     * arguments. The original logic is completely moved to the tranlocal.
     *
     * @param originalConstructor the constructor.
     */
    private MethodNode fixConstructor(MethodNode originalConstructor) {
        MethodNode m = fixInstanceMethod(originalConstructor);

        //inject extra code for tranlocal creation
        InsnList addedInstructions = new InsnList();
        addedInstructions.add(new TypeInsnNode(NEW, tranlocalName));
        addedInstructions.add(new VarInsnNode(ALOAD, 0));
        Type atomicObjectType = Type.getObjectType(atomicObject.name);
        String initialConstructorDesc = Type.getMethodDescriptor(Type.VOID_TYPE, new Type[]{atomicObjectType});

        addedInstructions.add(new MethodInsnNode(INVOKESPECIAL, tranlocalName, "<init>", initialConstructorDesc));
        //the original code in front of the constructor call can remain.
        AbstractInsnNode firstInstructionAfterSuper = findFirstInstructionAfterSuper(atomicObject.superName, m);

        m.instructions.insertBefore(firstInstructionAfterSuper, addedInstructions);
        return m;
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

    private MethodNode createPrivatizeMethod() {
        String desc = "(J)" + Type.getDescriptor(AlphaTranlocal.class);

        MethodNode m = new MethodNode(ACC_PUBLIC + ACC_SYNTHETIC, "privatize", desc, null, new String[]{});

        LabelNode start = new LabelNode();
        LabelNode end = new LabelNode();

        m.localVariables.add(new LocalVariableNode("x", internalFormToDescriptor(tranlocalName), null, start, end, 3));

        m.visitLabel(start.getLabel());
        m.visitVarInsn(ALOAD, 0);
        m.visitVarInsn(LLOAD, 1);
        m.visitMethodInsn(INVOKEVIRTUAL, atomicObject.name, "load", desc);

        //a null check to make sure that a not null value is retrieved.
        m.visitInsn(DUP);
        Label notNull = new Label();
        m.visitJumpInsn(IFNONNULL, notNull);
        m.visitTypeInsn(NEW, getInternalName(LoadUncommittedException.class));
        m.visitInsn(DUP);
        m.visitMethodInsn(INVOKESPECIAL, getInternalName(LoadUncommittedException.class), "<init>", "()V");
        m.visitInsn(ATHROW);

        m.visitLabel(notNull);
        m.visitTypeInsn(CHECKCAST, tranlocalName);
        m.visitVarInsn(ASTORE, 3);
        m.visitTypeInsn(NEW, tranlocalName);
        m.visitInsn(DUP);
        m.visitVarInsn(ALOAD, 3);
        String constructorDesc = format("(%s)V", internalFormToDescriptor(tranlocalName));
        m.visitMethodInsn(INVOKESPECIAL, tranlocalName, "<init>", constructorDesc);
        m.visitInsn(ARETURN);
        m.visitLabel(end.getLabel());

        return m;
    }
}
