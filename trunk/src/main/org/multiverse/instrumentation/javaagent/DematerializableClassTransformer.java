package org.multiverse.instrumentation.javaagent;

import org.multiverse.api.Dematerializable;
import org.multiverse.api.LazyReference;
import static org.multiverse.instrumentation.javaagent.InstrumentationUtils.*;
import org.multiverse.multiversionedstm.MaterializedObject;
import org.multiverse.multiversionedstm.MemberWalker;
import org.multiverse.multiversionedstm.MultiversionedHandle;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Type.getInternalName;
import org.objectweb.asm.tree.LabelNode;

import java.lang.reflect.Field;

public class DematerializableClassTransformer extends ClassBuilder {

    private String dematerializedClass;
    private Class materializedClass;

    public DematerializableClassTransformer(Class materializedClass) {
        super(materializedClass);

        this.materializedClass = materializedClass;
        this.dematerializedClass = getInternalNameOfDematerializedClass(materializedClass);

        addInterface(MaterializedObject.class);

        addPublicFinalSyntheticField("handle", MultiversionedHandle.class);
        addPublicSyntheticField("lastMaterialized", internalFormToDescriptor(dematerializedClass));

        for (Field field : materializedClass.getFields()) {
            if (!field.isSynthetic()) {
                if (field.getType().isAnnotationPresent(Dematerializable.class)) {
                    addPublicSyntheticField(field.getName() + "Ref", LazyReference.class);
                }
            }
        }

        addMethod(new WalkMaterializedMembersMethodBuilder());
        addMethod(new SetNextInChainMethodBuilder());
        addMethod(new GetNextInChainMethodBuilder());
        addMethod(new DematerializeMethodBuilder());
        addMethod(new IsDirtyMethodBuilder());
        addMethod(new GetHandleMethodBuilder());
    }

    //todo
    private class ConstructorBuilder extends MethodBuilder {

    }

    private class SetNextInChainMethodBuilder extends MethodBuilder {
        SetNextInChainMethodBuilder() {
            initWithInterfaceMethod(getMethod(MaterializedObject.class, "setNextInChain", MaterializedObject.class));

            ALOAD(0);
            ALOAD(1);
            PUTFIELD(materializedClass, "nextInChain", MultiversionedHandle.class);
            RETURN();
        }
    }

    private class GetNextInChainMethodBuilder extends MethodBuilder {
        GetNextInChainMethodBuilder() {
            initWithInterfaceMethod(getMethod(MaterializedObject.class, "getNextInChain"));

            ALOAD(0);
            GETFIELD(materializedClass, "nextInChain", MaterializedObject.class);
            ARETURN();
        }
    }

    private class DematerializeMethodBuilder extends MethodBuilder {
        DematerializeMethodBuilder() {
            initWithInterfaceMethod(getMethod(MaterializedObject.class, "dematerialize"));

            ALOAD(0);
            NEW(dematerializedClass);
            DUP();
            ALOAD(0);
            ACONST_NULL();
            INVOKESPECIAL(dematerializedClass, "<init>", getVoidMethodDescriptor(materializedClass));
            DUP_X1();
            PUTFIELD(getInternalName(materializedClass), "lastMaterialized", internalFormToDescriptor(dematerializedClass));
            ARETURN();
        }
    }

    private class GetHandleMethodBuilder extends MethodBuilder {

        GetHandleMethodBuilder() {
            initWithInterfaceMethod(getMethod(MaterializedObject.class, "getHandle"));

            ALOAD(0);
            GETFIELD(materializedClass, "handle", MultiversionedHandle.class);
            ARETURN();
        }
    }

    private class IsDirtyMethodBuilder extends MethodBuilder {

        IsDirtyMethodBuilder() {
            initWithInterfaceMethod(getMethod(MaterializedObject.class, "isDirty"));

            ALOAD(0);
            GETFIELD(getInternalName(materializedClass), "lastMaterialized", internalFormToDescriptor(dematerializedClass));
            LabelNode nonNullLastMaterialized = new LabelNode();
            IFNONNULL(nonNullLastMaterialized);
            ICONST_TRUE();
            IRETURN();
            placeLabelNode(nonNullLastMaterialized);

            for (Field field : materializedClass.getFields()) {
                if (!field.isSynthetic()) {
                    ALOAD(0);
                    LabelNode equals = new LabelNode();
                    GETFIELD(getInternalName(materializedClass), "lastDematerialized", internalFormToDescriptor(dematerializedClass));
                    GETFIELD(dematerializedClass, field.getName(), internalFormToDescriptor(dematerializedClass));
                    ALOAD(0);
                    GETFIELD(getInternalName(materializedClass), field.getName(), Type.getDescriptor(field.getType()));
                    if (field.getType().isPrimitive()) {
                        IF_ICMPEQ(equals);
                    } else if (field.getType().isAnnotationPresent(Dematerializable.class)) {
                        IF_ICMPEQ(equals);//todo
                    } else {
                        IF_ACMPEQ(equals);
                    }

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

            for (Field field : materializedClass.getFields()) {
                if (!field.isSynthetic()) {
                    if (field.getType().isAnnotationPresent(Dematerializable.class)) {
                        ALOAD(0);
                        GETFIELD(field.getDeclaringClass(), field.getName());
                        IFNULL(next);
                        ALOAD(1);
                        ALOAD(0);
                        GETFIELD(field.getDeclaringClass(), field.getName());
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
