package org.multiverse.instrumentation;

import org.multiverse.instrumentation.javaagent.MaterializedObjectClassFileTransformerContext;
import org.multiverse.instrumentation.javaagent.utils.AsmUtils;
import org.multiverse.instrumentation.utils.BytecodeWriteUtil;
import org.multiverse.api.Materializable;

import java.lang.instrument.IllegalClassFormatException;
import java.io.IOException;

public class Main {

    @Materializable
    static class Pair{
        private int a;
        private int b;
    }

    public static void main(String[] args ) throws IllegalClassFormatException, IOException {
        MaterializedObjectClassFileTransformerContext context = new MaterializedObjectClassFileTransformerContext(
                Pair.class.getClassLoader(),
                "Pair",
                AsmUtils.toBytecode(Pair.class.getName()));

        byte[] result = context.transform();
        BytecodeWriteUtil.writeToFileInTmpDirectory("foo.class", result);
    }
}
