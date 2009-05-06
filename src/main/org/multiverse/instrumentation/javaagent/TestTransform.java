package org.multiverse.instrumentation.javaagent;

import org.multiverse.instrumentation.SimplePair;
import org.multiverse.instrumentation.javaagent.utils.AsmUtils;
import org.multiverse.instrumentation.utils.BytecodeWriteUtil;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;

public class TestTransform {

    public static void main(String[] args) throws IOException {
        DematerializableClassTransformer classCreator = new DematerializableClassTransformer(SimplePair.class);
        ClassNode classNode = classCreator.create();
        AsmUtils.verify(classNode);

        BytecodeWriteUtil.writeToFixedTmpFile(AsmUtils.toBytecode(classNode));
    }
}
