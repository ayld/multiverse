package org.multiverse.instrumentation;

import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionTemplate;
import org.multiverse.api.annotations.Atomic;
import static org.multiverse.instrumentation.utils.AsmUtils.hasVisibleAnnotation;
import org.multiverse.instrumentation.utils.ClassNodeBuilder;
import org.multiverse.instrumentation.utils.InstructionsBuilder;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Type.getMethodDescriptor;
import static org.objectweb.asm.Type.getType;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import static java.lang.String.format;
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

        for (MethodNode method : (List<MethodNode>) new LinkedList(classNode.methods)) {
            if (hasVisibleAnnotation(method, Atomic.class)) {
                MethodNode methodDelegate = addAtomicMethodDelegate(method);
                ClassNode templateClass = addNewTemplateClass(method, methodDelegate);
                transform(method, templateClass);
            }
        }
    }

    private ClassNode addNewTemplateClass(MethodNode method, MethodNode atomicMethod) {
        TransactionTemplateClassNodeBuilder innerClassNodeBuilder = new TransactionTemplateClassNodeBuilder(method, atomicMethod);
        ClassNode transactionTemplateClassNode = innerClassNodeBuilder.create();
        registerInnerClass(transactionTemplateClassNode);
        return transactionTemplateClassNode;
    }

    private MethodNode addAtomicMethodDelegate(MethodNode method) {
        MethodNode delegateMethod = new MethodNode();
        delegateMethod.access = method.access;//todo: moet nog synthetic gemaakt worden
        delegateMethod.desc = method.desc;
        delegateMethod.name = method.name + "1";
        //delegateMethod.signature = method.signature;
        delegateMethod.tryCatchBlocks = method.tryCatchBlocks;
        delegateMethod.exceptions = method.exceptions;
        delegateMethod.instructions = method.instructions;

        classNode.methods.add(delegateMethod);
        return delegateMethod;
    }

    private void transform(MethodNode method, ClassNode transactionTemplateClassNode) {
        InstructionsBuilder builder = new InstructionsBuilder();
        Type returnType = Type.getReturnType(method.desc);

        //[..]
        builder.NEW(transactionTemplateClassNode);
        //[.., new]
        builder.DUP();
        //[.., new, new]
        builder.ALOAD(0);
        //[.., new, new, this]
        builder.INVOKESPECIAL(transactionTemplateClassNode, "<init>", format("(L%s;)V", classNode.name));
        //[.., new]
        builder.INVOKEVIRTUAL(transactionTemplateClassNode.name, "execute", "()Ljava/lang/Object;");
        //[.., result]

        if (returnType.getSort() != Type.VOID) {
            builder.CHECKCAST(returnType.getInternalName());
        }

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
        private MethodNode originalMethod;
        private MethodNode delegateMethod;

        TransactionTemplateClassNodeBuilder(MethodNode originalMethod, MethodNode delegateMethod) {
            this.delegateMethod = delegateMethod;
            this.originalMethod = originalMethod;
            this.classNode.version = V1_5;
            //this.classNode.outerClass = AtomicTransformer.this.classNode.name;
            this.classNode.name = AtomicTransformer.this.classNode.name + "TransactionTemplate" + innerClasses.size();

            addPublicFinalSyntheticField("$owner", AtomicTransformer.this.classNode);
            setAccess(ACC_PUBLIC | ACC_FINAL);
            setSuperclass(TransactionTemplate.class);
            addConstructor();
            addExecuteMethod();
        }

        void addConstructor() {
            MethodNode constructor = new MethodNode();

            constructor.name = "<init>";
            constructor.desc = format("(L%s;)V", AtomicTransformer.this.classNode.name);
            constructor.access = ACC_PUBLIC;
            constructor.exceptions = new LinkedList();
            constructor.tryCatchBlocks = new LinkedList();

            InstructionsBuilder i = new InstructionsBuilder();

            //initialize super
            i.ALOAD(0);
            i.INVOKESPECIAL(TransactionTemplate.class, "<init>", "()V");
            //assignment of first member
            i.ALOAD(0);
            i.ALOAD(1);
            i.PUTFIELD(classNode, "$owner", AtomicTransformer.this.classNode);
            //we are done, lets return
            i.RETURN();

            constructor.instructions = i.createInstructions();

            addMethod(constructor);
        }

        void addExecuteMethod() {
            MethodNode executeMethod = new MethodNode();
            executeMethod.name = "execute";
            executeMethod.desc = getMethodDescriptor(getType(Object.class), new Type[]{getType(Transaction.class)});
            executeMethod.access = ACC_PUBLIC;
            executeMethod.exceptions = originalMethod.exceptions;
            executeMethod.tryCatchBlocks = new LinkedList();

            Type returnType = Type.getReturnType(delegateMethod.desc);

            InstructionsBuilder i = new InstructionsBuilder();
            i.ALOAD(0);
            i.GETFIELD(classNode, "$owner", AtomicTransformer.this.classNode);

            //[.., callee]
            i.INVOKEVIRTUAL(AtomicTransformer.this.classNode.name, delegateMethod.name, delegateMethod.desc);
            if (returnType.getSort() == Type.VOID) {
                i.ACONST_NULL();
            }
            //[.., result]
            i.ARETURN();

            executeMethod.instructions = i.createInstructions();
            addMethod(executeMethod);
        }
    }

    public List<ClassNode> getInnerClasses() {
        return innerClasses;
    }

    public ClassNode create() {
        return classNode;
    }
}
