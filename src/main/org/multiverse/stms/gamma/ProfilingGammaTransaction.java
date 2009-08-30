package org.multiverse.stms.gamma;

import org.multiverse.api.TransactionStatus;
import org.multiverse.api.locks.LockManager;
import org.multiverse.utils.TodoException;
import org.multiverse.utils.profiling.Profiler;

public class ProfilingGammaTransaction implements GammaTransaction {

    private final GammaTransaction transaction;
    private final Profiler profiler;

    public ProfilingGammaTransaction(GammaTransaction transaction, Profiler profiler) {
        this.transaction = transaction;
        this.profiler = profiler;
    }

    @Override
    public GammaTranlocal privatize(GammaAtomicObject atomicObject) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void attachAsNew(GammaTranlocal tranlocal) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getFamilyName() {
        return transaction.getFamilyName();
    }

    @Override
    public long getReadVersion() {
        return transaction.getReadVersion();
    }

    @Override
    public TransactionStatus getStatus() {
        return transaction.getStatus();
    }

    @Override
    public long commit() {
        throw new TodoException();
    }

    @Override
    public void abort() {
        throw new TodoException();
    }

    @Override
    public void reset() {
        throw new TodoException();
    }

    @Override
    public void retry() {
        throw new TodoException();
    }

    @Override
    public void abortAndRetry() {
        throw new TodoException();
    }

    @Override
    public void startOr() {
        throw new TodoException();
    }

    @Override
    public void endOr() {
        throw new TodoException();
    }

    @Override
    public void endOrAndStartElse() {
        transaction.endOrAndStartElse();
    }

    @Override
    public LockManager getLockManager() {
        return transaction.getLockManager();
    }

    @Override
    public void executePostCommit(Runnable task) {
        transaction.executePostCommit(task);
    }
}
