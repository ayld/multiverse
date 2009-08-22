package org.multiverse.stms.beta;

import org.multiverse.api.TransactionStatus;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.FailedToObtainLocksException;
import org.multiverse.api.exceptions.PanicError;
import org.multiverse.api.exceptions.ResetFailureException;
import org.multiverse.api.locks.LockManager;
import org.multiverse.utils.TodoException;

import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link BetaTransaction}
 *
 * @author Peter Veentjer.
 */
public final class UpdateBetaTransaction implements BetaTransaction {
    private final AtomicLong clock;

    private long readVersion;
    private TransactionStatus status;
    private List<Runnable> postCommitTasks;
    private Map<BetaRef, BetaRefTranlocal> privatized = new IdentityHashMap<BetaRef, BetaRefTranlocal>(2);

    public UpdateBetaTransaction(AtomicLong clock) {
        this.clock = clock;
        init();
    }

    private void init() {
        this.status = TransactionStatus.active;
        this.readVersion = clock.get();
        this.postCommitTasks = null;
        this.privatized.clear();
    }

    @Override
    public <E> void attachNew(BetaRefTranlocal<E> refTranlocal) {
        switch (status) {
            case active:
                if (refTranlocal == null) {
                    throw new NullPointerException();
                }

                BetaRef ref = refTranlocal.getBetaRef();
                BetaRefTranlocal found = privatized.get(ref);
                if (found == null) {
                    privatized.put(ref, refTranlocal);
                } else if (found != refTranlocal) {
                    throw new PanicError();
                }
                break;
            case committed:
                throw new DeadTransactionException("Can't attachAsNew on a committed transaction");
            case aborted:
                throw new DeadTransactionException("Can't attachAsNew on an aborted transaction");
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public <E> BetaRefTranlocal<E> privatize(BetaRef<E> ref) {
        switch (status) {
            case active:
                if (ref == null) {
                    throw new NullPointerException();
                }

                BetaRefTranlocal tranlocal = privatized.get(ref);
                if (tranlocal != null) {
                    return tranlocal;
                }

                tranlocal = ref.privatize(readVersion);
                privatized.put(ref, tranlocal);
                return tranlocal;
            case committed:
                throw new DeadTransactionException("Can't privatize on a committed transaction");
            case aborted:
                throw new DeadTransactionException("Can't pribatize on an aborted transaction");
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public <E> BetaRefTranlocal<E> load(BetaRef<E> ref) {
        switch (status) {
            case active:
                if (ref == null) {
                    throw new NullPointerException();
                }

                BetaRefTranlocal tranlocal = privatized.get(ref);
                if (tranlocal != null) {
                    return tranlocal;
                }

                return ref.load(readVersion);
            case committed:
                throw new DeadTransactionException("Can't load on a committed transaction");
            case aborted:
                throw new DeadTransactionException("Can't load on an aborted transaction");
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
                boolean doAbort = true;
                try {
                    long version = doCommit();
                    doAbort = false;
                    status = TransactionStatus.committed;
                    return version;
                } finally {
                    if (doAbort) {
                        doAbort();
                    } else {
                        doExecutePostCommitTasks();
                    }
                }
            case committed:
                //ignore
                throw new TodoException();
            case aborted:
                throw new DeadTransactionException("Can't commit an aborted transaction");
            default:
                throw new IllegalStateException();
        }
    }

    private void doExecutePostCommitTasks() {
        if (postCommitTasks != null) {
            for (Runnable r : postCommitTasks) {
                r.run();
            }
        }
    }

    private long doCommit() {
        if (privatized.isEmpty()) {
            return readVersion;
        } else {
            BetaRefTranlocal[] writeSet = acquireAllLocksAndCheckForConflicts();
            long commitVersion = clock.incrementAndGet();
            writeAllChanges(writeSet, commitVersion);
            releaseAllLocks(writeSet);
            return commitVersion;
        }
    }

    /**
     * Acquires the locks and checks for writeconflicts.
     *
     * @return the writeset. An array containing the tranlocal (even index), and dirtinessstate (odd index). The
     *         array is 'empty' after the first null element is found or the end of the array is reached. Null is
     *         returned to indicate that the locks could not be obtained.
     */
    private BetaRefTranlocal[] acquireAllLocksAndCheckForConflicts() {
        BetaRefTranlocal[] writeSet = new BetaRefTranlocal[privatized.size()];

        boolean success = false;
        try {
            int k = 0;
            for (BetaRefTranlocal tranlocal : privatized.values()) {
                switch (tranlocal.getDirtinessState()) {
                    case clean:
                        break;
                    case dirty:
                        if (!tranlocal.getBetaRef().acquireLockAndDetectWriteConflict(this)) {
                            throw FailedToObtainLocksException.create();
                        } else {
                            writeSet[k] = tranlocal;
                            k++;
                        }
                        break;
                    case fresh:
                        writeSet[k] = tranlocal;
                        k++;
                        break;
                    default:
                        throw new IllegalStateException();
                }
            }

            success = true;
            return writeSet;
        } finally {
            if (!success) {
                releaseAllLocks(writeSet);
            }
        }
    }

    private void writeAllChanges(BetaRefTranlocal[] writeSet, long commitVersion) {
        for (int k = 0; k < writeSet.length; k++) {
            BetaRefTranlocal tranlocal = writeSet[k];

            if (tranlocal == null) {
                return;
            }

            tranlocal.signalCommit(commitVersion);
            tranlocal.getBetaRef().write(tranlocal);
        }
    }

    private void releaseAllLocks(BetaRefTranlocal[] writeSet) {
        for (int k = 0; k < writeSet.length; k++) {
            BetaRefTranlocal tranlocal = writeSet[k];

            if (tranlocal == null) {
                return;
            }

            tranlocal.getBetaRef().releaseLock(this);
        }
    }

    @Override
    public void abort() {
        switch (status) {
            case active:
                doAbort();
                break;
            case committed:
                throw new DeadTransactionException("Can't abort a committed transaction");
            case aborted:
                doAbort();
                break;
            default:
                throw new IllegalStateException();
        }
    }

    private void doAbort() {
        postCommitTasks = null;
        privatized.clear();
        status = TransactionStatus.aborted;
    }

    @Override
    public void reset() {
        switch (status) {
            case active:
                throw new ResetFailureException("Can't reset an active transaction");
            case committed:
                init();
                break;
            case aborted:
                init();
                break;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public void executePostCommit(Runnable task) {
        switch (status) {
            case active:
                if (task == null) {
                    throw new NullPointerException();
                }

                if (postCommitTasks == null) {
                    postCommitTasks = new LinkedList<Runnable>();
                }
                postCommitTasks.add(task);
                break;
            case committed:
                throw new DeadTransactionException("Can't executePostCommit on a committed transaction");
            case aborted:
                throw new DeadTransactionException("Can't executePostCommit on an aborted transaction");
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
        throw new UnsupportedOperationException();
    }

    @Override
    public void endOr() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void endOrAndStartElse() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LockManager getLockManager() {
        throw new UnsupportedOperationException();
    }
}
