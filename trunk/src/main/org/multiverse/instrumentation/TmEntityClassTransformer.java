package org.multiverse.instrumentation;

import org.multiverse.api.LazyReference;
import org.multiverse.api.TmEntity;
import org.multiverse.api.Transaction;
import static org.multiverse.instrumentation.utils.AsmUtils.hasVisibleAnnotation;
import static org.multiverse.instrumentation.utils.AsmUtils.isSynthetic;
import org.multiverse.instrumentation.utils.ClassBuilder;
import static org.multiverse.instrumentation.utils.InstrumentationUtils.*;
import org.multiverse.instrumentation.utils.MethodBuilder;
import org.multiverse.multiversionedstm.DefaultMultiversionedHandle;
import org.multiverse.multiversionedstm.MaterializedObject;
import org.multiverse.multiversionedstm.MemberWalker;
import org.multiverse.multiversionedstm.MultiversionedHandle;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Type.getDescriptor;
import static org.objectweb.asm.Type.getInternalName;
import org.objectweb.asm.tree.*;

import static java.lang.String.format;
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
        addPublicSyntheticField(VARNAME_LASTMATERIALIZED, internalFormToDescriptor(dematerialized.name));

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
                if (hasVisibleAnnotation(field.desc, TmEntity.class, classLoader)) {
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
                InsnList extra = new InsnList();

                extra.add(
                        new VarInsnNode(ALOAD, 0)
                );

                extra.add(
                        new TypeInsnNode(
                                NEW,
                                getInternalName(DefaultMultiversionedHandle.class)
                        )
                );

                extra.add(
                        new InsnNode(DUP)
                );

                extra.add(
                        new MethodInsnNode(
                                INVOKESPECIAL,
                                getInternalName(DefaultMultiversionedHandle.class),
                                "<init>",
                                "()V"
                        )
                );

                extra.add(
                        new FieldInsnNode(
                                PUTFIELD,
                                materializedClassNode.name,
                                VARNAME_HANDLE,
                                getDescriptor(MultiversionedHandle.class)
                        )
                );

                int indexOfReturn = indexOfReturn(methodNode.instructions);
                methodNode.instructions.insertBefore(methodNode.instructions.get(indexOfReturn), extra);
            }
        }
    }

    public int indexOfReturn(InsnList list) {
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
                getInnerInternalNameOfDematerializedClass(materializedClassNode),
                ACC_STATIC | ACC_PUBLIC);

        classNode.innerClasses.add(node);
    }

    /*
 0 aload_0
 1 invokespecial #35 <java/lang/Object.<init>>
 4 aload_0
 5 aload_1
 6 putfield #59 <org/multiverse/multiversionedstm/examples/StackNode.lastDematerialized>
 9 aload_0
10 aload_1
11 invokevirtual #63 <org/multiverse/multiversionedstm/examples/StackNode$DematerializedNode.getHandle>
14 putfield #40 <org/multiverse/multiversionedstm/examples/StackNode.handle>
17 aload_0
18 aload_1
19 invokestatic #67 <org/multiverse/multiversionedstm/examples/StackNode$DematerializedNode.access$000>
22 instanceof #69 <org/multiverse/api/Handle>
25 ifeq 44 (+19)
28 aload_2
29 aload_1
30 invokestatic #67 <org/multiverse/multiversionedstm/examples/StackNode$DematerializedNode.access$000>
33 checkcast #69 <org/multiverse/api/Handle>
36 invokeinterface #75 <org/multiverse/api/Transaction.read> count 2
41 goto 48 (+7)
44 aload_1
45 invokestatic #67 <org/multiverse/multiversionedstm/examples/StackNode$DematerializedNode.access$000>
48 putfield #44 <org/multiverse/multiversionedstm/examples/StackNode.value>
51 aload_0
52 aload_2
53 aload_1
54 invokestatic #79 <org/multiverse/multiversionedstm/examples/StackNode$DematerializedNode.access$100>
57 invokeinterface #83 <org/multiverse/api/Transaction.readLazyAndUnmanaged> count 2
62 putfield #49 <org/multiverse/multiversionedstm/examples/StackNode.nextRef>
65 return

     */

    private class RematerializeConstructorBuilder extends MethodBuilder {
        RematerializeConstructorBuilder() {
            methodNode.access = ACC_PUBLIC;
            methodNode.name = "<init>";
            methodNode.desc = format("(L%s;L%s;)V", dematerializedClassNode.name, Type.getInternalName(Transaction.class));

            ALOAD(0);
            INVOKESPECIAL(getConstructor(Object.class));
            ALOAD(0);
            ALOAD(1);
            GETFIELD(dematerializedClassNode, VARNAME_HANDLE, MultiversionedHandle.class);
            PUTFIELD(materializedClassNode, VARNAME_HANDLE, MultiversionedHandle.class);
            ALOAD(0);
            ALOAD(1);
            PUTFIELD(materializedClassNode, VARNAME_LASTMATERIALIZED, dematerializedClassNode);

            for (FieldNode field : (List<FieldNode>) materializedClassNode.fields) {
                if (!isSynthetic(field)) {
                    if (hasVisibleAnnotation(field.desc, TmEntity.class, classLoader)) {
                        //ALOAD(0);
                        //ALOAD(2);
                        //ALOAD(1);
                        //CHECKCAST(Handle.class);
                        //INVOKEINTERFACE(getMethod(Transaction.class, "read", Handle.class));
                        //PUTFIELD(materializedClassNode, field.name, LazyReference.class);
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
            GETFIELD(materializedClassNode, VARNAME_HANDLE, MultiversionedHandle.class);
            ARETURN();
        }
    }

    private class IsDirtyMethodBuilder extends MethodBuilder {

        IsDirtyMethodBuilder() {
            initWithInterfaceMethod(getMethod(MaterializedObject.class, "isDirty"));

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
            initWithInterfaceMethod(getMethod(MaterializedObject.class, "walkMaterializedMembers", MemberWalker.class));

            LabelNode next = new LabelNode();


            for (FieldNode field : (List<FieldNode>) materializedClassNode.fields) {
                if (!isSynthetic(field)) {
                    if (hasVisibleAnnotation(field.desc, TmEntity.class, classLoader)) {
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
