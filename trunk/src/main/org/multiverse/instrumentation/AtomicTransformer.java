package org.multiverse.instrumentation;

import org.multiverse.SharedStmInstance;
import org.multiverse.api.Stm;
import org.multiverse.api.TransactionTemplate;
import org.multiverse.api.annotations.Atomic;
import static org.multiverse.instrumentation.utils.AsmUtils.*;
import org.multiverse.instrumentation.utils.ClassNodeBuilder;
import org.multiverse.instrumentation.utils.InstructionsBuilder;
import static org.multiverse.instrumentation.utils.InternalFormClassnameUtil.getPackagename;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.LinkedList;
import java.util.List;

/**
 * Responsible for transforming all classes with @atomic methods so that the logic is added.
 */
public class AtomicTransformer implements Opcodes {
    private final ClassNode classNode;
    private final ClassLoader classLoader;
    private final List<ClassNode> innerClasses = new LinkedList<ClassNode>();

    public AtomicTransformer(ClassNode classNode, ClassLoader classLoader) {
        this.classNode = classNode;
        this.classLoader = classLoader;

        for (MethodNode method : (List<MethodNode>) classNode.methods) {
            if (hasVisibleAnnotation(method, Atomic.class)) {
                transform(method);
            }
        }
    }

    /**
     * 0 new #9 <org/multiverse/instrumentation/AtomicTransformer$1>
     * 3 dup
     * 4 aload_0
     * 5 invokestatic #73 <org/multiverse/SharedStmInstance.getInstance>
     * 8 invokespecial #76 <org/multiverse/instrumentation/AtomicTransformer$1.<init>>
     * 11 invokevirtual #79 <org/multiverse/instrumentation/AtomicTransformer$1.execute>
     * 14 areturn
     *
     * @param method
     */

    private void transform(MethodNode method) {
        TransactionTemplateClassNodeBuilder innerClassNodeBuilder = new TransactionTemplateClassNodeBuilder(method);

        ClassNode transactionTemplateClassNode = innerClassNodeBuilder.create();
        registerInnerClass(transactionTemplateClassNode);

        InstructionsBuilder builder = new InstructionsBuilder();

        builder.NEW(transactionTemplateClassNode);
        //[.., new]
        builder.DUP();
        //[.., new, new]
        builder.INVOKESTATIC(getMethod(SharedStmInstance.class, "getInstance"));
        //[.., new, new, stm]
        //builder.INVOKESPECIAL(transactionTemplateClassNode, "<init>", "()V");
        //[.., new]
        builder.POP();
        builder.POP();
        builder.POP();

        //builder.INVOKEVIRTUAL(transactionTemplateClassNode.name, "execute", "()V");

        builder.ACONST_NULL();
        builder.RETURN(OpcodeForType.RETURN(method));
        method.instructions = builder.createInstructions();
    }

    private void registerInnerClass(ClassNode transactionTemplateClassNode) {
        innerClasses.add(transactionTemplateClassNode);

        //    InnerClassNode innerClassNode = new InnerClassNode(
        //            transactionTemplateClassNode.name,
        //            classNode.name,
        //            "banana",
        //            ACC_STATIC | ACC_PUBLIC);
        //
        //    classNode.innerClasses.add(innerClassNode);
    }

    public class TransactionTemplateClassNodeBuilder extends ClassNodeBuilder {
        //the methodNode which needs an inner class of the TransactionTemplate.
        private MethodNode methodNode;

        TransactionTemplateClassNodeBuilder(MethodNode methodNode) {
            this.methodNode = methodNode;
            this.classNode.version = V1_5;
            //this.classNode.outerClass = AtomicTransformer.this.classNode.name;
            this.classNode.name = getPackagename(AtomicTransformer.this.classNode) + "/TransactionTemplate";

            setAccess(ACC_PUBLIC | ACC_FINAL);
            setSuperclass(TransactionTemplate.class);
            addConstructor();
            //addExecuteMethod();

            //addFields();
            //setmethodNode.instructions;
        }

        void addConstructor() {
            MethodNode constructor = new MethodNode();

            constructor.name = "<init>";
            constructor.desc = getVoidMethodDescriptor(Type.getType(Stm.class));
            constructor.access = ACC_PUBLIC;
            constructor.exceptions = new LinkedList();
            constructor.tryCatchBlocks = new LinkedList();

            InstructionsBuilder i = new InstructionsBuilder();
            i.ALOAD(0);
            i.ALOAD(1);
            i.INVOKESPECIAL(TransactionTemplate.class, "<init>", "()V");
            i.RETURN();
            constructor.instructions = i.createInstructions();

            addMethod(constructor);
        }

        void addExecuteMethod() {
            MethodNode newMethod = new MethodNode();
            newMethod.exceptions = methodNode.exceptions;
            newMethod.instructions = methodNode.instructions;
            addMethod(newMethod);
        }
    }

    public List<ClassNode> getInnerClasses() {
        return innerClasses;
    }

    public ClassNode create() {
        return classNode;
    }
}
