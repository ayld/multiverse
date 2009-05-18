package org.multiverse;

import org.multiverse.instrumentation.utils.AsmUtils;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;

public class Rubish {

    public static void foo(Object item) {
        System.out.println(item.getClass());
    }

    public boolean equals(float l1, float l2) {
        return l1 == l2;
    }

    public static void main(String[] args) throws IOException {
        ClassNode classNode = AsmUtils.loadAsClassNode(Rubish.class);
        AsmUtils.writeToFixedTmpFile(AsmUtils.toBytecode(classNode));
    }
}
