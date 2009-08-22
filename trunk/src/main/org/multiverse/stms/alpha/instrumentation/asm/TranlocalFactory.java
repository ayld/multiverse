package org.multiverse.stms.alpha.instrumentation.asm;

import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.stms.alpha.*;
import static org.multiverse.stms.alpha.instrumentation.asm.AsmUtils.*;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Type.*;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.RemappingMethodAdapter;
import org.objectweb.asm.tree.*;

import static java.lang.String.format;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * On Factory responsible for creating the {@link Tranlocal} class belongs to an
 * {@link org.multiverse.stms.alpha.AlphaAtomicObject}.
 * <p/>
 * TranlocalClassNodeFactory should not be reused.
 *
 * @author Peter Veentjer
 */
public final class TranlocalFactory implements Opcodes {
    private final ClassNode atomicObject;
    private String tranlocalSnapshotName;
    private String tranlocalName;
    private MetadataService metadataService;

    public TranlocalFactory(ClassNode atomicObject) {
        this.atomicObject = atomicObject;
        this.metadataService = MetadataService.INSTANCE;
    }

    public ClassNode create() {
        if (!metadataService.hasManagedInstanceFields(atomicObject)) {
            return null;
        }

        tranlocalName = metadataService.getTranlocalName(atomicObject);
        tranlocalSnapshotName = metadataService.getTranlocalSnapshotName(atomicObject);

        ClassNode result = new ClassNode();
        result.version = atomicObject.version;
        result.name = tranlocalName;
        result.superName = getInternalName(Tranlocal.class);
        result.access = ACC_PUBLIC + ACC_FINAL + ACC_SYNTHETIC;
        result.sourceFile = atomicObject.sourceFile;
        result.sourceDebug = atomicObject.sourceDebug;

        result.fields.add(createOriginField());
        result.fields.add(createAtomicObjectField());
        result.fields.addAll(remapInstanceFields());
        result.methods.addAll(remapInstanceMethods());
        result.methods.add(createPrivatizeConstrutor());
        result.methods.add(createPrepareForCommitMethod());
        result.methods.add(createGetDirtinessStatusMethod());
        result.methods.add(createGetAtomicObjectMethod());
        result.methods.add(createTakeSnapshotMethod());
        return result;
    }

    private List<FieldNode> remapInstanceFields() {
        List<FieldNode> result = new LinkedList<FieldNode>();
        for (FieldNode managedField : metadataService.getManagedInstanceFields(atomicObject)) {
            FieldNode remappedField = new FieldNode(
                    managedField.access,
                    managedField.name,
                    managedField.desc,
                    managedField.signature,
                    managedField.value);
            result.add(remappedField);
        }

        return result;
    }

    public List<MethodNode> remapInstanceMethods() {
        List<MethodNode> result = new LinkedList<MethodNode>();
        for (MethodNode atomicMethod : metadataService.getAtomicMethods(atomicObject)) {

            //only the instance methods need to be moved that are not private
            if (!isStatic(atomicMethod)) {
                MethodNode remappedMethod = remap(atomicMethod);
                result.add(remappedMethod);
            }
        }
        return result;
    }

    public MethodNode remap(MethodNode originalMethod) {
        if (originalMethod.name.equals("<init>")) {
            return fixConstructor(originalMethod);
        } else {
            return fixInstanceMethod(originalMethod);
        }
    }

