package org.multiverse.instrumentation.javaagent;

import org.multiverse.api.Materializable;
import org.multiverse.api.Originator;
import org.multiverse.instrumentation.javaagent.analysis.AsmFieldAnalyzer;
import static org.multiverse.instrumentation.javaagent.utils.AsmUtils.loadAsClassNode;
import static org.multiverse.instrumentation.javaagent.utils.AsmUtils.toBytecode;
import org.multiverse.instrumentation.utils.InternalFormClassnameUtil;
import org.multiverse.multiversionedstm.DematerializedObject;
import org.multiverse.multiversionedstm.MaterializedObject;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.instrument.IllegalClassFormatException;
import java.util.LinkedList;
import java.util.List;


/**
 * A {@link ClassFileTransformerContext} responsible for transforming the @Materializable
 *
 * @author Peter Veentjer.
 */
public final class MaterializedObjectClassFileTransformerContext implements ClassFileTransformerContext {

    private final static String name = Type.getType(Materializable.class).getDescriptor();

    private final byte[] originalBytecode;
    private final String classname;
    private final AsmFieldAnalyzer fieldAnalyzer;

    public MaterializedObjectClassFileTransformerContext(ClassLoader classLoader, String classname, byte[] originalBytecode) {
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
        if (!isMaterializable(classNode))
            return originalBytecode;

        transform(classNode);
        return toBytecode(classNode);
    }

    private boolean isMaterializable(ClassNode classNode) {
        List<AnnotationNode> annotations = (List<AnnotationNode>) classNode.visibleAnnotations;

        if (annotations == null) {
            return false;
        }

        for (AnnotationNode annotationNode : annotations) {
            if (annotationNode.desc.equals(name)) {
                return true;
            }
        }

        return false;
    }

    private void transform(ClassNode classNode) {
        addInterface(classNode);

        addFields(classNode);

        transformConstructors(classNode);

        addMaterializeConstructor(classNode);

        addIsDirtyMethod(classNode);

        addGetOriginatorMethod(classNode);

        addDematerializeMethod(classNode);

        addGetMaterializedMemberIterator(classNode);
    }

    private void addInterface(ClassNode classNode) {
        classNode.interfaces.add(InternalFormClassnameUtil.toInternalForm(MaterializedObject.class));
    }

    private void addGetMaterializedMemberIterator(ClassNode classNode) {
        //todo
    }

    private void addDematerializeMethod(ClassNode classNode) {
        MethodNode methodNode = new MethodNode(
                Opcodes.ACC_PUBLIC,
                "dematerialize",
                "()" + InternalFormClassnameUtil.toInternalForm(DematerializedObject.class),//
                null,//desc
                null//signature
        );

        //methodNode.instructions.add();

        classNode.methods.add(methodNode);
    }

    private void addGetOriginatorMethod(ClassNode classNode) {
        MethodNode methodNode = new MethodNode(
                Opcodes.ACC_PUBLIC,
                "getOriginator",
                "()" + InternalFormClassnameUtil.toInternalForm(Originator.class),//
                null,//desc
                null//signature
        );

        //methodNode.instructions.add();

        classNode.methods.add(methodNode);
    }

    private void addIsDirtyMethod(ClassNode classNode) {
        MethodNode methodNode = new MethodNode(
                Opcodes.ACC_PUBLIC,
                "isDirty",
                "()Z",//
                null,//desc
                null//signature
        );

        methodNode.instructions.add(new IntInsnNode(Opcodes.ALOAD, 0));
        //methodNode.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, 0));
        //methodNode.instructions.add();
        classNode.methods.add(methodNode);
    }

    private void addMaterializeConstructor(ClassNode classNode) {
        //todo
    }

    private void transformConstructors(ClassNode classNode) {
        //todo
    }

    private void addFields(ClassNode classNode) {
        List<FieldNode> extra = new LinkedList<FieldNode>();
        for (FieldNode fieldNode : (List<FieldNode>) classNode.fields) {
            String desc = fieldNode.desc;
            if (true) {
                FieldNode lazyReferenceFieldNode = new FieldNode(
                        Opcodes.ACC_PROTECTED,//access,
                        fieldNode.name + "LazyRef",//name
                        InternalFormClassnameUtil.toInternalForm(Originator.class),//desc
                        null,//signature
                        null//value
                );
                extra.add(lazyReferenceFieldNode);
            }
        }

        classNode.fields.addAll(extra);

        FieldNode originatorFieldNode = new FieldNode(
                Opcodes.ACC_PROTECTED & Opcodes.ACC_FINAL,//access,
                "originator",//name
                InternalFormClassnameUtil.toInternalForm(Originator.class),//desc
                null,//signature
                null//value
        );
        classNode.fields.add(originatorFieldNode);

        FieldNode lastDematerializedFieldNode = new FieldNode(
                Opcodes.ACC_PROTECTED,//access,
                "lastDematerialized",//name
                null,//desc  todo
                null,//signature
                null//value
        );
        //classNode.fields.add(lastDematerializedFieldNode);
    }
}
