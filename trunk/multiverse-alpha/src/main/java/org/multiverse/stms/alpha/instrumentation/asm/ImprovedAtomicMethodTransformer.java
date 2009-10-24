package org.multiverse.stms.alpha.instrumentation.asm;

import org.multiverse.api.Transaction;
import static org.multiverse.stms.alpha.instrumentation.asm.AsmUtils.*;
import org.multiverse.templates.AtomicTemplate;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Type.*;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import static java.lang.String.format;
import java.util.LinkedList;
import java.util.List;

/**
 * Transforms AtomicMethods. The first generation of atomicmethod transformers created an instance
 * of the AtomicTemplate that forwards the call. This transformer 'only' transforms the methods so
 * that the atomictemplate logic is added. This prevents creating another object.
 * <p/>
 * The question is how to transform.
 * The code needs to be placed around the original code. In principle this is no problem (can
 * be done with a try finally clause?). But the question is if this can cause problems with
 * constructors.
 * <p/>
 * If is is placed around the constructor, the constructor will be re-executed on the same
 * atomicobject.
 * <p/>
 * Another reason to drop the template approach is that a lot of boxing/unboxing goes on
 * with primitive return types of the atomicmethod.
 *
 * @author Peter Veentjer.
 */
public class ImprovedAtomicMethodTransformer {

    private static final String CALLEE = "callee";

    private final ClassNode classNode;
    private final List<ClassNode> innerClasses = new LinkedList<ClassNode>();
    private final MetadataRepository metadataService;

    public ImprovedAtomicMethodTransformer(ClassNode classNode) {
        this.classNode = classNode;
        this.metadataService = MetadataRepository.INSTANCE;
    }

    public ClassNode transform() {
        if (!metadataService.hasAtomicMethods(classNode)) {
            return null;
        }

        for (MethodNode atomicMethod : metadataService.getAtomicMethods(classNode)) {
            classNode.methods.remove(atomicMethod);

            MethodNode delegateMethod = createDelegateMethod(atomicMethod);
            classNode.methods.add(delegateMethod);

            ClassNode atomicTemplateClassNode = createNewAtomicTemplateClass(atomicMethod, delegateMethod);
            innerClasses.add(atomicTemplateClassNode);

            MethodNode improvedAtomicMethod = transformAtomicMethod(atomicMethod, atomicTemplateClassNode);
            classNode.methods.add(improvedAtomicMethod);

            if (isPrivate(delegateMethod)) {
                delegateMethod.access = delegateMethod.access - Opcodes.ACC_PRIVATE + Opcodes.ACC_PROTECTED;
            }
        }

        return classNode;
    }

    /**
     * The delegate method executes the original code. This method is called by the
     * AtomicTemplate.execute method.
     *
     * @param atomicMethod
     * @return
     */
    private MethodNode createDelegateMethod(MethodNode atomicMethod) {
        String name = createDelegateMethodName(atomicMethod);

        //todo: synthetic
        MethodNode delegateMethod = new MethodNode(upgradeToPublic(atomicMethod.access), name, atomicMethod.desc, atomicMethod.signature, AsmUtils.getExceptions(atomicMethod));
        atomicMethod.accept(delegateMethod);
        delegateMethod.visitEnd();
        return delegateMethod;
    }

    private String createDelegateMethodName(MethodNode atomicMethod) {
        if (atomicMethod.name.equals("<init>")) {
            return "initdelegate";
        } else if (atomicMethod.name.equals("<clinit")) {
            String msg = format("static initializer in class '%s' can't possibly have an AtomicMethod annotation",
                    atomicMethod.name);
            throw new IllegalArgumentException(msg);
        } else {
            return atomicMethod.name + "delegate";
        }
    }

