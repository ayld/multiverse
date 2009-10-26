package org.multiverse.stms;

import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionStatus;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.ResetFailureException;
import org.multiverse.utils.clock.Clock;
import org.multiverse.utils.commitlock.CommitLockPolicy;

import static java.text.MessageFormat.format;
import java.util.LinkedList;
import java.util.List;

/**
 * An abstract {@link Transaction} implementation that contains most of the plumbing logic. Extend
 * this and prevent duplicate logic.
 * <p/>
 * The on-methods can be overridden.
 * <p/>
 * The subclass needs to call the init when it has completed its constructor. Can't be done inside
 * the constructor of the abstracttransaction because properties in the subclass perhaps are not set.
 *
 * @author Peter Veentjer.
 */
public abstract class AbstractTransaction implements Transaction {

    protected final Clock clock;

    protected CommitLockPolicy commitLockPolicy;
    protected List<Runnable> deferredTasks;
    protected List<Runnable> compensatingTasks;
    protected long readVersion;
    protected long commitVersion;
    protected TransactionStatus status;
    protected String familyName;

    /**
     * Creates a new AbstractTransaction.
     *
     * @param familyName       the familyname this transaction has.
     * @param clock            the Clock this transaction uses
     * @param commitLockPolicy the CommitLockPolicy to use when the transaction commits.
     * @throws NullPointerException if clock or commitLockPolicy is null.
     */
    public AbstractTransaction(String familyName, Clock clock, CommitLockPolicy commitLockPolicy) {
        if (clock == null) {
            throw new NullPointerException();
        }
        this.clock = clock;
        this.commitLockPolicy = commitLockPolicy;
        this.familyName = familyName;
    }

    protected final void init() {
        this.deferredTasks = null;
        this.compensatingTasks = null;
        this.readVersion = clock.getTime();
        this.status = TransactionStatus.active;
        onInit();
    }

    public String getFamilyName() {
        return familyName;
    }

    protected void onInit() {
    }

    @Override
    public void deferredExecute(Runnable task) {
        switch (status) {
            case active:
                if (task == null) {
                    throw new NullPointerException();
                }
                if (deferredTasks == null) {
                    deferredTasks = new LinkedList<Runnable>();
                }
                deferredTasks.add(task);
                break;
            case committed: {
                String msg = format("Can't add deferredExecute task on already committed transaction '%s'", familyName);
                throw new DeadTransactionException(msg);
            }
            case aborted: {
                String msg = format("Can't add deferredExecute task on already aborted transaction '%s'", familyName);
                throw new DeadTransactionException(msg);
            }
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public void compensatingExecute(Runnable task) {
        switch (status) {
            case active:
                if (task == null) {
                    throw new NullPointerException();
                }

                if (compensatingTasks == null) {
                    compensatingTasks = new LinkedList<Runnable>();
                }
                compensatingTasks.add(task);
                break;
            case committed: {
                String msg = format("Can't execute compensating task on already committed transaction '%s'", familyName);
                throw new DeadTransactionException(msg);
            }
            case aborted: {
                String msg = format("Can't execute compensating task on already aborted transaction '%s'", familyName);
                throw new DeadTransactionException(msg);
            }
            default:
                throw new RuntimeException();
        }
    }

    public CommitLockPolicy getCommitLockPolicy() {
        return commitLockPolicy;
    }

    public void setCommitLockPolicy(CommitLockPolicy newCommitLockPolicy) {
        this.commitLockPolicy = newCommitLockPolicy;
    }

    @Override
    public long getReadVersion() {
        return readVersion;
    }

    @Override
    public Transaction restart() {
        switch (status) {
            case active: {
                String msg = format("Can't restart active transaction '%s', abort or commit first", familyName);
                throw new ResetFailureException(msg);
            }
            case committed:
                //fall through
            case aborted:
                init();
                return this;
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public TransactionStatus getStatus() {
        return status;
    }

    protected void executeDeferredTasks() {
        if (deferredTasks != null) {
            try {
                for (Runnable task : deferredTasks) {
                    task.run();
                }
            } finally {
                deferredTasks = null;
            }
        }
    }

    protected void executeCompensatingTasks() {
        if (compensatingTasks != null) {
            try {
                for (Runnable task : compensatingTasks) {
                    task.run();
                }
            } finally {
                compensatingTasks = null;
            }
        }
    }

    @Override
    public void abort() {
        switch (status) {
            case active:
                doAbort();
                executeCompensatingTasks();
                break;
            case committed: {
                String msg = format("Can't abort already committed transaction '%s'", familyName);
                throw new DeadTransactionException(msg);
            }
            case aborted:
                //ignore
                break;
            default:
                throw new RuntimeException();
        }
    }

    protected final void doAbort() {
        status = TransactionStatus.aborted;
        deferredTasks = null;
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
                        executeDeferredTasks();
                    }
                }
            case committed:
                return commitVersion;
            case aborted: {
                String msg = format("Can't commit already aborted transaction '%s'", familyName);
                throw new DeadTransactionException(msg);
            }
            default:
                throw new IllegalStateException();
        }
    }

    protected long onCommit() {
        return readVersion;
    }

    @Override
    public void abortAndWaitForRetry() {
        switch (status) {
            case active:
                onAbortAndRetry();
                break;
            case committed: {
                String msg = format("Can't call abortAndRetry on already committed transaction '%s'", familyName);
                throw new DeadTransactionException(msg);
            }
            case aborted: {
                String msg = format("Can't call abortAndRetry on already aborted transaction '%s'", familyName);
                throw new DeadTransactionException(msg);
            }
            default:
                throw new RuntimeException();
        }
    }

    protected void onAbortAndRetry() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void startOr() {
        switch (status) {
            case active:
                onStartOr();
                break;
            case committed: {
                String msg = format("Can't call startOr on already committed transaction '%s'", familyName);
                throw new DeadTransactionException(msg);
            }
            case aborted: {
                String msg = format("Can't call startOr on already aborted transaction '%s'", familyName);
                throw new DeadTransactionException(msg);
            }
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
            case committed: {
                String msg = format("Can't call endOr on already committed transaction '%s'", familyName);
                throw new DeadTransactionException(msg);
            }
            case aborted: {
                String msg = format("Can't call endOr on already aborted transaction '%s'", familyName);
                throw new DeadTransactionException(msg);
            }
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
            case committed: {
                String msg = format("Can't call endOrAndStartElse on already committed transaction '%s'", familyName);
                throw new DeadTransactionException(msg);
            }
            case aborted: {
                String msg = format("Can't call endOrAndStartElse on already aborted transaction '%s'", familyName);
                throw new DeadTransactionException(msg);
            }
            default:
                throw new RuntimeException();
        }
    }

    protected void onEndOrAndStartElse() {
    }
}
