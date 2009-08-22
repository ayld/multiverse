package org.multiverse.stms;

import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionStatus;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.ResetFailureException;
import org.multiverse.api.locks.DeactivatedLockManager;
import org.multiverse.api.locks.LockManager;
import org.multiverse.utils.atomicobjectlocks.AtomicObjectLockPolicy;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An abstract {@link Transaction} implementation that contains most of the plumbing logic. Extend
 * this and prevent duplicate logic.
 * <p/>
 * The on-methods can be overriden.
 *
 * @author Peter Veentjer.
 */
public abstract class AbstractTransaction implements Transaction {

    protected final AtomicLong clock;

    protected AtomicObjectLockPolicy lockPolicy;
    protected List<Runnable> postCommitTasks;
    protected long readVersion;
    protected long commitVersion;
    protected TransactionStatus status;

    public AbstractTransaction(AtomicLong clock, AtomicObjectLockPolicy lockPolicy) {
        this.clock = clock;
        this.lockPolicy = lockPolicy;
    }

    protected final void init() {
        this.postCommitTasks = null;
        this.readVersion = clock.get();
        this.status = TransactionStatus.active;
        onInit();
    }

    protected void onInit() {
    }

    @Override
    public void executePostCommit(Runnable task) {
        switch (status) {
            case active:
                if (task == null) {
                    throw new NullPointerException();
                } else if (postCommitTasks == null) {
                    postCommitTasks = new LinkedList<Runnable>();
                }
                postCommitTasks.add(task);
                break;
            case committed:
                throw new DeadTransactionException("Can't add afterCommit task on a committed transaction");
            case aborted:
                throw new DeadTransactionException("Can't add afterCommit task on an aborted transaction");
            default:
                throw new RuntimeException();
        }
    }

    public AtomicObjectLockPolicy getLockPolicy() {
        return lockPolicy;
    }

    public void setLockPolicy(AtomicObjectLockPolicy writeSetLockPolicy) {
        this.lockPolicy = writeSetLockPolicy;
    }

    @Override
    public long getReadVersion() {
        return readVersion;
    }

    @Override
    public void reset() {
        switch (status) {
            case active:
                throw new ResetFailureException("Can't reset an active transaction, abort or commit first");
            case committed:
                //fall through
            case aborted:
                init();
                break;
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public TransactionStatus getStatus() {
        return status;
    }

    protected void executePostCommitTasks() {
        if (postCommitTasks != null) {
            try {
                for (Runnable task : postCommitTasks) {
                    task.run();
                }
            } finally {
                postCommitTasks = null;
            }
        }
    }

    @Override
    public void abort() {
        switch (status) {
            case active:
                doAbort();
                break;
            case committed:
                throw new DeadTransactionException("Can't call abort on a committed transaction");
            case aborted:
                //ignore
                break;
            default:
                throw new RuntimeException();
        }
    }

    protected void doAbort() {
        status = TransactionStatus.aborted;
        postCommitTasks = null;
        onAbort();
    }

    protected void onAbort() {
    }

    @Override
    public long commit() {
        switch (status) {
            case active:
                boolean abort = true;
                try {
                    commitVersion = onCommit();
                    abort = false;
                    return commitVersion;
                } finally {
                    if (abort) {
                        doAbort();
                    } else {
                        status = TransactionStatus.committed;
                        executePostCommitTasks();
                    }
                }
            case committed:
                return commitVersion;
            case aborted:
                throw new DeadTransactionException("Can't commit an aborted transaction");
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public void retry() {
        switch (status) {
            case active:
                onRetry();
                break;
            case committed:
                throw new DeadTransactionException("Can't retry a committed transaction");
            case aborted:
                throw new DeadTransactionException("Can't retry an aborted transaction");
            default:
                throw new IllegalStateException();
        }
    }

    protected void onRetry() {
        throw new UnsupportedOperationException();
    }

    protected long onCommit() {
        return readVersion;
    }

    @Override
    public void abortAndRetry() {
        switch (status) {
            case active:
                onAbortAndRetry();
                break;
            case committed:
                throw new DeadTransactionException("Can't call abortAndRetry on a committed transaction");
            case aborted:
                throw new DeadTransactionException("Can't call abortAndRetry on an aborted transaction");
            default:
                throw new RuntimeException();
        }
    }

    protected void onAbortAndRetry() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LockManager getLockManager() {
        switch (status) {
            case active:
                return onGetLockManager();
            case aborted:
                throw new DeadTransactionException("Can't get the LockManager, transaction already is aborted.");
            case committed:
                throw new DeadTransactionException("Can't get the LockManager, transaction already is committed.");
            default:
                throw new RuntimeException();
        }
    }

    protected LockManager onGetLockManager() {
        return DeactivatedLockManager.INSTANCE;
    }

    @Override
    public void startOr() {
        switch (status) {
            case active:
                onStartOr();
                break;
            case committed:
                throw new DeadTransactionException("Can't call startOr on a committed transaction");
            case aborted:
                throw new DeadTransactionException("Can't call startOr on an aborted transaction");
            default:
                throw new RuntimeException();
        }
    }

    protected void onStartOr() {
    }

    @Override
    public void endOr() {
        switch (status) {
            case active:
                onEndOr();
                break;
            case committed:
                throw new DeadTransactionException("Can't call endOr on a committed transaction");
            case aborted:
                throw new DeadTransactionException("Can't call endOr on an aborted transaction");
            default:
                throw new RuntimeException();
        }
    }

    protected void onEndOr() {
    }

    @Override
    public void endOrAndStartElse() {
        switch (status) {
            case active:
                onEndOrAndStartElse();
                break;
            case committed:
                throw new DeadTransactionException("Can't call endOrAndStartElse on a committed transaction");
            case aborted:
                throw new DeadTransactionException("Can't call endOrAndStartElse on an aborted transaction");
            default:
                throw new RuntimeException();
        }
    }

    protected void onEndOrAndStartElse() {
    }
}
