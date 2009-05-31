package org.multiverse.instrumentation;

import static org.multiverse.instrumentation.utils.AsmUtils.toBytecode;
import org.objectweb.asm.tree.ClassNode;

public class MultiverseClassLoader extends TestLoader {

    public static final MultiverseClassLoader INSTANCE = new MultiverseClassLoader();

    public MultiverseClassLoader() {
        super(MultiverseClassLoader.class.getClassLoader());
    }

    public Class defineClass(ClassNode classNode) {
        return this.defineClass(classNode.name.replace("/", "."), toBytecode(classNode));
    }
}