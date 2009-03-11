package org.codehaus.multiverse.api;

public class DummyTransaction implements Transaction {
    public long attachAsRoot(Object root) {
        throw new RuntimeException();
    }

    public Object read(long handle) {
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

    public void lockNoWait(long handle, LockMode lockMode) {
        throw new RuntimeException();
    }

    public LockMode readLockMode(long handle) {
        throw new RuntimeException();
    }

    public TransactionId getId() {
        throw new RuntimeException();
    }
}
