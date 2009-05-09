package org.multiverse.instrumentation;

import static org.multiverse.instrumentation.utils.AsmUtils.toBytecode;
import org.objectweb.asm.tree.ClassNode;

public class MultiverseClassLoader extends TestLoader {

    public static MultiverseClassLoader INSTANCE;

    public MultiverseClassLoader() {
        super(MultiverseClassLoader.class.getClassLoader());
        INSTANCE = this;
    }

    public Class defineClass(ClassNode classNode) {
        return this.defineClass(classNode.name.replace("/", "."), toBytecode(classNode));
    }
}