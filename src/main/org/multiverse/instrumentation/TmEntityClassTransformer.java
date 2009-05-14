package org.multiverse.instrumentation;

import org.multiverse.api.Handle;
import org.multiverse.api.LazyReference;
import org.multiverse.api.Transaction;
import org.multiverse.instrumentation.utils.AsmUtils;
import static org.multiverse.instrumentation.utils.AsmUtils.isSynthetic;
import static org.multiverse.instrumentation.utils.AsmUtils.isTmEntity;
import org.multiverse.instrumentation.utils.ClassBuilder;
import org.multiverse.instrumentation.utils.InstructionsBuilder;
import org.multiverse.instrumentation.utils.MethodBuilder;
import org.multiverse.multiversionedstm.DefaultMultiversionedHandle;
import org.multiverse.multiversionedstm.MaterializedObject;
import org.multiverse.multiversionedstm.MemberWalker;
import org.multiverse.multiversionedstm.MultiversionedHandle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static java.lang.String.format;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TmEntityClassTransformer extends ClassBuilder {

    private static final String VARNAME_HANDLE = "handle";
    private static final String VARNAME_NEXTINCHAIN = "nextInChain";
    private static final String VARNAME_LASTMATERIALIZED = "lastMaterialized";

    private ClassNode materializedClassNode;

    private ClassLoader classLoader;
    private ClassNode dematerializedClassNode;

    public TmEntityClassTransformer(ClassNode materializedClassNode, ClassNode dematerialized, ClassLoader classLoader) {
        super(materializedClassNode);

        this.dematerializedClassNode = dematerialized;
        this.classLoader = classLoader;
        this.materializedClassNode = materializedClassNode;

        addInterface(MaterializedObject.class);

        addPublicFinalSyntheticField(VARNAME_HANDLE, MultiversionedHandle.class);
        addPublicSyntheticField(VARNAME_NEXTINCHAIN, MaterializedObject.class);
        addPublicSyntheticField(VARNAME_LASTMATERIALIZED, AsmUtils.internalFormToDescriptor(dematerialized.name));

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

    private void createAdditionalLazyReferenceFields() {
        for (FieldNode field : (List<FieldNode>) new ArrayList(materializedClassNode.fields)) {
            if (!isSynthetic(field)) {
                if (isTmEntity(field.desc, classLoader)) {
                    addPublicSyntheticField(field.name + "Ref", LazyReference.class);
                }
            }
        }
    }

    /**
     * Fouten:
     * <p/>
     * -constructors die this aanroepen krijgen dus meerdere keren een handle gezet
     * -constructors die super aanroepen waarbij de super ook een handle krijgt wordt meerdere
     * keren een handle gezet.
     */
    private void transformConstructors() {
        for (MethodNode methodNode : (List<MethodNode>) materializedClassNode.methods) {
            if (methodNode.name.equals("<init>")) {
                InstructionsBuilder codeBuilder = new InstructionsBuilder();
                codeBuilder.ALOAD(0);
                codeBuilder.NEW(DefaultMultiversionedHandle.class);
                codeBuilder.DUP();
                codeBuilder.INVOKESPECIAL(DefaultMultiversionedHandle.class, "<init>", "()V");
                codeBuilder.PUTFIELD(materializedClassNode, VARNAME_HANDLE, MultiversionedHandle.class);

                int indexOfReturn = indexOfReturn(methodNode.instructions);
                methodNode.instructions.insertBefore(methodNode.instructions.get(indexOfReturn), codeBuilder.createInstructions());
            }
        }
    }

    /**
     * Todo:
     * instead of placing code for the last return, it should be better if the code is added
     * to the at the beginning. A constructor could have multiple exit points, to if you add
     * the code for the last return, you have to add it multiple times.
     *
     * @param list
     * @return
     */
    public static int indexOfReturn(InsnList list) {
        for (int k = 0; k < list.size(); k++) {
            if (list.get(k).getOpcode() == RETURN)
                return k;
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
            INVOKESPECIAL(AsmUtils.getConstructor(Object.class));
            ALOAD(0);
            ALOAD(1);
            GETFIELD(dematerializedClassNode, VARNAME_HANDLE, MultiversionedHandle.class);
            PUTFIELD(materializedClassNode, VARNAME_HANDLE, MultiversionedHandle.class);
            ALOAD(0);
            ALOAD(1);
            PUTFIELD(materializedClassNode, VARNAME_LASTMATERIALIZED, dematerializedClassNode);

            for (FieldNode field : (List<FieldNode>) materializedClassNode.fields) {
                if (!isSynthetic(field)) {
                    if (isTmEntity(field.desc, classLoader)) {
                        ALOAD(0);
                        ALOAD(2);
                        ALOAD(1);
                        GETFIELD(dematerializedClassNode, field.name, MultiversionedHandle.class);
                        Method readLazyMethod = AsmUtils.getMethod(Transaction.class, "readLazy", Handle.class);
                        INVOKEINTERFACE(readLazyMethod);
                        CHECKCAST(LazyReference.class);
                        PUTFIELD(materializedClassNode, field.name + "Ref", LazyReference.class);
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
            initWithInterfaceMethod(AsmUtils.getMethod(MaterializedObject.class, "setNextInChain", MaterializedObject.class));

            ALOAD(0);
            ALOAD(1);
            PUTFIELD(materializedClassNode, VARNAME_NEXTINCHAIN, MaterializedObject.class);
            RETURN();
        }
    }

    private class GetNextInChainMethodBuilder extends MethodBuilder {
        GetNextInChainMethodBuilder() {
            initWithInterfaceMethod(AsmUtils.getMethod(MaterializedObject.class, "getNextInChain"));

            ALOAD(0);
            GETFIELD(materializedClassNode, VARNAME_NEXTINCHAIN, MaterializedObject.class);
            ARETURN();
        }
    }

    private class DematerializeMethodBuilder extends MethodBuilder {
        DematerializeMethodBuilder() {
            initWithInterfaceMethod(AsmUtils.getMethod(MaterializedObject.class, "dematerialize"));

            ALOAD(0);
            NEW(dematerializedClassNode);
            DUP();
            ALOAD(0);
            ACONST_NULL();
            INVOKESPECIAL(dematerializedClassNode, "<init>", format("(L%s;L%s;)V", materializedClassNode.name, Type.getInternalName(Transaction.class)));
            DUP_X1();
            PUTFIELD(materializedClassNode, "lastMaterialized", dematerializedClassNode);
            ARETURN();
        }
    }

    private class GetHandleMethodBuilder extends MethodBuilder {

        GetHandleMethodBuilder() {
            initWithInterfaceMethod(AsmUtils.getMethod(MaterializedObject.class, "getHandle"));

            ALOAD(0);
            GETFIELD(materializedClassNode, VARNAME_HANDLE, MultiversionedHandle.class);
            ARETURN();
        }
    }

    private class IsDirtyMethodBuilder extends MethodBuilder {

        IsDirtyMethodBuilder() {
            initWithInterfaceMethod(AsmUtils.getMethod(MaterializedObject.class, "isDirty"));

            ICONST_TRUE();
            IRETURN();
            /*

            ALOAD(0);
            GETFIELD(materializedClassNode, VARNAME_LASTMATERIALIZED, internalFormToDescriptor(dematerializedClassNode.name));
            LabelNode nonNullLastMaterialized = new LabelNode();
            IFNONNULL(nonNullLastMaterialized);
            ICONST_TRUE();
            IRETURN();

            placeLabelNode(nonNullLastMaterialized);

            for (FieldNode field : (List<FieldNode>) materializedClassNode.fields) {
                if (!isSynthetic(field)) {
                    ALOAD(0);
                    LabelNode equals = new LabelNode();
                    GETFIELD(materializedClassNode, VARNAME_LASTMATERIALIZED, internalFormToDescriptor(dematerializedClassNode.name));
                    GETFIELD(dematerializedClassNode, field.name, internalFormToDescriptor(dematerializedClassNode.name));
                    ALOAD(0);
                    GETFIELD(materializedClassNode, field.name, field.desc);

                    //if (field.getType().isPrimitive()) {
                    //    IF_ICMPEQ(equals);
                    //} else if (field.getType().isAnnotationPresent(Dematerializable.class)) {
                    IF_ICMPEQ(equals);//todo
                    //} else {
                    //    IF_ACMPEQ(equals);
                    //}

                    ICONST_TRUE();
                    IRETURN();
                    placeLabelNode(equals);
                }
            }

            ICONST_FALSE();
            IRETURN();*/
        }
    }

    private class WalkMaterializedMembersMethodBuilder extends MethodBuilder {

        WalkMaterializedMembersMethodBuilder() {
            initWithInterfaceMethod(AsmUtils.getMethod(MaterializedObject.class, "walkMaterializedMembers", MemberWalker.class));

            LabelNode next = new LabelNode();

            for (FieldNode field : (List<FieldNode>) materializedClassNode.fields) {
                if (!isSynthetic(field)) {
                    if (isTmEntity(field.desc, classLoader)) {
                        ALOAD(0);
                        GETFIELD(materializedClassNode, field);
                        IFNULL(next);
                        ALOAD(1);
                        ALOAD(0);
                        GETFIELD(materializedClassNode, field);
                        CHECKCAST(MaterializedObject.class);
                        Method onMemberMethod = AsmUtils.getMethod(MemberWalker.class, "onMember", MaterializedObject.class);
                        INVOKEINTERFACE(onMemberMethod);
                        placeLabelNode(next);
                        next = new LabelNode();
                    }
                }
            }

            RETURN();
        }
    }
}
