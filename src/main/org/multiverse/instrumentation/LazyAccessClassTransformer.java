package org.multiverse.instrumentation;

import org.multiverse.api.LazyReference;
import org.multiverse.instrumentation.utils.AsmUtils;
import static org.multiverse.instrumentation.utils.AsmUtils.getMethod;
import static org.multiverse.instrumentation.utils.AsmUtils.isTmEntity;
import org.multiverse.instrumentation.utils.InsnNodeListBuilder;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Method;
import java.util.List;

/**
 * The LazyAccessTransformer is responsible for transforming a ClassNode so that access it makes
 * to all @TmEntity objects, are modified if needed.
 * <p/>
 * For each member of type {@link @TmEntity} of an object, extra code will be generated to deal with the
 * LazyReference.
 *
 * @author Peter Veentjer.
 */
public class LazyAccessClassTransformer implements Opcodes {

    public final Method getMethod = getMethod(LazyReference.class, "get");

    private final ClassNode materializedClassNode;
    private final ClassLoader classLoader;

    public LazyAccessClassTransformer(ClassNode materializedClassNode, ClassLoader classLoader) {
        this.materializedClassNode = materializedClassNode;
        this.classLoader = classLoader;

        for (MethodNode method : (List<MethodNode>) materializedClassNode.methods) {
            transformMethod(method);
        }
    }

    public void transformMethod(MethodNode methodNode) {
        //a clone is created to prevent 'concurrent' failure. The unmodified is traversed and the
        //changes are made to the modifiedInstructions.
        InsnList modifiedInstructions = methodNode.instructions;
        InsnList unmodifiedInstructions = AsmUtils.cloneShallow(modifiedInstructions);

        for (int k = 0; k < unmodifiedInstructions.size(); k++) {
            AbstractInsnNode instruction = unmodifiedInstructions.get(k);

            if (isPutOnStmEntityField(instruction)) {
                FieldInsnNode putInstruction = (FieldInsnNode) instruction;
                InsnNodeListBuilder b = new InsnNodeListBuilder();

                //[.., materialized, value]
                b.DUP2();
                //[.., materialized, value, materialized, value]
                b.POP();
                //[.., materialized, value, materialized]
                b.ACONST_NULL();
                //[.., materialized, value, materialized, null]
                b.PUTFIELD(putInstruction.owner, putInstruction.name + "$Ref", LazyReference.class);
                //[.., materialized, value]

                modifiedInstructions.insertBefore(putInstruction, b.createInstructions());
            } else if (isGetOnStmEntityField(instruction)) {
                FieldInsnNode getInstruction = (FieldInsnNode) instruction;
                InsnNodeListBuilder b = new InsnNodeListBuilder();

                //[.., materialized]
                b.DUP();
                //[.., materialized, materialized]
                b.GETFIELD(getInstruction.owner, getInstruction.name + "$Ref", LazyReference.class);
                //[.., materialized, ref]
                LabelNode refIsNullLabel = new LabelNode();
                b.IFNULL(refIsNullLabel);
                //[.., materialized]
                b.DUP();
                //[.., materialized, materialized ]
                b.DUP();
                //[.., materialized, materialized, materialized]
                b.GETFIELD(getInstruction.owner, getInstruction.name + "$Ref", LazyReference.class);
                //[.., materialized, materialized, ref]
                b.INVOKEINTERFACE(getMethod);
                //[.., materialized, materialized, value]
                b.CHECKCAST(getInstruction.desc);
                //[.., materialized, materialized, value]
                b.PUTFIELD(getInstruction.owner, getInstruction.name, getInstruction.desc);
                //[.., materialized]
                b.DUP();
                //[.., materialized, materialized]
                b.ACONST_NULL();
                //[.., materialized, materialized, null]
                b.PUTFIELD(getInstruction.owner, getInstruction.name + "$Ref", LazyReference.class);
                //[.., materialized]*/
                b.add(refIsNullLabel);

                modifiedInstructions.insertBefore(getInstruction, b.createInstructions());
            }
        }
    }

    private boolean isPutOnStmEntityField(AbstractInsnNode insnNode) {
        return isFieldInsnNodeOnStmEntityField(insnNode, PUTFIELD);
    }

    private boolean isGetOnStmEntityField(AbstractInsnNode insnNode) {
        return isFieldInsnNodeOnStmEntityField(insnNode, GETFIELD);
    }

    private boolean isFieldInsnNodeOnStmEntityField(AbstractInsnNode insnNode, int opcode) {
        if (insnNode.getOpcode() != opcode) {
            return false;
        }

        FieldInsnNode fieldInsnNode = (FieldInsnNode) insnNode;
        return isTmEntity(fieldInsnNode.desc, classLoader);
    }

    public ClassNode create() {
        return materializedClassNode;
    }
}
