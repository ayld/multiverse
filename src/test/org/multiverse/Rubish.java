package org.multiverse;

import org.multiverse.instrumentation.utils.AsmUtils;
import org.multiverse.multiversionedstm.examples.ExampleStack;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;

public class Rubish {

    public static void foo(Object item) {
        System.out.println(item.getClass());
    }

    public static void main(String[] args) throws IOException {
        ClassNode classNode = AsmUtils.loadAsClassNode(ExampleStack.class);
        AsmUtils.writeToFixedTmpFile(AsmUtils.toBytecode(classNode));
    }
}
