package org.multiverse.instrumentation.javaagent;

import org.multiverse.instrumentation.javaagent.analysis.AsmFieldAnalyzer;
import org.multiverse.instrumentation.javaagent.analysis.FieldDescription;
import static org.multiverse.instrumentation.javaagent.utils.AsmUtils.loadAsClassNode;
import static org.multiverse.instrumentation.javaagent.utils.AsmUtils.toBytecode;
import org.multiverse.instrumentation.javaagent.utils.FieldInsnNodeVisitor;
import org.multiverse.instrumentation.utils.InternalFormFieldnameUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.instrument.IllegalClassFormatException;
import java.util.List;

public final class AccessObjectClassFileTransformerContext implements ClassFileTransformerContext {

    private final byte[] originalBytecode;
    private final String classname;
    private final AsmFieldAnalyzer fieldAnalyzer;

    /**
     * @param classLoader
     * @param classname
     * @param originalBytecode
     * @throws NullPointerException if classLoader or classname or originalByteCode concurrencyDetectionPolicy is null.
     */
    public AccessObjectClassFileTransformerContext(ClassLoader classLoader, String classname, byte[] originalBytecode) {
        if (classLoader == null || classname == null || originalBytecode == null)
            throw new NullPointerException();
        this.classname = classname;
        this.originalBytecode = originalBytecode;
        this.fieldAnalyzer = new AsmFieldAnalyzer(classLoader);
    }

    public byte[] transform() throws IllegalClassFormatException {
        System.out.println("Instance FieldAccess Transforming: " + classname);

        try {
            return transform(originalBytecode);
        } catch (IllegalStateException e) {
            throw new IllegalClassFormatException("Error: " + e.getMessage() + " while transforming class " + classname);
        }
    }

    public byte[] transform(byte[] originalBytecode) {
        ClassNode classNode = loadAsClassNode(originalBytecode);
        transform(classNode);
        return toBytecode(classNode);
    }

    private void transform(ClassNode classNode) {
        for (FieldNode field : (List<FieldNode>) classNode.fields) {
            //instrument(field);
        }
    }

    private void instrument(MethodNode method) {
        //InsnList instructions = method.instructions;
        //LabelNode beginScopeNode = new LabelNode();
        //LabelNode endScopeNode = new LabelNode();

        ////places the beginScopeNode before all other instructions
        //instructions.insertBefore(instructions.getFirst(), beginScopeNode);
        //places the endScopeNode after all other instructions
        //instructions.add(endScopeNode);

        //instrument all fields
        //applyAndInsertBeforeEachField(method.instructions, new MethodTransformer(recordingsVar));
    }

    /**
     * Checks if this method contains PUT_FIELD, GET_FIELD instructions we want to listen to. This is a
     * check to prevent unrequired transformations.
     *
     * @param method the method to check
     * @return true if the method needs instrumentation, false otherwise.
     */
    private boolean methodNeedsInstrumentation(MethodNode method) {
        FieldInsnNodeVisitor v = new FieldInsnNodeVisitor<Boolean>() {
            public Boolean visit(FieldInsnNode node) {
                String fieldpath = InternalFormFieldnameUtil.toInternalForm(node.owner, node.name);

                FieldDescription fieldDescription = fieldAnalyzer.find(fieldpath);
                if (fieldDescription == null)
                    return false;

                if (fieldDescription.isStatic())
                    return false;

                // return concurrencyDetectionPolicy.fieldNeedsInstrumentation(fieldDescription);
                throw new RuntimeException();
            }
        };

        //return applyAndStopAfterFirstSuccess(method.instructions, v);
        throw new RuntimeException();
    }


    private class MethodTransformer implements FieldInsnNodeVisitor<InsnList> {
        private final LocalVariableNode recordingsVar;

        public MethodTransformer(LocalVariableNode recordingsVar) {
            this.recordingsVar = recordingsVar;
        }

        public InsnList visit(FieldInsnNode fieldNode) {
            String fieldname = InternalFormFieldnameUtil.toInternalForm(fieldNode.owner, fieldNode.name);

            FieldDescription fieldDescription = fieldAnalyzer.find(fieldname);
            if (fieldDescription == null) {
                //if no information about the fieldDescription is found, no instrumentation is done.
                System.out.println("WARNING: No fieldDescription found for fieldname: " + fieldname);
                return new InsnList();
            }

            switch (fieldNode.getOpcode()) {
                case Opcodes.GETFIELD:
                    if (!fieldDescription.needsInstrumentation())
                        return new InsnList();
                    return codeForGetFieldRegistration(fieldNode, fieldDescription);
                case Opcodes.PUTFIELD:
                    if (!fieldDescription.needsInstrumentation())
                        return new InsnList();
                    return codeForPutFieldRegistration(fieldNode, fieldDescription);
                default:
                    return new InsnList();
            }
        }

