package org.multiverse.instrumentation;

import org.multiverse.api.LazyReference;
import org.multiverse.api.Transaction;
import org.multiverse.instrumentation.utils.AsmUtils;
import static org.multiverse.instrumentation.utils.AsmUtils.*;
import org.multiverse.instrumentation.utils.ClassNodeBuilder;
import org.multiverse.instrumentation.utils.MethodBuilder;
import org.multiverse.multiversionedstm.DematerializedObject;
import org.multiverse.multiversionedstm.MultiversionedHandle;
import org.multiverse.multiversionedstm.MultiversionedStmUtils;
import static org.objectweb.asm.Type.getDescriptor;
import static org.objectweb.asm.Type.getInternalName;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import static java.lang.String.format;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DematerializedClassBuilder extends ClassNodeBuilder {

    private static final String VARNAME_HANDLE = "handle";

    private final ClassNode materializedClass;
    private final ClassLoader classLoader;

    public DematerializedClassBuilder(ClassNode materializedClass, ClassLoader classLoader) {
        if (materializedClass == null || classLoader == null) {
            throw new NullPointerException();
        }

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

        //a clone of the list is made to prevent concurrentmodificationexceptions.
        for (FieldNode field : (List<FieldNode>) new ArrayList(materializedClass.fields)) {
            if (!isSynthetic(field) && !isStatic(field)) {
                if (isTmEntity(field.desc, classLoader)) {
                    addPublicFinalSyntheticField(field.name, MultiversionedHandle.class);
                } else {
                    addPublicFinalSyntheticField(field.name, field.desc);
                }
            }
        }
    }

    private class ConstructorBuilder extends MethodBuilder {

        private ConstructorBuilder() {
            setName("<init>");
            setDescriptor(format("(L%s;L%s;)V", materializedClass.name, getInternalName(Transaction.class)));

            ALOAD(0);
            Constructor objectConstructor = AsmUtils.getConstructor(Object.class);
            INVOKESPECIAL(objectConstructor);
            ALOAD(0);
            ALOAD(1);
            GETFIELD(materializedClass, VARNAME_HANDLE, MultiversionedHandle.class);
            PUTFIELD(getClassInternalName(), VARNAME_HANDLE, MultiversionedHandle.class);

            for (FieldNode field : (List<FieldNode>) materializedClass.fields) {
                if (!isSynthetic(field) && !isStatic(field)) {
                    if (isTmEntity(field.desc, classLoader)) {
                        ALOAD(0);
                        ALOAD(1);
                        GETFIELD(materializedClass, field.name + "Ref", LazyReference.class);
                        ALOAD(1);
                        GETFIELD(materializedClass, field);
                        Method getHandleMethod = getMethod(MultiversionedStmUtils.class, "getHandle", LazyReference.class, Object.class);
                        INVOKESTATIC(getHandleMethod);
                        PUTFIELD(getClassInternalName(), field.name, MultiversionedHandle.class);
                    } else {
                        ALOAD(0);
                        ALOAD(1);
                        GETFIELD(materializedClass, field);
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
            String constructorDescriptor = format("(L%s;%s)V", getClassInternalName(), getDescriptor(Transaction.class));
            INVOKESPECIAL(materializedClass, "<init>", constructorDescriptor);
            ARETURN();
        }
    }
}
