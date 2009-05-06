package org.multiverse;

import org.multiverse.api.*;

public class DummyTransaction implements Transaction {

    @Override
    public void commit() {
    }

    @Override
    public void abort() {
    }

    @Override
    public Transaction abortAndRetry() throws InterruptedException {
        return null;
    }

    @Override
    public <T> T read(Handle<T> handle) {
        return null;
    }

    @Override
    public <T> LazyReference<T> readLazy(Handle<T> handle) {
        return null;
    }

    @Override
    public <T> LazyReference<T> readLazyAndUnmanaged(Handle<T> handle) {
        return null;
    }

    @Override
    public <T> T readUnmanaged(Handle<T> handle) {
        return null;
    }

    @Override
    public <T> Handle<T> attach(T obj) {
        return null;
    }

    @Override
    public TransactionId getId() {
        return null;
    }

    @Override
    public TransactionState getState() {
        return null;
    }

    @Override
    public void setDescription(String description) {
    }

    @Override
    public String getDescription() {
        return null;
    }
}