    private MethodNode fixConstructor(MethodNode originalConstructor) {
        MethodNode x = fixInstanceMethod(originalConstructor);
        MethodNode result = introduceAtomicObjectVar(x);
        result.access = upgradeToPublic(result.access);
        //the first thing that needs to be done after the super/this constructor
        //has been called, is the assigmnent of the AtomicObject by

        InsnList i = new InsnList();
        i.add(new VarInsnNode(ALOAD, 0));
        i.add(new MethodInsnNode(INVOKESPECIAL, getInternalName(Tranlocal.class), "<init>", "()V"));

        i.add(new VarInsnNode(ALOAD, 0));
        i.add(new VarInsnNode(ALOAD, 1));
        i.add(new FieldInsnNode(PUTFIELD, tranlocalName, "atomicObject", internalFormToDescriptor(atomicObject.name)));

        String attachAsNewDesc = format("(%s)V", getDescriptor(Tranlocal.class));
        i.add(new VarInsnNode(ALOAD, 0));
        i.add(new MethodInsnNode(INVOKESTATIC, getInternalName(AlphaStmUtils.class), "attachAsNew", attachAsNewDesc));


        //since the constructor owner already has been transformed 
        AbstractInsnNode first = findFirstInstructionAfterSuper(atomicObject.superName, result);
        boolean constructorFinished = false;
        for (ListIterator<AbstractInsnNode> it = result.instructions.iterator(); it.hasNext();) {
            AbstractInsnNode node = it.next();
            if (node == first) {
                constructorFinished = true;
            }

            if (constructorFinished) {
                i.add(node);
            }
        }

        result.instructions = i;
        return result;
    }

    private MethodNode introduceAtomicObjectVar(MethodNode originalMethod) {
        String[] exceptions = getExceptions(originalMethod);

        String newMethodDesc = createShiftedMethodDescriptor(originalMethod.desc, atomicObject.name);

        MethodNode newMethod = new MethodNode(
                originalMethod.access,
                originalMethod.name,
                newMethodDesc,
                null,
                exceptions);

        ShiftArgsToTheRightMethodAdapter shiftVisitor = new ShiftArgsToTheRightMethodAdapter(newMethod);
        originalMethod.accept(shiftVisitor);

        //the extra variable should be added after the shift has been done,
        //else it also will be shifted.
        LabelNode start = new LabelNode();
        LabelNode end = new LabelNode();

        newMethod.visitLocalVariable(
                "atomicObject",
                internalFormToDescriptor(atomicObject.name),
                null,
                start.getLabel(),
                end.getLabel(),
                1);
        newMethod.instructions.insertBefore(newMethod.instructions.getFirst(), start);
        newMethod.instructions.add(end);
        return newMethod;
    }

    /**
     * Fixes the instance methods (also constructors). It upgrades the method to
     * public, and for the internals about the instruction fix, see the
     * {@link ManagedFieldRemappingMethodAdapter}.
     *
     * @param originalMethod the instance method to fix.
     * @return a new fixed method.
     */
    private MethodNode fixInstanceMethod(MethodNode originalMethod) {
        //System.out.println("fixing method: " + originalMethod.name + "." + originalMethod.desc);

        MethodNode mappedMethod = new MethodNode(
                originalMethod.access,
                originalMethod.name,
                originalMethod.desc,
                originalMethod.signature,
                getExceptions(originalMethod));

        RemappingMethodAdapter remapVisitor = new ManagedFieldRemappingMethodAdapter(
                mappedMethod, atomicObject, originalMethod);

        originalMethod.accept(remapVisitor);
        //todo: the  readonly stuff moet er nog bij
        //todo: whatch out with constructor.. they don't need readonly
        return mappedMethod;
    }


    class ReadonlyCheckAdviceAdapter extends AdviceAdapter {
        ReadonlyCheckAdviceAdapter(MethodVisitor mv, int access, String name, String desc) {
            super(mv, access, name, desc);
        }

