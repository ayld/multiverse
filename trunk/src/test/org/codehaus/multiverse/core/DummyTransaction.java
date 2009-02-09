package org.codehaus.multiverse.core;

public class DummyTransaction implements Transaction {
    public long attachAsRoot(Object root) {
        throw new RuntimeException();
    }

    public Object read(long handle) {
        throw new RuntimeException();
    }

    public Object readAndRegisterAsConditionVariable(long handle) {
        throw new RuntimeException();
    }

    public TransactionStatus getStatus() {
        throw new RuntimeException();
    }

    public void commit() {
        throw new RuntimeException();
    }

    public void abort() {
        throw new RuntimeException();
    }
}
