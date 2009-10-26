package org.multiverse;

import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionStatus;

/**
 * @author Peter Veentjer
 */
public class DummyTransaction implements Transaction {

    @Override
    public String getFamilyName() {
        throw new RuntimeException();
    }

    @Override
    public void deferredExecute(Runnable r) {
        throw new RuntimeException();
    }

    @Override
    public void startOr() {
        throw new RuntimeException();
    }

    @Override
    public Transaction restart() {
        throw new RuntimeException();
    }

    @Override
    public void endOr() {
        throw new RuntimeException();
    }

    @Override
    public void endOrAndStartElse() {
        throw new RuntimeException();
    }

    @Override
    public long getReadVersion() {
        throw new RuntimeException();
    }

    @Override
    public TransactionStatus getStatus() {
        throw new RuntimeException();
    }

    @Override
    public long commit() {
        throw new RuntimeException();
    }

    @Override
    public void abort() {
        throw new RuntimeException();
    }

    @Override
    public void abortAndWaitForRetry() {
        throw new RuntimeException();
    }

    @Override
    public void compensatingExecute(Runnable task) {
        throw new RuntimeException();
    }
}