        @Override
        protected void onMethodEnter() {
            mv.visitVarInsn(ALOAD, 0);
            //[this,...
            mv.visitFieldInsn(GETFIELD, tranlocalName, "committed", Type.BOOLEAN_TYPE.getDescriptor());
            //[value,...
            Label notCommitted = new Label();
            mv.visitJumpInsn(IFEQ, notCommitted);
            //[..
            mv.visitTypeInsn(NEW, getInternalName(ReadonlyException.class));
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, getInternalName(ReadonlyException.class), "<init>", "()V");
            mv.visitInsn(ATHROW);
            mv.visitLabel(notCommitted);
        }
    }

    private FieldNode createOriginField() {
        return new FieldNode(
                ACC_PUBLIC + ACC_SYNTHETIC,
                "origin",
                internalFormToDescriptor(tranlocalName), null, null);
    }

    private FieldNode createAtomicObjectField() {
        return new FieldNode(
                ACC_PUBLIC + ACC_SYNTHETIC,
                "atomicObject",
                internalFormToDescriptor(atomicObject.name), null, null);
    }


    private MethodNode createPrivatizeConstrutor() {
        MethodNode m = new MethodNode(
                ACC_PUBLIC + ACC_SYNTHETIC,
                "<init>",
                format("(%s)V", internalFormToDescriptor(tranlocalName)),
                null,
                new String[]{});

        //init
        m.visitVarInsn(ALOAD, 0);
        m.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(Tranlocal.class), "<init>", "()V");

        //placement of the atomicObject
        m.visitVarInsn(ALOAD, 0);
        m.visitVarInsn(ALOAD, 1);
        m.visitFieldInsn(GETFIELD, tranlocalName, "atomicObject", internalFormToDescriptor(atomicObject.name));
        m.visitFieldInsn(PUTFIELD, tranlocalName, "atomicObject", internalFormToDescriptor(atomicObject.name));

        //placement of the original
        m.visitVarInsn(ALOAD, 0);
        m.visitVarInsn(ALOAD, 1);
        m.visitFieldInsn(PUTFIELD, tranlocalName, "origin", internalFormToDescriptor(tranlocalName));

        //placement of the version
        m.visitVarInsn(ALOAD, 0);
        m.visitVarInsn(ALOAD, 1);
        m.visitFieldInsn(GETFIELD, tranlocalName, "version", Type.LONG_TYPE.getDescriptor());
        m.visitFieldInsn(PUTFIELD, tranlocalName, "version", Type.LONG_TYPE.getDescriptor());

        //placement of the rest of the fields.
        for (FieldNode managedField : metadataService.getManagedInstanceFields(atomicObject)) {
            m.visitVarInsn(ALOAD, 0);
            m.visitVarInsn(ALOAD, 1);
            m.visitFieldInsn(GETFIELD, tranlocalName, managedField.name, managedField.desc);
            m.visitFieldInsn(PUTFIELD, tranlocalName, managedField.name, managedField.desc);
        }

        m.visitInsn(RETURN);
        m.visitMaxs(0, 0);//value's don't matter, will be reculculated, but call is needed
        m.visitEnd();
        return m;
    }

    private MethodNode createPrepareForCommitMethod() {
        MethodNode m = new MethodNode(
                ACC_PUBLIC + ACC_SYNTHETIC,
                "prepareForCommit",
                "(J)V",
                null,
                new String[]{});

        m.visitVarInsn(ALOAD, 0);
        m.visitInsn(ICONST_1);
        m.visitFieldInsn(PUTFIELD, tranlocalName, "committed", Type.BOOLEAN_TYPE.getDescriptor());

        m.visitVarInsn(ALOAD, 0);
        m.visitVarInsn(LLOAD, 1);
        m.visitFieldInsn(PUTFIELD, tranlocalName, "version", Type.LONG_TYPE.getDescriptor());

        m.visitVarInsn(ALOAD, 0);
        m.visitInsn(ACONST_NULL);
        m.visitFieldInsn(PUTFIELD, tranlocalName, "origin", internalFormToDescriptor(tranlocalName));
        m.visitInsn(RETURN);
        m.visitMaxs(0, 0);
        m.visitEnd();
        return m;
    }

    private MethodNode createGetAtomicObjectMethod() {
        MethodNode m = new MethodNode(
                ACC_PUBLIC + ACC_SYNTHETIC,
                "getAtomicObject",
                format("()%s", getDescriptor(AlphaAtomicObject.class)),
                null,
                new String[]{});

        //check on committed
        m.visitVarInsn(ALOAD, 0);
        m.visitFieldInsn(GETFIELD, tranlocalName, "atomicObject", internalFormToDescriptor(atomicObject.name));
        m.visitInsn(ARETURN);
        m.visitMaxs(0, 0);//value's don't matter, will be reculculated, but call is needed
        m.visitEnd();
        return m;
    }

    private MethodNode createGetDirtinessStatusMethod() {
        MethodNode m = new MethodNode(
                ACC_PUBLIC + ACC_SYNTHETIC,
                "getDirtinessStatus",
                format("()%s", getDescriptor(DirtinessStatus.class)),
                null,
                new String[]{});

        //check on committed
        m.visitVarInsn(ALOAD, 0);
        m.visitFieldInsn(GETFIELD, tranlocalName, "committed", Type.BOOLEAN_TYPE.getDescriptor());
        Label failure = new Label();
        m.visitJumpInsn(IFEQ, failure);
        m.visitFieldInsn(GETSTATIC, getInternalName(DirtinessStatus.class), "committed", getDescriptor(DirtinessStatus.class));
        m.visitInsn(ARETURN);

        //check on original
        m.visitLabel(failure);
        m.visitVarInsn(ALOAD, 0);
        m.visitFieldInsn(GETFIELD, tranlocalName, "origin", internalFormToDescriptor(tranlocalName));
        failure = new Label();
        m.visitJumpInsn(IFNONNULL, failure);
        m.visitFieldInsn(GETSTATIC, getInternalName(DirtinessStatus.class), "fresh", getDescriptor(DirtinessStatus.class));
        m.visitInsn(ARETURN);

        //check on arguments
        for (FieldNode managedField : metadataService.getManagedInstanceFields(atomicObject)) {
            m.visitLabel(failure);
            m.visitVarInsn(ALOAD, 0);
            m.visitFieldInsn(GETFIELD, tranlocalName, "origin", internalFormToDescriptor(tranlocalName));
            m.visitFieldInsn(GETFIELD, tranlocalName, managedField.name, managedField.desc);
            m.visitVarInsn(ALOAD, 0);
            m.visitFieldInsn(GETFIELD, tranlocalName, managedField.name, managedField.desc);

            failure = new Label();
            switch (getType(managedField.desc).getSort()) {
                case Type.OBJECT:
                    m.visitJumpInsn(IF_ACMPEQ, failure);
                    break;
                case Type.BOOLEAN:
                case Type.BYTE:
                case Type.CHAR:
                case Type.SHORT:
                case Type.INT:
                    m.visitJumpInsn(IF_ICMPEQ, failure);
                    break;
                case Type.FLOAT:
                    m.visitInsn(FCMPL);
                    m.visitJumpInsn(IFEQ, failure);
                    break;
                case Type.LONG:
                    m.visitInsn(LCMP);
                    m.visitJumpInsn(IFEQ, failure);
                    break;
                case Type.DOUBLE:
                    m.visitInsn(DCMPL);
                    m.visitJumpInsn(IFEQ, failure);
                    break;
                case Type.ARRAY:
                    m.visitJumpInsn(IF_ACMPEQ, failure);
                    break;
                default:
                    throw new RuntimeException("Unhandled type: " + managedField.desc);
            }

            m.visitFieldInsn(GETSTATIC, getInternalName(DirtinessStatus.class), "fresh", getDescriptor(DirtinessStatus.class));
            m.visitInsn(ARETURN);
        }

        //this is the last part, where the clean value is returned.
        m.visitLabel(failure);
        m.visitFieldInsn(GETSTATIC, getInternalName(DirtinessStatus.class), "clean", getDescriptor(DirtinessStatus.class));
        m.visitInsn(ARETURN);
        m.visitMaxs(0, 0);//value's don't matter, will be reculculated, but call is needed
        m.visitEnd();
        return m;
    }

    private MethodNode createTakeSnapshotMethod() {
        MethodNode m = new MethodNode(
                ACC_PUBLIC + ACC_SYNTHETIC,
                "takeSnapshot",
                format("()%s", getDescriptor(TranlocalSnapshot.class)),
                null,
                new String[]{});

        m.visitTypeInsn(NEW, tranlocalSnapshotName);
        m.visitInsn(DUP);
        m.visitVarInsn(ALOAD, 0);
        String constructorDesc = format("(%s)V", internalFormToDescriptor(tranlocalName));
        m.visitMethodInsn(INVOKESPECIAL, tranlocalSnapshotName, "<init>", constructorDesc);
        m.visitInsn(ARETURN);
        m.visitMaxs(0, 0);//value's don't matter, will be reculculated, but call is needed
        m.visitEnd();
        return m;
    }
}
