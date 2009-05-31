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
import org.objectweb.asm.tree.MethodNode;

import static java.lang.String.format;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DematerializedClassBuilder extends ClassNodeBuilder {

    public final static Method getHandleMethod = getMethod(
            MultiversionedStmUtils.class, "getHandle", LazyReference.class, Object.class);

    public final static Constructor objectConstructor = AsmUtils.getConstructor(Object.class);

    private static final String VARNAME_HANDLE = "$handle";

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
        addMethod(buildConstructor());
        addMethod(buildGetHandleMethod());
        addMethod(buildRematerializeMethod());
    }

    private void addFields() {
        addPublicFinalSyntheticField(VARNAME_HANDLE, MultiversionedHandle.class);

        //a clone of the list is made to prevent concurrentmodificationexceptions.
        for (FieldNode field : (List<FieldNode>) new ArrayList(materializedClass.fields)) {
            if (!isExcluded(field)) {
                if (isTmEntity(field.desc, classLoader)) {
                    addPublicFinalSyntheticField(field.name, MultiversionedHandle.class);
                } else {
                    addPublicFinalSyntheticField(field.name, field.desc);
                }
            }
        }
    }

    private MethodNode buildConstructor() {
        MethodBuilder builder = new MethodBuilder();
        builder.setName("<init>");
        builder.setDescriptor(format("(L%s;L%s;)V", materializedClass.name, getInternalName(Transaction.class)));

        builder.ALOAD(0);
        builder.INVOKESPECIAL(objectConstructor);
        builder.ALOAD(0);
        builder.ALOAD(1);
        builder.GETFIELD(materializedClass, VARNAME_HANDLE, MultiversionedHandle.class);
        builder.PUTFIELD(getClassInternalName(), VARNAME_HANDLE, MultiversionedHandle.class);

        for (FieldNode field : (List<FieldNode>) materializedClass.fields) {
            if (!isExcluded(field)) {
                if (isTmEntity(field.desc, classLoader)) {
                    builder.ALOAD(0);
                    builder.ALOAD(1);
                    builder.GETFIELD(materializedClass, field.name + "$Ref", LazyReference.class);
                    builder.ALOAD(1);
                    builder.GETFIELD(materializedClass, field);
                    builder.INVOKESTATIC(getHandleMethod);
                    builder.PUTFIELD(getClassInternalName(), field.name, MultiversionedHandle.class);
                } else {
                    builder.ALOAD(0);
                    builder.ALOAD(1);
                    builder.GETFIELD(materializedClass, field);
                    builder.PUTFIELD(getClassInternalName(), field.name, field.desc);
                }
            }
        }
        builder.RETURN();
        return builder.createMethod();
    }

    private MethodNode buildGetHandleMethod() {
        MethodBuilder builder = new MethodBuilder();
        builder.initWithInterfaceMethod(getMethod(DematerializedObject.class, "getHandle"));

        builder.ALOAD(0);
        builder.GETFIELD(getClassInternalName(), VARNAME_HANDLE, MultiversionedHandle.class);
        builder.ARETURN();

        return builder.createMethod();
    }

    private MethodNode buildRematerializeMethod() {
        MethodBuilder builder = new MethodBuilder();
        builder.initWithInterfaceMethod(getMethod(DematerializedObject.class, "rematerialize", Transaction.class));

        builder.NEW(materializedClass);
        builder.DUP();
        builder.ALOAD(0);
        builder.ALOAD(1);
        String constructorDescriptor = format("(L%s;%s)V", getClassInternalName(), getDescriptor(Transaction.class));
        builder.INVOKESPECIAL(materializedClass, "<init>", constructorDescriptor);
        builder.ARETURN();
        return builder.createMethod();
    }
}
