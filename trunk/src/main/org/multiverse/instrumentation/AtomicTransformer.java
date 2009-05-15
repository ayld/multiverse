package org.multiverse.instrumentation;

import org.multiverse.api.Atomic;
import static org.multiverse.instrumentation.utils.AsmUtils.hasVisibleAnnotation;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

/**
 * Responsible for transforming all classes with @atomic methods so that the logic is added.
 */
public class AtomicTransformer implements Opcodes {
    private final ClassNode classNode;
    private final ClassLoader classLoader;

    public AtomicTransformer(ClassNode classNode, ClassLoader classLoader) {
        this.classNode = classNode;
        this.classLoader = classLoader;

        for (MethodNode method : (List<MethodNode>) classNode.methods) {
            if (hasVisibleAnnotation(method, Atomic.class)) {
                transform(method);
            }
        }
    }

    private void transform(MethodNode method) {
        //To change body of created methods use File | Settings | File Templates.
    }

    public ClassNode create() {
        return classNode;
    }
}
