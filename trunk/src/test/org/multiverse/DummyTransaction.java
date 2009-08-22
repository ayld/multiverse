package org.multiverse;

import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionStatus;
import org.multiverse.api.locks.LockManager;

/**
 * @author Peter Veentjer
 */
public class DummyTransaction implements Transaction {

    @Override
    public void executePostCommit(Runnable r) {
        throw new RuntimeException();
    }

    @Override
    public LockManager getLockManager() {
        throw new RuntimeException();
    }

    @Override
    public void startOr() {
        throw new RuntimeException();
    }

    @Override
    public void reset() {
        throw new RuntimeException();
    }

    @Override
    public void retry() {
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
    public void abortAndRetry() {
        throw new RuntimeException();
    }
}
