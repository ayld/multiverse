package org.multiverse.instrumentation;

import org.multiverse.api.Dematerializable;
import org.multiverse.instrumentation.javaagent.MaterializedObjectClassFileTransformerContext;
import org.multiverse.instrumentation.javaagent.utils.AsmUtils;
import static org.multiverse.instrumentation.utils.BytecodeWriteUtil.writeToFileInTmpDirectory;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;

public class Main {

    @Dematerializable
    static class Pair {
        private int a;
        private int b;

    }


    public static void main(String[] args) throws IllegalClassFormatException, IOException {
        MaterializedObjectClassFileTransformerContext context = new MaterializedObjectClassFileTransformerContext(
                Pair.class.getClassLoader(),
                "Pair",
                AsmUtils.toBytecode(Pair.class.getName()));

        byte[] result = context.transform();
        File file = writeToFileInTmpDirectory("foo.class", result);
        AsmUtils.verify(file);
    }
}