    /**
     * Transforms the original method so that it creates an atomictemplate, calls the execute
     * (which in turn will call the delegate method) and returns.
     * <p/>
     * The trick is to leave the stack as it is.
     *
     * @param atomicMethod
     * @param transactionTemplateClass
     * @return
     */
    private MethodNode transformAtomicMethod(MethodNode atomicMethod, ClassNode transactionTemplateClass) {
        CodeBuilder cb = new CodeBuilder();

        Type returnType = getReturnType(atomicMethod.desc);

        Type[] argTypes = getArgumentTypes(atomicMethod.desc);

        if (atomicMethod.name.equals("<init>")) {
            int first = findIndexOfFirstInstructionAfterConstructor(classNode.superName, classNode.name, atomicMethod);
            for (int k = 0; k < first; k++) {
                cb.add(atomicMethod.instructions.get(k));
            }
        }

        //[..]
        cb.NEW(transactionTemplateClass);
        //[.., template]
        cb.DUP();
        //[.., template, template]

        if (!isStatic(atomicMethod)) {
            cb.ALOAD(0);
            //[.., template, template, this]
        }

        //place all the method arguments on the stack.
        int loadIndex = isStatic(atomicMethod) ? 0 : 1;
        for (Type argType : argTypes) {
            cb.LOAD(argType, loadIndex);
            loadIndex += argType.getSize();
        }
        //[.., template, template, this?, arg1, arg2, arg3]

        String constructorDescriptor = getConstructorDescriptorForTransactionTemplate(atomicMethod);

        cb.INVOKESPECIAL(transactionTemplateClass, "<init>", constructorDescriptor);

        //[.., template]
        cb.INVOKEVIRTUAL(transactionTemplateClass.name, "executeChecked", "()Ljava/lang/Object;");
        //[.., result]

        switch (returnType.getSort()) {
            case Type.VOID:
                //a null was placed on the stack to deal with a void, so that should be removed
                cb.POP();
                break;
            case Type.ARRAY:
                //fall through
            case Type.OBJECT:
                cb.CHECKCAST(returnType.getInternalName());
                break;
            case Type.BOOLEAN:
                //fall through
            case Type.BYTE:
                //fall through
            case Type.CHAR:
                //fall through
            case Type.SHORT:
                //fall through
            case Type.INT:
                cb.CHECKCAST(Integer.class);
                cb.INVOKEVIRTUAL(getInternalName(Integer.class), "intValue", "()I");
                break;
            case Type.LONG:
                cb.CHECKCAST(Long.class);
                cb.INVOKEVIRTUAL(getInternalName(Long.class), "longValue", "()J");
                break;
            case Type.FLOAT:
                cb.CHECKCAST(Float.class);
                cb.INVOKEVIRTUAL(getInternalName(Float.class), "floatValue", "()F");
                break;
            case Type.DOUBLE:
                cb.CHECKCAST(Double.class);
                cb.INVOKEVIRTUAL(getInternalName(Double.class), "doubleValue", "()D");
                break;
            default:
                throw new RuntimeException("Unhandled type " + returnType);
        }

        cb.RETURN(returnType);

        MethodNode method = new MethodNode();
        method.access = atomicMethod.access;
        method.exceptions = atomicMethod.exceptions;
        method.desc = atomicMethod.desc;
        method.name = atomicMethod.name;
        method.instructions = cb.build();
        method.tryCatchBlocks = new LinkedList();
        return method;
    }

    private String getConstructorDescriptorForTransactionTemplate(MethodNode originalMethod) {
        Type[] constructorArgTypes;
        if (isStatic(originalMethod)) {
            constructorArgTypes = getArgumentTypes(originalMethod.desc);
        } else {
            Type[] methodArgTypes = getArgumentTypes(originalMethod.desc);
            constructorArgTypes = new Type[methodArgTypes.length + 1];

            constructorArgTypes[0] = Type.getObjectType(classNode.name);

            System.arraycopy(methodArgTypes, 0, constructorArgTypes, 1, methodArgTypes.length);
        }

        return getMethodDescriptor(Type.VOID_TYPE, constructorArgTypes);
    }

    private String generateTransactionTemplateClassname() {
        return ImprovedAtomicMethodTransformer.this.classNode.name + "AtomicTemplate" + innerClasses.size();
    }

    private ClassNode createNewAtomicTemplateClass(MethodNode atomicMethod, MethodNode delegateMethod) {
        String transactionTemplateClassName = generateTransactionTemplateClassname();

        ClassBuilder classBuilder = new ClassBuilder();
        classBuilder.setVersion(V1_5);
        //this.classNode.outerClass = AtomicTransformer.this.classNode.name;
        classBuilder.setName(transactionTemplateClassName);
        classBuilder.setAccess(ACC_PUBLIC + ACC_FINAL + ACC_SYNTHETIC);
        classBuilder.setSuperclass(AtomicTemplate.class);

        Type[] argTypes = getArgumentTypes(delegateMethod.desc);
        for (int k = 0; k < argTypes.length; k++) {
            Type type = argTypes[k];
            classBuilder.addPublicFinalSyntheticField("arg" + k, type);
        }

        classBuilder.addMethod(createAtomicTemplateConstructor(atomicMethod, transactionTemplateClassName));
        classBuilder.addMethod(createAtomicTemplateExecuteMethod(delegateMethod, transactionTemplateClassName));

        ClassNode transactionTemplateClassNode = classBuilder.build();
        transactionTemplateClassNode.sourceFile = classNode.sourceFile;
        transactionTemplateClassNode.sourceDebug = classNode.sourceDebug;
        return transactionTemplateClassNode;
    }