        private InsnList codeForGetFieldRegistration(FieldInsnNode node, FieldDescription fieldDescription) {
            //de stack ziet er als volgt uit [target, ...]
            InsnList result = new InsnList();

            //push de target nog een keer op de stack
            //de stack ziet hierna als volgt er uit [target, target, ...]
            result.add(new InsnNode(Opcodes.DUP));

            //pushes the fieldLevelThreadRecordings on the stack
            //de stack ziet hierna als volgt er uit[fieldrecordingsregistry, target, target, ...]
            result.add(new IntInsnNode(Opcodes.ALOAD, recordingsVar.index));

            //swap zodat de target weer boven op staat en de threadrecordings daaronder.
            //de stack ziet er nu als volgt uit [target, fieldrecordingsregistry, target, ...]
            //de swap mag uitgevoerd worden omdat de bovenste 2 elementen op de stack van de 1e categorie zijn
            result.add(new InsnNode(Opcodes.SWAP));

            //pushes the name of the fieldDescription on the stack
            //de stack ziet er hierna als volgt uit [name_of_field, target, fieldrecordingsregistry, target, ....
            result.add(new LdcInsnNode(fieldDescription.toInternalForm()));

            //roept de fieldrecordingsregistry.recordGetField(target, name_of_field) aan
            //de stack ziet er hierna als volgt uit [target,... ]
            result.add(new MethodInsnNode(
                    Opcodes.INVOKEVIRTUAL,
                    "org/codehaus/concurrencydetector/applications/instancefieldaccess/InstanceFieldAccessThreadRecordings",//todo: magic
                    "recordGetField",
                    "(Ljava/lang/Object;Ljava/lang/String;)V")
            );

            return result;
        }

        private InsnList codeForPutFieldRegistration(FieldInsnNode node, FieldDescription fieldDescription) {
            //de stack ziet er als volgt uit [value, target, ...]

            InsnList result = new InsnList();

            if (fieldDescription.hasSecondCategoryType()) {
                //de stack ziet er als volgt uit [value64bits, target, ...]

                //stack hierna  [value64bits, target, value64bits,...]
                result.add(new InsnNode(Opcodes.DUP2_X1));

                //stack hierna [target, value64bits, ...]
                result.add(new InsnNode(Opcodes.POP2));

                //de stack hierna [target, value64bits, target, ...]
                result.add(new InsnNode(Opcodes.DUP_X2));
            } else {
                //clones the 2 top element of the stack and pushes them on the stack
                //stack hierna: [value, target, value, target, .....]
                result.add(new InsnNode(Opcodes.DUP2));

                //removes the topelement of the stack
                //stack hierna: [target, value, target, ...]
                result.add(new InsnNode(Opcodes.POP));
            }

            //pushes the fieldLevelThreadRecordings on the stack
            //stack hierna: [recordingsvar, target, value, target, ...]
            result.add(new IntInsnNode(Opcodes.ALOAD, recordingsVar.index));

            //swap zodat de target weer boven op staat en de threadrecordings daaronder.
            //de swap mag uitgevoerd worden omdat de bovenste 2 elementen op de stack van de 1e categorie zijn
            //stack hierna: [target, recordingsvar, value, target, ...]
            result.add(new InsnNode(Opcodes.SWAP));

            //pushes the name of the fieldDescription on the stack
            //de stack hierna [name, target, threadrecordingsregistry, value, target, ...]
            result.add(new LdcInsnNode(fieldDescription.toInternalForm()));

            //calls the SignalUtil.recordPutField method.
            //de stack hierna [value, target, ...]
            result.add(new MethodInsnNode(
                    Opcodes.INVOKEVIRTUAL,
                    "org/codehaus/concurrencydetector/applications/instancefieldaccess/InstanceFieldAccessThreadRecordings",//todo: magic
                    "recordPutField",
                    "(Ljava/lang/Object;Ljava/lang/String;)V")
            );

            return result;
        }
    }
}
