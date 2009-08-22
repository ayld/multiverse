package org.multiverse.stms.beta;

import org.multiverse.api.TransactionStatus;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.api.exceptions.ResetFailureException;
import org.multiverse.api.locks.DeactivatedLockManager;
import org.multiverse.api.locks.LockManager;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A readonly BetaTransaction implementation. This implementation doesn't increase in size with long transactions
 * (unlike update transactions).
 *
 * @author Peter Veentjer.
 */
public class ReadonlyBetaTransaction implements BetaTransaction {
    private final AtomicLong clock;

    private TransactionStatus status;
    private long readVersion;
    private List<Runnable> postCommitTasks;

    public ReadonlyBetaTransaction(AtomicLong clock) {
        this.clock = clock;
        init();
    }

    private void init() {
        if (postCommitTasks != null) {
            postCommitTasks.clear();
        }
        status = TransactionStatus.active;
        readVersion = clock.get();
    }

    @Override
    public <E> void attachNew(BetaRefTranlocal<E> refTranlocal) {
        throw new ReadonlyException();
    }

    @Override
    public <E> BetaRefTranlocal<E> privatize(BetaRef<E> ref) {
        throw new ReadonlyException();
    }

    @Override
    public <E> BetaRefTranlocal<E> load(BetaRef<E> ref) {
        switch (status) {
            case active:
                return ref == null ? null : ref.load(readVersion);
            case committed:
                throw new DeadTransactionException("Can't load from a committed transaction");
            case aborted:
                throw new DeadTransactionException("Can't load from an aborted transaction");
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public long getReadVersion() {
        return readVersion;
    }

    @Override
    public TransactionStatus getStatus() {
        return status;
    }

    @Override
    public long commit() {
        switch (status) {
            case active:
                status = TransactionStatus.committed;
                executePostCommitTasks();
                return readVersion;
            case committed:
                return readVersion;
            case aborted:
                throw new DeadTransactionException("Can't commit an aborted transaction");
            default:
                throw new IllegalStateException();
        }
    }

    private void executePostCommitTasks() {
        if (postCommitTasks != null) {
            for (Runnable task : postCommitTasks) {
                task.run();
            }
        }
    }

    @Override
    public void abort() {
        switch (status) {
            case active:
                status = TransactionStatus.aborted;
                postCommitTasks = null;
                break;
            case committed:
                throw new DeadTransactionException("Can't abort an committed transaction");
            case aborted:
                //ignore
                break;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public void reset() {
        switch (status) {
            case active:
                throw new ResetFailureException("An active transaction can't be reset (needs to be aborted or committed first)");
            case committed:
                //fall through
            case aborted:
                init();
                break;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public void retry() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void abortAndRetry() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void startOr() {
        //ignore
    }

    @Override
    public void endOr() {
        //ignore
    }

    @Override
    public void endOrAndStartElse() {
        //ignore
    }

    @Override
    public LockManager getLockManager() {
        return DeactivatedLockManager.INSTANCE;
    }

    @Override
    public void executePostCommit(Runnable task) {
        switch (status) {
            case active:
                if (task == null) {
                    throw new NullPointerException();
                } else {
                    if (postCommitTasks == null) {
                        postCommitTasks = new LinkedList<Runnable>();
                    }
                    postCommitTasks.add(task);
                }
                break;
            case committed:
                throw new DeadTransactionException("Can't executePostCommit on an committed transaction");
            case aborted:
                throw new DeadTransactionException("Can't executePostCommit on an aborted transaction");
            default:
                throw new IllegalStateException();
        }
    }
}