    private MethodNode createAtomicTemplateConstructor(MethodNode originalMethod, String transactionTemplateClass) {
        MethodNode constructor = new MethodNode();

        AtomicMethodParams atomicMethodParams = metadataService.getAtomicMethodParams(classNode, originalMethod);

        constructor.name = "<init>";
        constructor.desc = getConstructorDescriptorForTransactionTemplate(originalMethod);
        constructor.access = ACC_PUBLIC + ACC_SYNTHETIC;
        constructor.exceptions = new LinkedList();
        constructor.tryCatchBlocks = new LinkedList();

        CodeBuilder cb = new CodeBuilder();

        //initialize super
        cb.ALOAD(0);
        //[.., this]
        cb.LDC(atomicMethodParams.familyName);
        cb.ICONST(atomicMethodParams.isReadonly);
        cb.LDC(atomicMethodParams.retryCount);
        cb.INVOKESPECIAL(AtomicTemplate.class, "<init>", "(Ljava/lang/String;ZI)V");
        //[..]

        //place the other arguments on the stack.
        Type[] argTypes = getArgumentTypes(originalMethod.desc);
        //the first argument is template and if the method is non static, the callee was placed
        int loadIndex = isStatic(originalMethod) ? 1 : 2;
        for (int k = 0; k < argTypes.length; k++) {
            Type argType = argTypes[k];

            cb.ALOAD(0);
            //[.., this]
            cb.LOAD(argType, loadIndex);
            //[.., this, argk]
            cb.PUTFIELD(transactionTemplateClass, "arg" + k, argType.getDescriptor());
            //[..]

            loadIndex += argType.getSize();
        }

        //we are done, lets return
        cb.RETURN();

        constructor.instructions = cb.build();
        //AsmUtils.addLocalVarTableIfMissing(constructor);
        return constructor;
    }

    /**
     * Receives the call from first method and forwards it to the delegate method.
     *
     * @param delegateMethod
     * @param classname
     * @return
     */
    private MethodNode createAtomicTemplateExecuteMethod(MethodNode delegateMethod, String classname) {
        MethodNode executeMethod = new MethodNode();
        executeMethod.name = "execute";
        executeMethod.desc = getMethodDescriptor(getType(Object.class), new Type[]{getType(Transaction.class)});
        executeMethod.access = ACC_PUBLIC | ACC_SYNTHETIC;
        executeMethod.exceptions = delegateMethod.exceptions;
        executeMethod.tryCatchBlocks = new LinkedList();

        Type returnType = getReturnType(delegateMethod.desc);

        CodeBuilder cb = new CodeBuilder();

        if (isStatic(delegateMethod)) {
            loadArgumentsOnStack(cb, delegateMethod.desc, classname);

            //[..,arg1, arg2, arg3]
            cb.INVOKESTATIC(
                    classNode.name,
                    delegateMethod.name,
                    delegateMethod.desc
            );
            //[.., result]
        } else {
            //load the callee on the stack
            cb.ALOAD(0);
            cb.GETFIELD(classname, CALLEE, classNode);

            //load the rest of the arguments on the stack
            loadArgumentsOnStack(cb, delegateMethod.desc, classname);

            //[.., callee, arg1, arg2, arg3]
            cb.INVOKEVIRTUAL(
                    classNode.name,
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
                cb.INVOKESTATIC(getMethod(Integer.class, "valueOf", Integer.TYPE));
                break;
            case Type.LONG:
                cb.INVOKESTATIC(getMethod(Long.class, "valueOf", Long.TYPE));
                break;
            case Type.FLOAT:
                cb.INVOKESTATIC(getMethod(Float.class, "valueOf", Float.TYPE));
                break;
            case Type.DOUBLE:
                cb.INVOKESTATIC(getMethod(Double.class, "valueOf", Double.TYPE));
                break;
            case Type.ARRAY:
                break;
            case Type.OBJECT:
                break;
            case Type.VOID:
                cb.ACONST_NULL();
                break;
            default:
                throw new RuntimeException("Unhandled type: " + returnType);
        }

        //[.., result]
        cb.ARETURN();

        executeMethod.instructions = cb.build();
        return executeMethod;
    }

    private void loadArgumentsOnStack(CodeBuilder cb, String methodDescriptor, String classname) {
        Type[] argTypes = getArgumentTypes(methodDescriptor);

        for (int k = 0; k < argTypes.length; k++) {
            cb.ALOAD(0);
            cb.GETFIELD(classname, "arg" + k, argTypes[k].getDescriptor());
        }
    }

    public List<ClassNode> getInnerClasses() {
        return innerClasses;
    }

    public ClassNode create() {
        return classNode;
    }
}
