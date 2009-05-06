package org.multiverse.instrumentation.utils;

import org.multiverse.instrumentation.javaagent.utils.AsmUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class BytecodeWriteUtil {

    public static void writeToFixedTmpFile(Class clazz) throws IOException {
        byte[] bytecode = AsmUtils.toBytecode(AsmUtils.loadAsClassNode(clazz));
        writeToFixedTmpFile(bytecode);
    }

    public static void writeToFixedTmpFile(byte[] bytecode) throws IOException {
        File file = new File(getTmpDir(), "debug.class");
        writeToFile(file, bytecode);
    }

    private static String getTmpDir() {
        return System.getProperty("java.io.tmpdir");
    }

    public static File writeToFileInTmpDirectory(String filename, byte[] bytecode) throws IOException {
        File file = new File(getTmpDir(), filename);
        writeToFile(file, bytecode);
        return file;
    }

    public static void writeToTmpFile(byte[] bytecode) throws IOException {
        File file = File.createTempFile("foo", ".class");
        writeToFile(file, bytecode);
    }

    public static void writeToFile(File file, byte[] bytecode) throws IOException {
        if (file == null || bytecode == null) throw new NullPointerException();

        ensureExistingParent(file);

        OutputStream writer = new FileOutputStream(file);
        try {
            writer.write(bytecode);
        } finally {
            writer.close();
        }
    }

    private static void ensureExistingParent(File file) throws IOException {
        File parent = file.getParentFile();
        if (parent.isDirectory())
            return;

        if (!parent.mkdirs())
            throw new IOException("Failed to make parent directories for file " + file);
    }

    //we don't want instances
    private BytecodeWriteUtil() {
    }
}
