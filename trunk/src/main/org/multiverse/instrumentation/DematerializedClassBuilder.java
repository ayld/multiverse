package org.multiverse.instrumentation;

import org.multiverse.api.LazyReference;
import org.multiverse.api.TmEntity;
import org.multiverse.api.Transaction;
import static org.multiverse.instrumentation.utils.AsmUtils.hasVisibleAnnotation;
import static org.multiverse.instrumentation.utils.AsmUtils.isSynthetic;
import org.multiverse.instrumentation.utils.ClassBuilder;
import static org.multiverse.instrumentation.utils.InstrumentationUtils.*;
import org.multiverse.instrumentation.utils.MethodBuilder;
import org.multiverse.multiversionedstm.DematerializedObject;
import org.multiverse.multiversionedstm.MultiversionedHandle;
import org.multiverse.multiversionedstm.MultiversionedStmUtils;
import static org.objectweb.asm.Type.getDescriptor;
import static org.objectweb.asm.Type.getInternalName;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import static java.lang.String.format;
import java.util.ArrayList;
import java.util.List;

public class DematerializedClassBuilder extends ClassBuilder {

    private static final String VARNAME_HANDLE = "handle";

    private final ClassNode materializedClass;
    private final ClassLoader classLoader;

    public DematerializedClassBuilder(ClassNode materializedClass, ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.materializedClass = materializedClass;
        this.classNode.version = V1_5;
        this.classNode.outerClass = materializedClass.name;
        this.classNode.name = getInternalNameOfDematerializedClass(materializedClass);

        setAccess(ACC_PUBLIC | ACC_FINAL);
        addInterface(DematerializedObject.class);
        addFields();
        addMethod(new ConstructorBuilder());
        addMethod(new GetHandleMethodBuilder());
        addMethod(new RematerializeMethodBuilder());
    }

    private void addFields() {
        addPublicFinalSyntheticField(VARNAME_HANDLE, MultiversionedHandle.class);

        for (FieldNode field : (List<FieldNode>) new ArrayList(materializedClass.fields)) {
            if (!isSynthetic(field)) {
                if (hasVisibleAnnotation(field.desc, TmEntity.class, classLoader)) {
                    addPublicFinalSyntheticField(field.name, LazyReference.class);
                } else {
                    addPublicFinalSyntheticField(field.name, field.desc);
                }
            }
        }
    }

    /*
12 aload_0
13 aload_1
14 invokestatic #35 <org/multiverse/multiversionedstm/examples/Stack.access$300>
17 aload_1
18 getfield #39 <org/multiverse/multiversionedstm/examples/Stack.head>
21 invokestatic #44 <org/multiverse/multiversionedstm/MultiversionedStmUtils.getHandle>
24 putfield #46 <org/multiverse/multiversionedstm/examples/Stack$DematerializedStack.headHandle>
    */

    private class ConstructorBuilder extends MethodBuilder {

        private ConstructorBuilder() {
            setName("<init>");
            setDescriptor(format("(L%s;L%s;)V", materializedClass.name, getInternalName(Transaction.class)));

            ALOAD(0);
            INVOKESPECIAL(getConstructor(Object.class));
            ALOAD(0);
            ALOAD(1);
            GETFIELD(materializedClass, VARNAME_HANDLE, MultiversionedHandle.class);
            PUTFIELD(getClassInternalName(), VARNAME_HANDLE, MultiversionedHandle.class);

            for (FieldNode field : (List<FieldNode>) materializedClass.fields) {
                if (!isSynthetic(field)) {
                    if (hasVisibleAnnotation(field.desc, TmEntity.class, classLoader)) {
                        ALOAD(0);
                        ALOAD(1);
                        GETFIELD(materializedClass, field.name + "Ref", LazyReference.class);
                        ALOAD(1);
                        GETFIELD(materializedClass, field.name, field.desc);
                        INVOKESTATIC(getMethod(MultiversionedStmUtils.class, "getHandle", LazyReference.class, Object.class));
                        PUTFIELD(getClassInternalName(), field.name, MultiversionedHandle.class);
                    } else {
                        ALOAD(0);
                        ALOAD(1);
                        GETFIELD(materializedClass, field.name, field.desc);
                        PUTFIELD(getClassInternalName(), field.name, field.desc);
                    }
                }

            }
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
