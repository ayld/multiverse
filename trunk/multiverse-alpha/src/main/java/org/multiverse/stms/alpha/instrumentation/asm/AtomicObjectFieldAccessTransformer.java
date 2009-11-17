package org.multiverse.stms.alpha.instrumentation.asm;

import static org.multiverse.stms.alpha.instrumentation.asm.AsmUtils.isAbstract;
import static org.multiverse.stms.alpha.instrumentation.asm.AsmUtils.isNative;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.LinkedList;
import java.util.List;

public class AtomicObjectFieldAccessTransformer implements Opcodes {

    private final ClassNode originalObject;

    public AtomicObjectFieldAccessTransformer(ClassNode originalObject) {
        this.originalObject = originalObject;
    }

    public ClassNode transform() {
        fixMethods();
        return originalObject;
    }

    private void fixMethods() {
        List<MethodNode> methods = new LinkedList<MethodNode>();

        for (MethodNode originalMethod : (List<MethodNode>) originalObject.methods) {
            MethodNode fixedMethod = fixMethod(originalMethod);
            methods.add(fixedMethod);
        }

        originalObject.methods = methods;
    }

    private MethodNode fixMethod(MethodNode originalMethod) {
        if (isAbstract(originalMethod) || isNative(originalMethod)) {
            return originalMethod;
        }

        MethodNode fixedMethod = new MethodNode();
        fixedMethod.access = originalMethod.access;
        fixedMethod.localVariables = new LinkedList();
        fixedMethod.name = originalMethod.name;
        fixedMethod.desc = originalMethod.desc;
        fixedMethod.exceptions = originalMethod.exceptions;
        fixedMethod.tryCatchBlocks = new LinkedList();//originalMethod.tryCatchBlocks;

        originalMethod.accept(new AtomicObjectRemappingMethodAdapter(fixedMethod));

        return fixedMethod;
    }
}
