package org.multiverse.instrumentation;

import org.multiverse.api.Transaction;
import org.multiverse.instrumentation.utils.ClassBuilder;
import static org.multiverse.instrumentation.utils.InstrumentationUtils.*;
import org.multiverse.instrumentation.utils.MethodBuilder;
import org.multiverse.multiversionedstm.DematerializedObject;
import org.multiverse.multiversionedstm.MultiversionedHandle;
import static org.objectweb.asm.Type.getDescriptor;
import static org.objectweb.asm.Type.getInternalName;
import org.objectweb.asm.tree.ClassNode;

import static java.lang.String.format;

public class DematerializedClassBuilder extends ClassBuilder {
    private static final String VARNAME_HANDLE = "handle";

    private final ClassNode materializedClass;

    public DematerializedClassBuilder(ClassNode materializedClass) {
        this.materializedClass = materializedClass;
        this.classNode.version = V1_5;
        this.classNode.name = getInternalNameOfDematerializedClass(materializedClass);
        this.classNode.outerClass = materializedClass.name;
        setAccess(ACC_PUBLIC | ACC_FINAL);

        addInterface(DematerializedObject.class);

        addPublicFinalSyntheticField(VARNAME_HANDLE, MultiversionedHandle.class);

        //for (Field field : materializedClass.getFields()) {
        //    if (field.getType().isAnnotationPresent(Dematerializable.class)) {
        //        addPublicFinalSyntheticField(field.getName(), MultiversionedHandle.class);
        //    } else {
        //        addPublicFinalSyntheticField(field.getName(), field.getType());
        //    }
        //}

        addMethod(new ConstructorBuilder());
        addMethod(new GetHandleMethodBuilder());
        addMethod(new RematerializeMethodBuilder());
    }

    private class ConstructorBuilder extends MethodBuilder {

        private ConstructorBuilder() {
            setName("<init>");
            setDescriptor(format("(L%s;L%s;)V", materializedClass.name, getInternalName(Transaction.class)));

            ALOAD(0);
            INVOKESPECIAL(getConstructor(Object.class));
            ALOAD(0);
            ALOAD(1);

            //todo: de rest van de constructor.

            /*
             6 invokevirtual #26 <org/multiverse/multiversionedstm/examples/IntegerValue.getHandle>
             9 putfield #28 <org/multiverse/multiversionedstm/examples/IntegerValue$DematerializedIntegerValue.handle>
            12 aload_0
            13 aload_1
            14 invokestatic #32 <org/multiverse/multiversionedstm/examples/IntegerValue.access$300>
            17 putfield #34 <org/multiverse/multiversionedstm/examples/IntegerValue$DematerializedIntegerValue.value>
            */
            RETURN();
        }
    }

    private class GetHandleMethodBuilder extends MethodBuilder {
        private GetHandleMethodBuilder() {
            initWithInterfaceMethod(getMethod(DematerializedObject.class, "getHandle"));

            ALOAD(0);
            GETFIELD(getClassInternalName(), VARNAME_HANDLE, MultiversionedHandle.class);
            ARETURN();
        }
    }

    private class RematerializeMethodBuilder extends MethodBuilder {

        private RematerializeMethodBuilder() {
            initWithInterfaceMethod(getMethod(DematerializedObject.class, "rematerialize", Transaction.class));

            NEW(materializedClass);
            DUP();
            ALOAD(0);
            ALOAD(1);
            String desc = format("(L%s;%s)V", getClassInternalName(), getDescriptor(Transaction.class));
            INVOKESPECIAL(materializedClass, "<init>", desc);
            ARETURN();
        }
    }
}
