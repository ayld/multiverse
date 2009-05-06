package org.multiverse.instrumentation.javaagent;

import org.multiverse.api.Dematerializable;
import org.multiverse.api.LazyReference;
import static org.multiverse.instrumentation.javaagent.InstrumentationUtils.*;
import org.multiverse.multiversionedstm.MaterializedObject;
import org.multiverse.multiversionedstm.MemberWalker;
import org.multiverse.multiversionedstm.MultiversionedHandle;
import static org.objectweb.asm.Type.getInternalName;

import java.lang.reflect.Field;

public class DematerializableClassTransformer extends ClassBuilder {

    private String dematerializedClass;
    private Class materializedClass;

    public DematerializableClassTransformer(Class materializedClass) {
        super(materializedClass);

        this.materializedClass = materializedClass;
        this.dematerializedClass = getInternalNameOfDematerializedClass(materializedClass);

        addInterface(MaterializedObject.class);

        addPublicFinalField("handle", MultiversionedHandle.class);
        addPublicFinalField("lastMaterialized", internalFormToDescriptor(dematerializedClass));

        for (Field field : materializedClass.getFields()) {
            if (field.getType().isAnnotationPresent(Dematerializable.class)) {
                addPublicFinalField(field.getName() + "Ref", LazyReference.class);
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

            /*
 0 aload_0
 1 getfield #66 <org/multiverse/multiversionedstm/examples/IntegerValue.lastDematerialized>
 4 ifnonnull 9 (+5)
 7 iconst_1
 8 ireturn
 9 aload_0
10 getfield #66 <org/multiverse/multiversionedstm/examples/IntegerValue.lastDematerialized>
13 invokestatic #74 <org/multiverse/multiversionedstm/examples/IntegerValue$DematerializedIntegerValue.access$100>
16 aload_0
17 getfield #36 <org/multiverse/multiversionedstm/examples/IntegerValue.value>
20 if_icmpeq 25 (+5)
23 iconst_1
24 ireturn
25 iconst_0
26 ireturn

             */

            ALOAD(0);
            //GETFIELD();

            codeForThrowRuntimeException();
        }
    }

    private class WalkMaterializedMembersMethodBuilder extends MethodBuilder {

        WalkMaterializedMembersMethodBuilder() {
            initWithInterfaceMethod(getMethod(MaterializedObject.class, "walkMaterializedMembers", MemberWalker.class));

            codeForThrowRuntimeException();
        }
    }
}
