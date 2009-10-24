package org.multiverse.stms.alpha.instrumentation.asm;

import org.multiverse.api.exceptions.LoadUncommittedException;
import org.multiverse.stms.alpha.AlphaAtomicObject;
import org.multiverse.stms.alpha.AlphaStmUtils;
import org.multiverse.stms.alpha.AlphaTranlocal;
import static org.multiverse.stms.alpha.instrumentation.asm.AsmUtils.*;
import org.multiverse.utils.TodoException;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Type.getDescriptor;
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
 * <p/>
 * The constructor of the donor is not copied. So what out with relying on a constructor
 * in the donor.
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

            boolean isAtomicMethod = metadataService.isAtomicMethod(atomicObject, originalMethod);
            boolean isConstructor = isConstructor(originalMethod);
            if (isAtomicMethod && isConstructor) {
                fixedMethod = fixConstructor(originalMethod);
            }

            methods.add(fixedMethod);
        }

        atomicObject.methods = methods;
    }

    private boolean isConstructor(MethodNode method) {
        return method.name.equals("<init>");
    }

    /**
     * The constructor is fixed so that the 'this' is placed on the stack,
     * the tranlocal constructor is called with the this, and the original
     * arguments. The original logic is completely moved to the tranlocal.
     *
     * @param m the constructor.
     * @return a newly created constructor that contains the fix.
     */
    private MethodNode fixConstructor(MethodNode m) {
        //operandstack.push isAttached(this)
        InsnList newCode = new InsnList();
        newCode.add(new VarInsnNode(ALOAD, 0));
        String desc = Type.getMethodDescriptor(Type.BOOLEAN_TYPE, new Type[]{Type.getType(AlphaAtomicObject.class)});
        String owner = Type.getInternalName(AlphaStmUtils.class);
        newCode.add(new MethodInsnNode(INVOKESTATIC, owner, "isAttached", desc));

        LabelNode alreadyConstructedLabel = new LabelNode();
        //1 is true, 0 is false, als het goed is staat nu op de stack een 0 om aan te geven dat er nog geen tranlocal is
        //ifne die branched naar de already
        //the actual creation
        newCode.add(new JumpInsnNode(IFNE, alreadyConstructedLabel));
        newCode.add(new TypeInsnNode(NEW, tranlocalName));
        newCode.add(new InsnNode(DUP));

        newCode.add(new VarInsnNode(ALOAD, 0));
        Type atomicObjectType = Type.getObjectType(atomicObject.name);
        String initialConstructorDesc = Type.getMethodDescriptor(Type.VOID_TYPE, new Type[]{atomicObjectType});
        newCode.add(new MethodInsnNode(INVOKESPECIAL, tranlocalName, "<init>", initialConstructorDesc));

        //attach this newly created tranlocal to the transaction.
        String attachAsNewDesc = format("(%s)V", getDescriptor(AlphaTranlocal.class));
        newCode.add(new MethodInsnNode(INVOKESTATIC, getInternalName(AlphaStmUtils.class), "attachAsNew", attachAsNewDesc));

        //this is where the branches join
        newCode.add(alreadyConstructedLabel);

        //inject the newCode in front of the other code that follows the construction
        AbstractInsnNode firstInstructionAfterSuper = findFirstInstructionAfterSuper(atomicObject.superName, atomicObject.name, m);
        m.instructions.insertBefore(firstInstructionAfterSuper, newCode);
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
        m.visitVarInsn(ALOAD, 0);
        String getLoadUncommittedMessageSig = Type.getMethodDescriptor(
                Type.getType(String.class),
                new Type[]{Type.getType(AlphaAtomicObject.class)});
        m.visitMethodInsn(
                INVOKESTATIC,
                getInternalName(AlphaStmUtils.class),
                "getLoadUncommittedMessage",
                getLoadUncommittedMessageSig);
        m.visitMethodInsn(INVOKESPECIAL, getInternalName(LoadUncommittedException.class), "<init>", "(Ljava/lang/String;)V");
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
