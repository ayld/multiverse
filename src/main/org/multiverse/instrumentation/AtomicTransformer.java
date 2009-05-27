package org.multiverse.instrumentation;

import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionTemplate;
import org.multiverse.api.annotations.Atomic;
import static org.multiverse.instrumentation.utils.AsmUtils.getMethod;
import static org.multiverse.instrumentation.utils.AsmUtils.hasVisibleAnnotation;
import org.multiverse.instrumentation.utils.ClassNodeBuilder;
import org.multiverse.instrumentation.utils.InstructionsBuilder;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Type.*;
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

        List<MethodNode> originalMethods = new LinkedList<MethodNode>(classNode.methods);
        for (MethodNode method : originalMethods) {
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
        delegateMethod.access = ACC_PUBLIC + ACC_SYNTHETIC;
        delegateMethod.desc = method.desc;
        delegateMethod.name = method.name + "$delegate";
        delegateMethod.tryCatchBlocks = method.tryCatchBlocks;
        delegateMethod.exceptions = method.exceptions;
        delegateMethod.instructions = method.instructions;

        classNode.methods.add(delegateMethod);
        return delegateMethod;
    }

    private void transform(MethodNode originalMethod, ClassNode transactionTemplateClassNode) {
        InstructionsBuilder builder = new InstructionsBuilder();

        Type returnType = getReturnType(originalMethod.desc);

        Type[] argTypes = getArgumentTypes(originalMethod.desc);

        //[..]
        builder.NEW(transactionTemplateClassNode);
        //[.., template]
        builder.DUP();
        //[.., template, template]
        builder.ALOAD(0);
        //[.., template, template, this]

        //place all the method arguments on the heap.
        int loadIndex = 1;
        for (int k = 0; k < argTypes.length; k++) {
            Type argType = argTypes[k];
            builder.LOAD(argType, loadIndex);
            loadIndex += argType.getSize();
        }
        //[.., template, template, this, arg1, arg2, arg3]

        String constructorDescriptor = getConstructorDescriptor(originalMethod);
        builder.INVOKESPECIAL(transactionTemplateClassNode, "<init>", constructorDescriptor);

        //[.., template]
        builder.INVOKEVIRTUAL(transactionTemplateClassNode.name, "execute", "()Ljava/lang/Object;");
        //[.., result]

        switch (returnType.getSort()) {
            case Type.VOID:
                //a null was placed on the stack to deal with a void, so that should be removed
                builder.POP();
                break;
            case Type.ARRAY:
            case Type.OBJECT:
                builder.CHECKCAST(returnType.getInternalName());
                break;
            case Type.BOOLEAN:
            case Type.BYTE:
            case Type.CHAR:
            case Type.SHORT:
            case Type.INT:
                builder.CHECKCAST(Integer.class);
                builder.INVOKEVIRTUAL(getInternalName(Integer.class), "intValue", "()I");
                break;
            case Type.LONG:
                builder.CHECKCAST(Long.class);
                builder.INVOKEVIRTUAL(getInternalName(Long.class), "longValue", "()J");
                break;
            case Type.FLOAT:
                builder.CHECKCAST(Float.class);
                builder.INVOKEVIRTUAL(getInternalName(Float.class), "floatValue", "()F");
                break;
            case Type.DOUBLE:
                builder.CHECKCAST(Double.class);
                builder.INVOKEVIRTUAL(getInternalName(Double.class), "doubleValue", "()D");
                break;
            default:
                throw new RuntimeException("Unhandled type " + returnType);
        }

        builder.RETURN(returnType);
        originalMethod.instructions = builder.createInstructions();
    }

    private String getConstructorDescriptor(MethodNode originalMethod) {
        Type[] methodArgTypes = getArgumentTypes(originalMethod.desc);
        Type[] constructorArgTypes = new Type[methodArgTypes.length + 1];

        constructorArgTypes[0] = Type.getObjectType(classNode.name);
        for (int k = 0; k < methodArgTypes.length; k++) {
            constructorArgTypes[k + 1] = methodArgTypes[k];
        }

        return getMethodDescriptor(Type.VOID_TYPE, constructorArgTypes);
    }

    private void registerInnerClass(ClassNode transactionTemplateClassNode) {
        innerClasses.add(transactionTemplateClassNode);
    }

    public class TransactionTemplateClassNodeBuilder extends ClassNodeBuilder {
        private MethodNode originalMethod;
        private MethodNode delegateMethod;

        TransactionTemplateClassNodeBuilder(MethodNode originalMethod, MethodNode delegateMethod) {
            this.delegateMethod = delegateMethod;
            this.originalMethod = originalMethod;
            this.classNode.version = V1_5;
            //this.classNode.outerClass = AtomicTransformer.this.classNode.name;
            this.classNode.name = generateClassname();
            setAccess(ACC_PUBLIC | ACC_FINAL);
            setSuperclass(TransactionTemplate.class);

            addPublicFinalSyntheticField("owner", AtomicTransformer.this.classNode);

            Type[] argTypes = getArgumentTypes(originalMethod.desc);
            for (int k = 0; k < argTypes.length; k++) {
                Type type = argTypes[k];
                addPublicFinalSyntheticField("arg" + k, type);
            }

            addConstructor();
            addExecuteMethod();
        }

        private String generateClassname() {
            return AtomicTransformer.this.classNode.name + "TransactionTemplate" + innerClasses.size();
        }

        void addConstructor() {
            MethodNode constructor = new MethodNode();

            constructor.name = "<init>";
            constructor.desc = getConstructorDescriptor(originalMethod);
            System.out.println("constructor.desc: " + constructor.desc);
            constructor.access = ACC_PUBLIC;
            constructor.exceptions = new LinkedList();
            constructor.tryCatchBlocks = new LinkedList();

            InstructionsBuilder i = new InstructionsBuilder();

            //initialize super
            i.ALOAD(0);
            //[.., this]
            i.INVOKESPECIAL(TransactionTemplate.class, "<init>", "()V");
            //[..]

            //place the owner on the stack
            i.ALOAD(0);
            //[.., this]
            i.ALOAD(1);
            //[.., this, owner]
            i.PUTFIELD(classNode, "owner", AtomicTransformer.this.classNode);
            //[..]

            //place the other arguments on the stack.
            Type[] argTypes = getArgumentTypes(originalMethod.desc);
            //the first argument is this, the second argument is the 'owner'. So to get real arguments,
            //we need to jump to the third argument (so the one with loadIndex = 2).
            int loadIndex = 2;
            for (int k = 0; k < argTypes.length; k++) {
                Type argType = argTypes[k];

                i.ALOAD(0);
                //[.., this]
                i.LOAD(argType, loadIndex);
                //[.., this, argk]
                i.PUTFIELD(classNode, "arg" + k, argType);
                //[..]

                loadIndex += argType.getSize();
            }

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
            i.GETFIELD(classNode, "owner", AtomicTransformer.this.classNode);

            Type[] argTypes = getArgumentTypes(originalMethod.desc);
            for (int k = 0; k < argTypes.length; k++) {
                i.ALOAD(0);
                i.GETFIELD(classNode, "arg" + k, argTypes[k]);
            }

            //[.., owner, arg0, arg1, arg2]
            i.INVOKEVIRTUAL(AtomicTransformer.this.classNode.name, delegateMethod.name, delegateMethod.desc);

            switch (returnType.getSort()) {
                case Type.BOOLEAN:
                case Type.BYTE:
                case Type.CHAR:
                case Type.SHORT:
                case Type.INT:
                    i.INVOKESTATIC(getMethod(Integer.class, "valueOf", Integer.TYPE));
                    break;
                case Type.LONG:
                    i.INVOKESTATIC(getMethod(Long.class, "valueOf", Long.TYPE));
                    break;
                case Type.FLOAT:
                    i.INVOKESTATIC(getMethod(Float.class, "valueOf", Float.TYPE));
                    break;
                case Type.DOUBLE:
                    i.INVOKESTATIC(getMethod(Double.class, "valueOf", Double.TYPE));
                    break;
                case Type.ARRAY:
                    break;
                case Type.OBJECT:
                    break;
                case Type.VOID:
                    i.ACONST_NULL();
                    break;
                default:
                    throw new RuntimeException("Unhandled type: " + returnType);
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
