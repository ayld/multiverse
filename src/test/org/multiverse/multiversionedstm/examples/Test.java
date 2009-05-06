package org.multiverse.multiversionedstm.examples;

import org.multiverse.instrumentation.javaagent.utils.AsmUtils;
import org.multiverse.instrumentation.utils.BytecodeWriteUtil;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;

public class Test {

    public static void main(String[] args) throws IOException {
        ClassNode classNode = AsmUtils.loadAsClassNode(IntegerValue.class);
        BytecodeWriteUtil.writeToFixedTmpFile(AsmUtils.toBytecode(classNode));
    }

}
