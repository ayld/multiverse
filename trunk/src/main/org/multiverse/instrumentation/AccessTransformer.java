package org.multiverse.instrumentation;

import org.multiverse.api.LazyReference;
import static org.multiverse.instrumentation.utils.AsmUtils.isTmEntity;
import org.multiverse.instrumentation.utils.InstructionsBuilder;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.List;

public class AccessTransformer implements Opcodes {

    private final ClassNode materializedClassNode;
    private final ClassLoader classLoader;

    public AccessTransformer(ClassNode materializedClassNode, ClassLoader classLoader) {
        this.materializedClassNode = materializedClassNode;
        this.classLoader = classLoader;

        for (MethodNode method : (List<MethodNode>) materializedClassNode.methods) {
            transformMethod(method);
        }
    }


    public void transformMethod(MethodNode methodNode) {
        InsnList modifiedInstructions = methodNode.instructions;
        InsnList unmodifiedInstructions = clone(modifiedInstructions);

        for (int k = 0; k < unmodifiedInstructions.size(); k++) {
            AbstractInsnNode instruction = unmodifiedInstructions.get(k);
            if (isPutOnStmEntityField(instruction)) {
                FieldInsnNode putInstruction = (FieldInsnNode) instruction;
                InstructionsBuilder b = new InstructionsBuilder();

                //[.., materialized, value]
                b.DUP2();
                //[.., materialized, value, materialized, value]
                b.POP();
                //[.., materialized, value, materialized]
                b.ACONST_NULL();
                //[.., materialized, value, materialized, null]
                b.PUTFIELD(putInstruction.owner, putInstruction.name + "Ref", LazyReference.class);
                //[.., materialized, value]

                modifiedInstructions.insertBefore(putInstruction, b.createInstructions());
            } else if (isGetOnStmEntityField(instruction)) {
                FieldInsnNode getInstruction = (FieldInsnNode) instruction;
                InstructionsBuilder b = new InstructionsBuilder();

                //[.., materialized]
                b.DUP();
                //[.., materialized, materialized]
                b.GETFIELD(getInstruction.owner, getInstruction.name + "Ref", LazyReference.class);
                //[.., materialized, ref]
                LabelNode refIsNullLabel = new LabelNode();
                b.IFNULL(refIsNullLabel);
                //[.., materialized]
                b.DUP();
                //[.., materialized, materialized ]
                b.DUP2();
                //[.., materialized, materialized, materialized, materialized]
                b.GETFIELD(getInstruction.owner, getInstruction.name + "Ref", LazyReference.class);
                //[.., materialized, materialized, materialized, ref]
                b.codeForPrintClassTopItem();

                //b.INVOKEINTERFACE(getMethod(LazyReference.class, "get"));
                //[.., materialized, materialized, value]

                //[.., materialized, materialized, materialized, ref]
                b.POP();
                //[.., materialized, materialized, materialized]
                b.POP();
                //[.., materialized, materialized]
                b.ACONST_NULL();
                //[.., materialized, materialized, null]


                b.CHECKCAST(getInstruction.desc);
                //[.., materialized, materialized, value]
                b.PUTFIELD(getInstruction.owner, getInstruction.name, getInstruction.desc);
                //[.., materialized]
                b.DUP();
                //[.., materialized, materialized]
                b.ACONST_NULL();
                //[.., materialized, materialized, null]
                b.PUTFIELD(getInstruction.owner, getInstruction.name + "Ref", LazyReference.class);
                //[.., materialized]*/
                b.placeLabelNode(refIsNullLabel);

                modifiedInstructions.insertBefore(getInstruction, b.createInstructions());
            }
        }
    }

    /*
 0 aload_0
 1 getfield #82 <org/multiverse/multiversionedstm/examples/ExampleStack.headRef>
 4 ifnull 28 (+24)
 7 aload_0
 8 aload_0
 9 getfield #82 <org/multiverse/multiversionedstm/examples/ExampleStack.headRef>
12 invokeinterface #87 <org/multiverse/api/LazyReference.get> count 1
17 checkcast #47 <org/multiverse/multiversionedstm/examples/ExampleStackNode>
20 putfield #45 <org/multiverse/multiversionedstm/examples/ExampleStack.head>
23 aload_0
24 aconst_null
25 putfield #82 <org/multiverse/multiversionedstm/examples/ExampleStack.headRef>

28 return

     */

    public InsnList clone(InsnList insnList) {
        InsnList cloned = new InsnList();
        for (int k = 0; k < insnList.size(); k++) {
            cloned.add(insnList.get(k));
        }
        return cloned;
    }

    private boolean isPutOnStmEntityField(AbstractInsnNode insnNode) {
        if (insnNode.getOpcode() != PUTFIELD) {
            return false;
        }

        FieldInsnNode fieldInsnNode = (FieldInsnNode) insnNode;
        return isTmEntity(fieldInsnNode.desc, classLoader);
    }

    private boolean isGetOnStmEntityField(AbstractInsnNode insnNode) {
        if (insnNode.getOpcode() != GETFIELD) {
            return false;
        }

        FieldInsnNode fieldInsnNode = (FieldInsnNode) insnNode;
        return isTmEntity(fieldInsnNode.desc, classLoader);
    }

    public ClassNode create() {
        return materializedClassNode;
    }
}
