package org.codehaus.multiverse.api;

public class DummyTransaction implements Transaction {

    @Override
    public long attachAsRoot(Object root) {
        throw new RuntimeException();
    }

    @Override
    public Object read(long handle) {
        throw new RuntimeException();
    }

    @Override
    public TransactionStatus getStatus() {
        throw new RuntimeException();
    }

    @Override
    public void commit() {
        throw new RuntimeException();
    }

    @Override
    public void abort() {
        throw new RuntimeException();
    }

    @Override
    public TransactionId getId() {
        throw new RuntimeException();
    }

    @Override
    public Object readAndLockOrFail(long handle, LockMode lockMode) {
        throw new RuntimeException();
    }

    @Override
    public Object readAndLockOrBlock(long handle, LockMode lockMode) {
        throw new RuntimeException();
    }

    @Override
    public PessimisticLock getPessimisticLock(Object object) {
        throw new RuntimeException();
    }
}
