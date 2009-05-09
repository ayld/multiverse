package org.multiverse.instrumentation;

import org.multiverse.api.Dematerializable;
import org.multiverse.api.Transaction;
import static org.multiverse.instrumentation.utils.AsmUtils.hasVisibleAnnotation;
import static org.multiverse.instrumentation.utils.AsmUtils.isSynthetic;
import org.multiverse.instrumentation.utils.ClassBuilder;
import org.multiverse.instrumentation.utils.InstrumentationUtils;
import static org.multiverse.instrumentation.utils.InstrumentationUtils.*;
import org.multiverse.instrumentation.utils.MethodBuilder;
import org.multiverse.multiversionedstm.MaterializedObject;
import org.multiverse.multiversionedstm.MemberWalker;
import org.multiverse.multiversionedstm.MultiversionedHandle;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Type.getInternalName;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.LabelNode;

import static java.lang.String.format;
import java.util.List;

public class DematerializableClassTransformer extends ClassBuilder {

    private static final String VARNAME_HANDLE = "handle";
    private static final String VARNAME_NEXTINCHAIN = "nextInChain";
    private static final String VARNAME_LASTMATERIALIZED = "lastMaterialized";

    private ClassNode materializedClassNode;

    private ClassLoader classLoader;
    private ClassNode dematerializedClassNode;

    public DematerializableClassTransformer(ClassNode materializedClassNode, ClassNode dematerialized, ClassLoader classLoader) {
        super(materializedClassNode);

        this.dematerializedClassNode = dematerialized;
        this.classLoader = classLoader;
        this.materializedClassNode = materializedClassNode;

        addInterface(MaterializedObject.class);

        addPublicFinalSyntheticField(VARNAME_HANDLE, MultiversionedHandle.class);
        addPublicSyntheticField(VARNAME_NEXTINCHAIN, MaterializedObject.class);
        addPublicSyntheticField(VARNAME_LASTMATERIALIZED, internalFormToDescriptor(dematerialized.name));

        for (FieldNode field : (List<FieldNode>) materializedClassNode.fields) {
            if (!isSynthetic(field)) {
                //    if (field.getType().isAnnotationPresent(Dematerializable.class)) {
                //        addPublicSyntheticField(field.name + "Ref", LazyReference.class);
                //    }
            }
        }

        addMethod(new RematerializeConstructorBuilder());
        addMethod(new WalkMaterializedMembersMethodBuilder());
        addMethod(new SetNextInChainMethodBuilder());
        addMethod(new GetNextInChainMethodBuilder());
        addMethod(new DematerializeMethodBuilder());
        //addMethod(new IsDirtyMethodBuilder());
        addMethod(new GetHandleMethodBuilder());
        addDematerializedInnerClass();
    }

    private void addDematerializedInnerClass() {
        InnerClassNode node = new InnerClassNode(
                dematerializedClassNode.name,//innerclass name
                materializedClassNode.name,//outerclass name
                InstrumentationUtils.getInnerInternalNameOfDematerializedClass(materializedClassNode),
                ACC_STATIC | ACC_PUBLIC);

        classNode.innerClasses.add(node);
    }

    private class RematerializeConstructorBuilder extends MethodBuilder {
        RematerializeConstructorBuilder() {
            methodNode.access = ACC_PUBLIC;
            methodNode.name = "<init>";
            methodNode.desc = format("(L%s;L%s;)V", dematerializedClassNode.name, Type.getInternalName(Transaction.class));

            ALOAD(0);
            INVOKESPECIAL(getConstructor(Object.class));
            ALOAD(0);
            ALOAD(1);


            //handle assignen
            //lastMaterialized assignen
            //todo: the fields need to be assigned.

            RETURN();
        }
    }

    private class SetNextInChainMethodBuilder extends MethodBuilder {
        SetNextInChainMethodBuilder() {
            initWithInterfaceMethod(getMethod(MaterializedObject.class, "setNextInChain", MaterializedObject.class));

            ALOAD(0);
            ALOAD(1);
            PUTFIELD(materializedClassNode, VARNAME_NEXTINCHAIN, MaterializedObject.class);
            RETURN();
        }
    }

    private class GetNextInChainMethodBuilder extends MethodBuilder {
        GetNextInChainMethodBuilder() {
            initWithInterfaceMethod(getMethod(MaterializedObject.class, "getNextInChain"));

            ALOAD(0);
            GETFIELD(materializedClassNode, VARNAME_NEXTINCHAIN, MaterializedObject.class);
            ARETURN();
        }
    }

    private class DematerializeMethodBuilder extends MethodBuilder {
        DematerializeMethodBuilder() {
            initWithInterfaceMethod(getMethod(MaterializedObject.class, "dematerialize"));
            ALOAD(0);
            NEW(dematerializedClassNode);
            DUP();
            ALOAD(0);
            ACONST_NULL();
            INVOKESPECIAL(dematerializedClassNode, "<init>", format("(L%s;L%s;)V", materializedClassNode.name, getInternalName(Transaction.class)));
            DUP_X1();
            PUTFIELD(materializedClassNode, "lastMaterialized", dematerializedClassNode);
            ARETURN();
        }
    }

    private class GetHandleMethodBuilder extends MethodBuilder {

        GetHandleMethodBuilder() {
            initWithInterfaceMethod(getMethod(MaterializedObject.class, "getHandle"));

            ALOAD(0);
            GETFIELD(materializedClassNode, "handle", MultiversionedHandle.class);
            ARETURN();
        }
    }

    private class IsDirtyMethodBuilder extends MethodBuilder {

        IsDirtyMethodBuilder() {
            initWithInterfaceMethod(getMethod(MaterializedObject.class, "isDirty"));

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
            IRETURN();
        }
    }

    private class WalkMaterializedMembersMethodBuilder extends MethodBuilder {

        WalkMaterializedMembersMethodBuilder() {
            initWithInterfaceMethod(getMethod(MaterializedObject.class, "walkMaterializedMembers", MemberWalker.class));

            LabelNode next = new LabelNode();


            for (FieldNode field : (List<FieldNode>) materializedClassNode.fields) {
                if (!isSynthetic(field)) {
                    if (hasVisibleAnnotation(field.desc, Dematerializable.class, classLoader)) {
                        ALOAD(0);
                        GETFIELD(materializedClassNode, field.name, field.desc);
                        IFNULL(next);
                        ALOAD(1);
                        ALOAD(0);
                        GETFIELD(materializedClassNode, field.name, field.desc);
                        CHECKCAST(MaterializedObject.class);
                        INVOKEINTERFACE(getMethod(MemberWalker.class, "onMember", MaterializedObject.class));
                        placeLabelNode(next);
                        next = new LabelNode();
                    }
                }
            }

            RETURN();
        }
    }
}
