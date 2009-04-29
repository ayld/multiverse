package org.multiverse;

import org.multiverse.api.*;

public class DummyTransaction implements Transaction {
    @Override
    public void commit() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void abort() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Transaction abortAndRetry() throws InterruptedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <T> T read(Originator<T> originator) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <T> LazyReference<T> readLazy(Originator<T> originator) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <T> LazyReference<T> readLazyAndUnmanaged(Originator<T> originator) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <T> T readUnmanaged(Originator<T> originator) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <T> Originator<T> attach(T obj) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public TransactionId getId() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public TransactionState getState() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setDescription(String description) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getDescription() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
