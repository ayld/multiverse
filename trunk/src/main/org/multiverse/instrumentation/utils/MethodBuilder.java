package org.multiverse.instrumentation.utils;

import static org.objectweb.asm.Type.getConstructorDescriptor;
import static org.objectweb.asm.Type.getMethodDescriptor;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedList;

/**
 * A MethodBuilder a Builder responsible for creating {@link MethodNode}.
 *
 * @author Peter Veentjer.
 */
public abstract class MethodBuilder extends InstructionsBuilder {

    protected MethodNode methodNode;

    /**
     * Creates a MethodBuilder with a default MethodNode with a public/synthetic access modifier.
     */
    public MethodBuilder() {
        methodNode = new MethodNode();
        setAccess(ACC_PUBLIC | ACC_SYNTHETIC);
        methodNode.exceptions = new LinkedList();
        methodNode.tryCatchBlocks = new LinkedList();
    }

    /**
     * Creates a MethodBuilder with the provided MethodNode.
     *
     * @param methodNode the MethodNode
     */
    public MethodBuilder(MethodNode methodNode) {
        if (methodNode == null) {
            throw new NullPointerException();
        }
        this.methodNode = methodNode;
    }

    public void setName(String name) {
        methodNode.name = name;
    }

    public void setAccess(int access) {
        methodNode.access = access;
    }

    public void setDescriptor(String desc) {
        methodNode.desc = desc;
    }

    public void initWithConstructor(Constructor constructor) {
        methodNode.access = constructor.getModifiers();
        methodNode.name = constructor.getName();
        methodNode.desc = getConstructorDescriptor(constructor);
    }

    public void initWithInterfaceMethod(Method method) {
        methodNode.name = method.getName();
        methodNode.desc = getMethodDescriptor(method);
    }

    public MethodNode createMethod() {
        methodNode.instructions = createInstructions();
        return methodNode;
    }
}
