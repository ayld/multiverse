package org.multiverse.instrumentation;

import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionTemplate;
import org.multiverse.api.TransactionTemplate.InvisibleCheckedException;
import org.multiverse.api.annotations.Atomic;
import static org.multiverse.instrumentation.utils.AsmUtils.*;
import org.multiverse.instrumentation.utils.ClassNodeBuilder;
import org.multiverse.instrumentation.utils.InsnNodeListBuilder;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Type.*;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * Responsible for transforming all classes with @atomic methods so that the transaction logic is added.
 * The actual logic is executed by the {@link TransactionTemplate}.  The instructions of the original
 * method are placed to the delegate method.
 */
public class AtomicClassTransformer implements Opcodes {

    private static final String CALLEE = "callee";

    private static final Method GET_CAUSE_METHOD = getMethod(
            InvisibleCheckedException.class, "getCause");

    private final ClassNode classNode;
    private final List<ClassNode> innerClasses = new LinkedList<ClassNode>();

    public AtomicClassTransformer(ClassNode classNode) {
        this.classNode = classNode;

        //a copy of the list is made, because new methods are added.
        List<MethodNode> originalMethods = new LinkedList<MethodNode>(classNode.methods);

        for (MethodNode originalMethod : originalMethods) {
            if (hasVisibleAnnotation(originalMethod, Atomic.class)) {

                String methodFullname = classNode.name + "." + originalMethod.name + originalMethod.desc;

                //if (isAbstract(originalMethod)) {
                //    System.err.printf("Annotation on method %s is ignored because method is abstract\n",methodFullname);
                //    break;
                //}

                //if (isNative(originalMethod)) {
                //    System.err.printf("Annotation on method %s is ignored because method is native\n",methodFullname);
                //    break;
                //}

                MethodNode delegateMethod = createDelegateMethod(originalMethod);
                ClassNode templateClass = addNewTemplateClass(originalMethod, delegateMethod);
                transformOriginalMethod(originalMethod, templateClass);
            }
        }
    }

    private ClassNode addNewTemplateClass(MethodNode method, MethodNode atomicMethod) {
        TransactionTemplateBuilder innerClassNodeBuilder = new TransactionTemplateBuilder(method, atomicMethod);
        ClassNode transactionTemplateClassNode = innerClassNodeBuilder.create();
        registerInnerClass(transactionTemplateClassNode);
        return transactionTemplateClassNode;
    }

    private MethodNode createDelegateMethod(MethodNode originalMethod) {
        MethodNode delegateMethod = new MethodNode();

        if (isStatic(originalMethod)) {
            delegateMethod.access = ACC_PUBLIC + ACC_SYNTHETIC + ACC_STATIC;
        } else {
            delegateMethod.access = ACC_PUBLIC + ACC_SYNTHETIC;
        }

        delegateMethod.desc = originalMethod.desc;
        delegateMethod.name = originalMethod.name + "$delegate";
        delegateMethod.tryCatchBlocks = originalMethod.tryCatchBlocks;
        delegateMethod.exceptions = originalMethod.exceptions;
        delegateMethod.instructions = originalMethod.instructions;
        classNode.methods.add(delegateMethod);
        return delegateMethod;
    }

    private void transformOriginalMethod(MethodNode originalMethod, ClassNode transactionTemplateClass) {
        InsnNodeListBuilder builder = new InsnNodeListBuilder();

        Type returnType = getReturnType(originalMethod.desc);

        Type[] argTypes = getArgumentTypes(originalMethod.desc);

        //[..]
        builder.NEW(transactionTemplateClass);
        //[.., template]
        builder.DUP();
        //[.., template, template]

        if (!isStatic(originalMethod)) {
            builder.ALOAD(0);
            //[.., template, template, this]
        }

        //place all the method arguments on the heap.
        int loadIndex = isStatic(originalMethod) ? 0 : 1;
        for (int k = 0; k < argTypes.length; k++) {
            Type argType = argTypes[k];
            builder.LOAD(argType, loadIndex);
            loadIndex += argType.getSize();
        }
        //[.., template, template, this?, arg1, arg2, arg3]

        String constructorDescriptor = getConstructorDescriptor(originalMethod);
        builder.INVOKESPECIAL(transactionTemplateClass, "<init>", constructorDescriptor);

        //[.., template]
        builder.INVOKEVIRTUAL(transactionTemplateClass.name, "execute", "()Ljava/lang/Object;");
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
        //    addUnwrapExceptionHandlerIfNeeded(originalMethod);
    }

