package org.multiverse.instrumentation;

import org.multiverse.api.Handle;
import org.multiverse.api.LazyReference;
import org.multiverse.api.Transaction;
import org.multiverse.instrumentation.utils.AsmUtils;
import static org.multiverse.instrumentation.utils.AsmUtils.*;
import org.multiverse.instrumentation.utils.ClassNodeBuilder;
import org.multiverse.instrumentation.utils.InsnNodeListBuilder;
import org.multiverse.instrumentation.utils.MethodBuilder;
import org.multiverse.multiversionedstm.*;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static java.lang.String.format;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TmEntityClassTransformer extends ClassNodeBuilder {

    private static final String VARNAME_HANDLE = "$handle";

    private static final String VARNAME_NEXTINCHAIN = "$nextInChain";

    private static final String VARNAME_LASTMATERIALIZED = "$lastMaterialized";

    private static final Method READ_LAZY_METHOD =
            getMethod(Transaction.class, "readLazy", Handle.class);

    private static final Method READ_LAZY_AND_SELF_MANAGED_METHOD =
            getMethod(Transaction.class, "readLazyAndSelfManaged", Handle.class);

    private static final Method WALK_MATERIALIZED_MEMBERS_METHOD =
            getMethod(MaterializedObject.class, "walkMaterializedMembers", MemberWalker.class);

    private static final Method ON_MEMBER_METHOD =
            getMethod(MemberWalker.class, "onMember", MaterializedObject.class);

    private static final Method SET_NEXT_IN_CHAIN_METHOD =
            getMethod(MaterializedObject.class, "setNextInChain", MaterializedObject.class);

    private static final Method IS_DIRTY_METHOD =
            getMethod(MaterializedObject.class, "isDirty");

    private static final Constructor OBJECT_CONSTRUCTOR =
            getConstructor(Object.class);

    private static final Method GET_NEXT_IN_CHAIN_METHOD =
            getMethod(MaterializedObject.class, "getNextInChain");

    private static final Method GET_HANDLE_METHOD =
            getMethod(MaterializedObject.class, "getHandle");

    private static final Method DEMATERIALIZE_METHOD =
            getMethod(MaterializedObject.class, "dematerialize");

    private ClassNode materializedClassNode;
    private ClassNode dematerializedClassNode;
    private ClassLoader classLoader;

    public TmEntityClassTransformer(ClassNode materializedClassNode, ClassNode dematerializedClassNode, ClassLoader classLoader) {
        super(materializedClassNode);

        this.dematerializedClassNode = dematerializedClassNode;
        this.classLoader = classLoader;
        this.materializedClassNode = materializedClassNode;

        addInterface(MaterializedObject.class);
        makeFieldsPublicAndNonFinal();
        addHandleField();
        addNextInChainField();
        addLastMaterializedField();
        createAdditionalLazyReferenceFields();
        transformConstructors();
        addMethod(buildRematerializeConstructor());
        addMethod(buildWalkMaterializedMembersMethod());
        addMethod(buildSetNextInChainMethod());
        addMethod(buildGetNextInChainMethod());
        addMethod(buildDematerializedMethod());
        addMethod(buildIsDirtyMethod());
        addMethod(buildGetHandleMethod());
        addDematerializedInnerClass();
    }

    private void addLastMaterializedField() {
        FieldNode field = addPublicSyntheticField(
                VARNAME_LASTMATERIALIZED,
                internalFormToDescriptor(dematerializedClassNode.name));
        exclude(field);
    }

    private void addNextInChainField() {
        FieldNode field = addPublicSyntheticField(VARNAME_NEXTINCHAIN, MaterializedObject.class);
        exclude(field);
    }

    private void addHandleField() {
        FieldNode field = addPublicFinalSyntheticField(VARNAME_HANDLE, MultiversionedHandle.class);
        exclude(field);
    }

    private void createAdditionalLazyReferenceFields() {
        for (FieldNode field : (List<FieldNode>) new ArrayList(materializedClassNode.fields)) {
            if (!isExcluded(field)) {
                if (isTmEntity(field.desc, classLoader)) {
                    FieldNode fieldNode = addPublicSyntheticField(field.name + "$Ref", LazyReference.class);
                    exclude(fieldNode);
                }
            }
        }
    }

    private void makeFieldsPublicAndNonFinal() {
        for (FieldNode field : (List<FieldNode>) materializedClassNode.fields) {
            if (!isExcluded(field)) {
                removeAccess(field, ACC_FINAL);
                removeAccess(field, ACC_PRIVATE);
                removeAccess(field, ACC_PROTECTED);
                removeAccess(field, ACC_PUBLIC);
                field.access += ACC_PUBLIC;
            }
        }
    }

    public static void removeAccess(FieldNode field, int removed) {
        int access = field.access;
        field.access = (access & removed) != 0 ? access - removed : access;
    }

    private void transformConstructors() {
        for (MethodNode methodNode : (List<MethodNode>) materializedClassNode.methods) {
            if (methodNode.name.equals("<init>")) {
                //    if (!doesSelfCall(methodNode)) {
                transformConstructor(methodNode);
                //    }
            }
        }
    }

    private boolean doesSelfCall(MethodNode methodNode) {
        return false;
    }

    private void transformConstructor(MethodNode methodNode) {
        InsnNodeListBuilder codeBuilder = new InsnNodeListBuilder();
        //[..]
        codeBuilder.ALOAD(0);
        //[.., this]
        codeBuilder.NEW(DefaultMultiversionedHandle.class);
        //[.., this, handle]
        codeBuilder.DUP();
        //[.., this, handle, handle]
        codeBuilder.INVOKESPECIAL(DefaultMultiversionedHandle.class, "<init>", "()V");
        //[.., this, handle]
        codeBuilder.PUTFIELD(materializedClassNode, VARNAME_HANDLE, MultiversionedHandle.class);
        //[..]

        MethodInsnNode superInit = getSuperInit(methodNode);
        methodNode.instructions.insert(superInit, codeBuilder.createInstructions());
    }

    private MethodInsnNode getSuperInit(MethodNode methodNode) {
        return (MethodInsnNode) methodNode.instructions.get(indexOfSuperInit(methodNode.instructions));
    }

    public static int indexOfSuperInit(InsnList list) {
        for (int k = 0; k < list.size(); k++) {
            if (list.get(k).getOpcode() == INVOKESPECIAL) {
                return k;
            }
        }

        return -1;
    }

    private void addDematerializedInnerClass() {
        InnerClassNode node = new InnerClassNode(
                dematerializedClassNode.name,//innerclass name
                materializedClassNode.name,//outerclass name
                AsmUtils.getInnerInternalNameOfDematerializedClass(materializedClassNode),
                ACC_STATIC | ACC_PUBLIC);

        classNode.innerClasses.add(node);
    }

    public MethodNode buildRematerializeConstructor() {
        MethodBuilder builder = new MethodBuilder();
        builder.setAccess(ACC_PUBLIC);
        builder.setName("<init>");
        builder.setDescriptor(format("(L%s;L%s;)V", dematerializedClassNode.name, Type.getInternalName(Transaction.class)));

        builder.ALOAD(0);
        builder.INVOKESPECIAL(OBJECT_CONSTRUCTOR);
        builder.ALOAD(0);
        builder.ALOAD(1);
        builder.GETFIELD(dematerializedClassNode, VARNAME_HANDLE, MultiversionedHandle.class);
        builder.PUTFIELD(materializedClassNode, VARNAME_HANDLE, MultiversionedHandle.class);
        builder.ALOAD(0);
        builder.ALOAD(1);
        builder.PUTFIELD(materializedClassNode, VARNAME_LASTMATERIALIZED, dematerializedClassNode);

        for (FieldNode field : (List<FieldNode>) materializedClassNode.fields) {
            if (!isExcluded(field)) {
                if (isTmEntity(field.desc, classLoader)) {
                    builder.ALOAD(0);
                    builder.ALOAD(2);
                    builder.ALOAD(1);
                    builder.GETFIELD(dematerializedClassNode, field.name, MultiversionedHandle.class);

                    if (isNonEscaping(field)) {
                        builder.INVOKEINTERFACE(READ_LAZY_AND_SELF_MANAGED_METHOD);
                    } else {
                        builder.INVOKEINTERFACE(READ_LAZY_METHOD);
                    }

                    builder.CHECKCAST(LazyReference.class);
                    builder.PUTFIELD(materializedClassNode, field.name + "$Ref", LazyReference.class);
                } else {
                    builder.ALOAD(0);
                    builder.ALOAD(1);
                    builder.GETFIELD(dematerializedClassNode, field.name, field.desc);
                    builder.PUTFIELD(materializedClassNode, field.name, field.desc);
                }
            }
        }
        builder.RETURN();

        return builder.createMethod();
    }

    private MethodNode buildSetNextInChainMethod() {
        MethodBuilder builder = new MethodBuilder();
        builder.initWithInterfaceMethod(SET_NEXT_IN_CHAIN_METHOD);

        builder.ALOAD(0);
        builder.ALOAD(1);
        builder.PUTFIELD(materializedClassNode, VARNAME_NEXTINCHAIN, MaterializedObject.class);
        builder.RETURN();

        return builder.createMethod();
    }

    private MethodNode buildGetNextInChainMethod() {
        MethodBuilder builder = new MethodBuilder();
        builder.initWithInterfaceMethod(GET_NEXT_IN_CHAIN_METHOD);

        builder.ALOAD(0);
        builder.GETFIELD(materializedClassNode, VARNAME_NEXTINCHAIN, MaterializedObject.class);
        builder.ARETURN();

        return builder.createMethod();
    }

    private MethodNode buildDematerializedMethod() {
        MethodBuilder builder = new MethodBuilder();
        builder.initWithInterfaceMethod(DEMATERIALIZE_METHOD);

        builder.ALOAD(0);
        builder.NEW(dematerializedClassNode);
        builder.DUP();
        builder.ALOAD(0);
        builder.ACONST_NULL();
        builder.INVOKESPECIAL(dematerializedClassNode, "<init>", format("(L%s;L%s;)V", materializedClassNode.name, Type.getInternalName(Transaction.class)));
        builder.DUP_X1();
        builder.PUTFIELD(materializedClassNode, VARNAME_LASTMATERIALIZED, dematerializedClassNode);
        builder.ARETURN();

        return builder.createMethod();
    }

    private MethodNode buildGetHandleMethod() {
        MethodBuilder builder = new MethodBuilder();
        builder.initWithInterfaceMethod(GET_HANDLE_METHOD);

        builder.ALOAD(0);
        builder.GETFIELD(materializedClassNode, VARNAME_HANDLE, MultiversionedHandle.class);
        builder.ARETURN();

        return builder.createMethod();
    }


    private MethodNode buildWalkMaterializedMembersMethod() {
        MethodBuilder builder = new MethodBuilder();
        builder.initWithInterfaceMethod(WALK_MATERIALIZED_MEMBERS_METHOD);

        LabelNode next = new LabelNode();
        for (FieldNode field : (List<FieldNode>) materializedClassNode.fields) {
            if (!isExcluded(field)) {
                if (isTmEntity(field.desc, classLoader)) {
                    builder.ALOAD(0);
                    builder.GETFIELD(materializedClassNode, field);
                    builder.IFNULL(next);
                    builder.ALOAD(1);
                    builder.ALOAD(0);
                    builder.GETFIELD(materializedClassNode, field);
                    builder.CHECKCAST(MaterializedObject.class);
                    builder.INVOKEINTERFACE(ON_MEMBER_METHOD);
                    builder.add(next);
                    next = new LabelNode();
                }
            }
        }
        builder.RETURN();

        return builder.createMethod();
    }

    private MethodNode buildIsDirtyMethod() {
        MethodBuilder builder = new MethodBuilder();
        builder.initWithInterfaceMethod(IS_DIRTY_METHOD);

        String dematerializedDesc = internalFormToDescriptor(dematerializedClassNode.name);

        //adds the code for the check if the materializedClassNode has been set.
        //if this is not set, the object is dirty by default.
        builder.ALOAD(0);
        builder.GETFIELD(materializedClassNode, VARNAME_LASTMATERIALIZED, dematerializedDesc);
        LabelNode nonNullLastMaterialized = new LabelNode();
        builder.IFNONNULL(nonNullLastMaterialized);
        builder.ICONST_TRUE();
        builder.IRETURN();

        builder.add(nonNullLastMaterialized);

        for (FieldNode field : (List<FieldNode>) materializedClassNode.fields) {
            if (!isExcluded(field)) {
                Type type = Type.getType(field.desc);
                LabelNode equalsLabel = new LabelNode();

                if (isTmEntity(type.getDescriptor(), classLoader)) {

                    //load the value of the dematerialized object field on the stack.
                    builder.ALOAD(0);
                    builder.GETFIELD(materializedClassNode, VARNAME_LASTMATERIALIZED, dematerializedDesc);
                    builder.GETFIELD(dematerializedClassNode, field.name, MultiversionedHandle.class);
                    //[..., dematerialized-handle]

                    //load the value of the materialized object field on the stack.
                    builder.ALOAD(0);
                    builder.GETFIELD(materializedClassNode, field.name + "$Ref", LazyReference.class);
                    builder.ALOAD(0);
                    builder.GETFIELD(materializedClassNode, field.name, type);
                    //[..., dematerialized-handle, field-ref, field]

                    Method getHandleMethod = getMethod(
                            MultiversionedStmUtils.class,
                            "getHandle",
                            LazyReference.class, Object.class
                    );
                    builder.INVOKESTATIC(getHandleMethod);
                    //[... dematerialized-handle, handle]
                    //do a comparison on those fields.. if they are the same, continue to the
                    //next field. If they are not the same, the function can exit with a true.
                    builder.IF_ACMPEQ(equalsLabel);
                    //[..]
                    builder.ICONST_TRUE();
                    builder.IRETURN();
                } else {
                    builder.ALOAD(0);
                    builder.GETFIELD(materializedClassNode, VARNAME_LASTMATERIALIZED, dematerializedDesc);
                    builder.GETFIELD(dematerializedClassNode, field);

                    builder.ALOAD(0);
                    builder.GETFIELD(materializedClassNode, field);

                    switch (type.getSort()) {
                        case Type.OBJECT:
                            builder.IF_ACMPEQ(equalsLabel);
                            break;
                        case Type.BOOLEAN:
                        case Type.BYTE:
                        case Type.CHAR:
                        case Type.SHORT:
                        case Type.INT:
                            builder.IF_ICMPEQ(equalsLabel);
                            break;
                        case Type.FLOAT:
                            builder.FCMPL();
                            builder.IFEQ(equalsLabel);
                            break;
                        case Type.LONG:
                            builder.LCMP();
                            builder.IFEQ(equalsLabel);
                            break;
                        case Type.DOUBLE:
                            builder.DCMPL();
                            builder.IFEQ(equalsLabel);
                            break;
                        default:
                            throw new RuntimeException("Unhandled type: " + type);
                    }

                    builder.ICONST_TRUE();
                    builder.IRETURN();
                }
                builder.add(equalsLabel);
            }
        }
        builder.ICONST_FALSE();
        builder.IRETURN();

        return builder.createMethod();
    }

}
