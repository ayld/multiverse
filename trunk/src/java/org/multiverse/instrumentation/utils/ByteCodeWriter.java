package org.multiverse.instrumentation.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class ByteCodeWriter {

    public static void writeToFixedTmpFile(byte[] bytecode) throws IOException {
        File file = new File(System.getProperty("java.io.tmpdir"), "debug.class");
        writeToFile(file, bytecode);
    }

    public static void writeToTmpFile(byte[] bytecode) throws IOException {
        File file = File.createTempFile("foo", ".class");
        writeToFile(file, bytecode);
    }

    public static void writeToFile(File file, byte[] bytecode) throws IOException {
        if(file == null || bytecode == null)throw new NullPointerException();
        OutputStream writer = new FileOutputStream(file);
        try {
            writer.write(bytecode);
        } finally {
            writer.close();
        }
    }

    //we don't want instances
    private ByteCodeWriter(){}
}