    public void addUnwrapExceptionHandlerIfNeeded(MethodNode method) {
        LabelNode startTry = new LabelNode();
        LabelNode endTry = new LabelNode();
        LabelNode startHandler = new LabelNode();
        //LabelNode endHandler = new LabelNode();

        Class exceptionClass = InvisibleCheckedException.class;

        //creation of the tryCatch
        TryCatchBlockNode tryCatchLabelNode = new TryCatchBlockNode(
                startTry,
                endTry,
                startHandler,
                getInternalName(exceptionClass));

        method.tryCatchBlocks.add(tryCatchLabelNode);

        //creation of the exceptionvariable in the exception handler.
        //LocalVariableNode exceptionVar = new LocalVariableNode(
        //        "$exception",//name
        //        getDescriptor(exceptionClass),// desc
        //        null,//signature
        //        startHandler,//start scope
        //        endHandler,//end scope
        //        0//index   todo
        //);
        //method.localVariables.add(exceptionVar);

        InsnNodeListBuilder builder = new InsnNodeListBuilder();
        builder.add(startTry);
        builder.add(method.instructions);
        builder.add(endTry);
        builder.add(startHandler);
        builder.ACONST_NULL();
        builder.ARETURN();
        //builder.ASTORE(1);
        //builder.ALOAD(1);
        //builder.INVOKEVIRTUAL(GET_CAUSE_METHOD);
        //builder.ATHROW();
        //builder.add(endHandler);

        method.instructions = builder.createInstructions();
    }

    private String getConstructorDescriptor(MethodNode originalMethod) {
        Type[] constructorArgTypes;
        if (isStatic(originalMethod)) {
            constructorArgTypes = getArgumentTypes(originalMethod.desc);
        } else {
            Type[] methodArgTypes = getArgumentTypes(originalMethod.desc);
            constructorArgTypes = new Type[methodArgTypes.length + 1];

            constructorArgTypes[0] = Type.getObjectType(classNode.name);

            for (int k = 0; k < methodArgTypes.length; k++) {
                constructorArgTypes[k + 1] = methodArgTypes[k];
            }
        }

        return getMethodDescriptor(Type.VOID_TYPE, constructorArgTypes);
    }

    private void registerInnerClass(ClassNode transactionTemplateClassNode) {
        innerClasses.add(transactionTemplateClassNode);
    }

    public class TransactionTemplateBuilder extends ClassNodeBuilder {
        private MethodNode originalMethod;
        private MethodNode delegateMethod;

        TransactionTemplateBuilder(MethodNode originalMethod, MethodNode delegateMethod) {
            this.delegateMethod = delegateMethod;
            this.originalMethod = originalMethod;
            this.classNode.version = V1_5;
            //this.classNode.outerClass = AtomicTransformer.this.classNode.name;
            this.classNode.name = generateClassname();
            setAccess(ACC_PUBLIC | ACC_FINAL);
            setSuperclass(TransactionTemplate.class);

            if (!isStatic(originalMethod)) {
                addPublicFinalSyntheticField(CALLEE, AtomicClassTransformer.this.classNode);
            }

            Type[] argTypes = getArgumentTypes(originalMethod.desc);
            for (int k = 0; k < argTypes.length; k++) {
                Type type = argTypes[k];
                addPublicFinalSyntheticField("arg" + k, type);
            }

            addConstructor();
            addExecuteMethod();
        }

        private String generateClassname() {
            return AtomicClassTransformer.this.classNode.name + "TransactionTemplate" + innerClasses.size();
        }

