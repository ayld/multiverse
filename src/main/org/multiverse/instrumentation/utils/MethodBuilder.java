package org.multiverse.instrumentation.utils;

import static org.objectweb.asm.Type.getConstructorDescriptor;
import static org.objectweb.asm.Type.getMethodDescriptor;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedList;

public abstract class MethodBuilder extends InstructionsBuilder {

    protected MethodNode methodNode;

    public MethodBuilder() {
        methodNode = new MethodNode();
        setAccess(ACC_PUBLIC | ACC_SYNTHETIC);
        methodNode.exceptions = new LinkedList();
        methodNode.tryCatchBlocks = new LinkedList();
    }

    public MethodBuilder(MethodNode methodNode) {
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
