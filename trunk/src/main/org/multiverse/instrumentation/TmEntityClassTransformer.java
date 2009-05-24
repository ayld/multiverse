package org.multiverse.instrumentation;

import org.multiverse.api.Handle;
import org.multiverse.api.LazyReference;
import org.multiverse.api.Transaction;
import org.multiverse.instrumentation.utils.AsmUtils;
import static org.multiverse.instrumentation.utils.AsmUtils.*;
import org.multiverse.instrumentation.utils.ClassNodeBuilder;
import org.multiverse.instrumentation.utils.InstructionsBuilder;
import org.multiverse.instrumentation.utils.MethodBuilder;
import org.multiverse.multiversionedstm.DefaultMultiversionedHandle;
import org.multiverse.multiversionedstm.MaterializedObject;
import org.multiverse.multiversionedstm.MemberWalker;
import org.multiverse.multiversionedstm.MultiversionedHandle;
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
        addMethod(new RematerializeConstructorBuilder());
        addMethod(new WalkMaterializedMembersMethodBuilder());
        addMethod(new SetNextInChainMethodBuilder());
        addMethod(new GetNextInChainMethodBuilder());
        addMethod(new DematerializeMethodBuilder());
        addMethod(new IsDirtyMethodBuilder());
        addMethod(new GetHandleMethodBuilder());
        addDematerializedInnerClass();
    }

    private void addLastMaterializedField() {
        FieldNode field = addPublicSyntheticField(VARNAME_LASTMATERIALIZED, internalFormToDescriptor(dematerializedClassNode.name));
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

    private void removeAccess(FieldNode field, int removed) {
        int access = field.access;
        field.access = (access & removed) != 0 ? access - removed : access;
    }

    private void transformConstructors() {
        for (MethodNode methodNode : (List<MethodNode>) materializedClassNode.methods) {
            if (methodNode.name.equals("<init>")) {
                transformConstructor(methodNode);
            }
        }
    }

    private void transformConstructor(MethodNode methodNode) {
        InstructionsBuilder codeBuilder = new InstructionsBuilder();
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

    private class RematerializeConstructorBuilder extends MethodBuilder {
        RematerializeConstructorBuilder() {
            methodNode.access = ACC_PUBLIC;
            methodNode.name = "<init>";
            methodNode.desc = format("(L%s;L%s;)V", dematerializedClassNode.name, Type.getInternalName(Transaction.class));

            ALOAD(0);
            INVOKESPECIAL(OBJECT_CONSTRUCTOR);
            ALOAD(0);
            ALOAD(1);
            GETFIELD(dematerializedClassNode, VARNAME_HANDLE, MultiversionedHandle.class);
            PUTFIELD(materializedClassNode, VARNAME_HANDLE, MultiversionedHandle.class);
            ALOAD(0);
            ALOAD(1);
            PUTFIELD(materializedClassNode, VARNAME_LASTMATERIALIZED, dematerializedClassNode);

            for (FieldNode field : (List<FieldNode>) materializedClassNode.fields) {
                if (!isExcluded(field)) {
                    if (isTmEntity(field.desc, classLoader)) {
                        ALOAD(0);
                        ALOAD(2);
                        ALOAD(1);
                        GETFIELD(dematerializedClassNode, field.name, MultiversionedHandle.class);

                        if (isSelfManaged(field)) {
                            INVOKEINTERFACE(READ_LAZY_AND_SELF_MANAGED_METHOD);
                        } else {
                            INVOKEINTERFACE(READ_LAZY_METHOD);
                        }

                        CHECKCAST(LazyReference.class);
                        PUTFIELD(materializedClassNode, field.name + "$Ref", LazyReference.class);
                    } else {
                        ALOAD(0);
                        ALOAD(1);
                        GETFIELD(dematerializedClassNode, field.name, field.desc);
                        PUTFIELD(materializedClassNode, field.name, field.desc);
                    }
                }
            }
            RETURN();
        }
    }

    private class SetNextInChainMethodBuilder extends MethodBuilder {
        SetNextInChainMethodBuilder() {
            initWithInterfaceMethod(SET_NEXT_IN_CHAIN_METHOD);

            ALOAD(0);
            ALOAD(1);
            PUTFIELD(materializedClassNode, VARNAME_NEXTINCHAIN, MaterializedObject.class);
            RETURN();
        }
    }

    private class GetNextInChainMethodBuilder extends MethodBuilder {
        GetNextInChainMethodBuilder() {
            initWithInterfaceMethod(GET_NEXT_IN_CHAIN_METHOD);

            ALOAD(0);
            GETFIELD(materializedClassNode, VARNAME_NEXTINCHAIN, MaterializedObject.class);
            ARETURN();
        }
    }

    private class DematerializeMethodBuilder extends MethodBuilder {
        DematerializeMethodBuilder() {
            initWithInterfaceMethod(DEMATERIALIZE_METHOD);

            ALOAD(0);
            NEW(dematerializedClassNode);
            DUP();
            ALOAD(0);
            ACONST_NULL();
            INVOKESPECIAL(dematerializedClassNode, "<init>", format("(L%s;L%s;)V", materializedClassNode.name, Type.getInternalName(Transaction.class)));
            DUP_X1();
            PUTFIELD(materializedClassNode, VARNAME_LASTMATERIALIZED, dematerializedClassNode);
            ARETURN();
        }
    }

    private class GetHandleMethodBuilder extends MethodBuilder {

        GetHandleMethodBuilder() {
            initWithInterfaceMethod(GET_HANDLE_METHOD);

            ALOAD(0);
            GETFIELD(materializedClassNode, VARNAME_HANDLE, MultiversionedHandle.class);
            ARETURN();
        }
    }

    private class IsDirtyMethodBuilder extends MethodBuilder {

        IsDirtyMethodBuilder() {
            initWithInterfaceMethod(IS_DIRTY_METHOD);

            String dematerializedDesc = internalFormToDescriptor(dematerializedClassNode.name);

            ICONST_TRUE();
            IRETURN();

            /*
            ALOAD(0);

            GETFIELD(materializedClassNode, VARNAME_LASTMATERIALIZED, dematerializedDesc);
            LabelNode nonNullLastMaterialized = new LabelNode();
            IFNONNULL(nonNullLastMaterialized);
            ICONST_TRUE();
            IRETURN();

            
            placeLabelNode(nonNullLastMaterialized);

            for (FieldNode field : (List<FieldNode>) materializedClassNode.fields) {
                if (!isSynthetic(field) && !isStatic(field)) {

                    ALOAD(0);
                    LabelNode equalsLabel = new LabelNode();
                    GETFIELD(materializedClassNode, VARNAME_LASTMATERIALIZED, dematerializedDesc);
                    GETFIELD(dematerializedClassNode, field.name, dematerializedDesc);
                    ALOAD(0);
                    GETFIELD(materializedClassNode, field.name, field.desc);

                    String type = field.desc;
                    if (isPrimitive(type)) {
                        IF_ICMPEQ(equalsLabel);
                    } else if (isTmEntity(type, classLoader)) {
                        IF_ICMPEQ(equalsLabel);//todo
                    } else {
                        IF_ACMPEQ(equalsLabel);
                    }

                    ICONST_TRUE();
                    IRETURN();
                    placeLabelNode(equalsLabel);
                }
            }

            ICONST_FALSE();
            IRETURN();*/
        }

        private boolean isPrimitive(String type) {
            return false;  //To change body of created methods use File | Settings | File Templates.
        }
    }

    private class WalkMaterializedMembersMethodBuilder extends MethodBuilder {

        WalkMaterializedMembersMethodBuilder() {
            initWithInterfaceMethod(WALK_MATERIALIZED_MEMBERS_METHOD);

            LabelNode next = new LabelNode();
            for (FieldNode field : (List<FieldNode>) materializedClassNode.fields) {
                if (!isExcluded(field)) {
                    if (isTmEntity(field.desc, classLoader)) {
                        ALOAD(0);
                        GETFIELD(materializedClassNode, field);
                        IFNULL(next);
                        ALOAD(1);
                        ALOAD(0);
                        GETFIELD(materializedClassNode, field);
                        CHECKCAST(MaterializedObject.class);
                        INVOKEINTERFACE(ON_MEMBER_METHOD);
                        placeLabelNode(next);
                        next = new LabelNode();
                    }
                }
            }

            RETURN();
        }
    }
}