        void addConstructor() {
            MethodNode constructor = new MethodNode();

            constructor.name = "<init>";
            constructor.desc = getConstructorDescriptor(originalMethod);
            constructor.access = ACC_PUBLIC;
            constructor.exceptions = new LinkedList();
            constructor.tryCatchBlocks = new LinkedList();

            InsnNodeListBuilder builder = new InsnNodeListBuilder();

            //initialize super
            builder.ALOAD(0);
            //[.., this]
            builder.INVOKESPECIAL(TransactionTemplate.class, "<init>", "()V");
            //[..]

            if (!isStatic(originalMethod)) {
                //place the callee on the stack
                builder.ALOAD(0);
                //[.., this]
                builder.ALOAD(1);
                //[.., this, callee]
                builder.PUTFIELD(classNode, CALLEE, AtomicClassTransformer.this.classNode);
                //[..]
            }

            //place the other arguments on the stack.
            Type[] argTypes = getArgumentTypes(originalMethod.desc);
            //the first argument is template and if the method is non static, the callee was placed
            int loadIndex = isStatic(originalMethod) ? 1 : 2;
            for (int k = 0; k < argTypes.length; k++) {
                Type argType = argTypes[k];

                builder.ALOAD(0);
                //[.., this]
                builder.LOAD(argType, loadIndex);
                //[.., this, argk]
                builder.PUTFIELD(classNode, "arg" + k, argType);
                //[..]

                loadIndex += argType.getSize();
            }

            //we are done, lets return
            builder.RETURN();

            constructor.instructions = builder.createInstructions();

            addMethod(constructor);
        }

        void addExecuteMethod() {
            MethodNode executeMethod = new MethodNode();
            executeMethod.name = "execute";
            executeMethod.desc = getMethodDescriptor(getType(Object.class), new Type[]{getType(Transaction.class)});
            executeMethod.access = ACC_PUBLIC;
            executeMethod.exceptions = originalMethod.exceptions;
            executeMethod.tryCatchBlocks = new LinkedList();

            Type returnType = getReturnType(delegateMethod.desc);

            InsnNodeListBuilder builder = new InsnNodeListBuilder();

            if (isStatic(originalMethod)) {
                loadArgumentsOnStack(builder);

                //[..,arg1, arg2, arg3]
                builder.INVOKESTATIC(
                        AtomicClassTransformer.this.classNode.name,
                        delegateMethod.name,
                        delegateMethod.desc
                );
                //[.., result]
            } else {
                //load the callee on the stack
                builder.ALOAD(0);
                builder.GETFIELD(classNode, CALLEE, AtomicClassTransformer.this.classNode);

                //load the rest of the arguments on the stack
                loadArgumentsOnStack(builder);

                //[.., callee, arg1, arg2, arg3]
                builder.INVOKEVIRTUAL(
                        AtomicClassTransformer.this.classNode.name,
                        delegateMethod.name,
                        delegateMethod.desc);
                //[.., result]
            }

            //prepare the result.
            switch (returnType.getSort()) {
                case Type.BOOLEAN:
                case Type.BYTE:
                case Type.CHAR:
                case Type.SHORT:
                case Type.INT:
                    builder.INVOKESTATIC(getMethod(Integer.class, "valueOf", Integer.TYPE));
                    break;
                case Type.LONG:
                    builder.INVOKESTATIC(getMethod(Long.class, "valueOf", Long.TYPE));
                    break;
                case Type.FLOAT:
                    builder.INVOKESTATIC(getMethod(Float.class, "valueOf", Float.TYPE));
                    break;
                case Type.DOUBLE:
                    builder.INVOKESTATIC(getMethod(Double.class, "valueOf", Double.TYPE));
                    break;
                case Type.ARRAY:
                    break;
                case Type.OBJECT:
                    break;
                case Type.VOID:
                    builder.ACONST_NULL();
                    break;
                default:
                    throw new RuntimeException("Unhandled type: " + returnType);
            }

            //[.., result]
            builder.ARETURN();

            executeMethod.instructions = builder.createInstructions();
            addMethod(executeMethod);
        }

        private void loadArgumentsOnStack(InsnNodeListBuilder builder) {
            Type[] argTypes = getArgumentTypes(originalMethod.desc);

            for (int k = 0; k < argTypes.length; k++) {
                builder.ALOAD(0);
                builder.GETFIELD(classNode, "arg" + k, argTypes[k]);
            }
        }
    }

    public List<ClassNode> getInnerClasses() {
        return innerClasses;
    }

    public ClassNode create() {
        return classNode;
    }
}
