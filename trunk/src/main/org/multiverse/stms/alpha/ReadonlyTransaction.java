package org.multiverse.stms.alpha;

import org.multiverse.api.TransactionStatus;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.LoadUncommittedException;
import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.api.exceptions.ResetFailureException;
import org.multiverse.api.locks.DeactivatedLockManager;
import org.multiverse.api.locks.LockManager;
import org.multiverse.utils.TodoException;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A readonly {@link org.multiverse.api.Transaction} implementation. Unlike the {@link UpdateTransaction}
 * a readonly transaction doesn't need track any reads done. This has the advantage that a
 * readonly transaction consumes a lot less resources.
 *
 * @author Peter Veentjer.
 */
final class ReadonlyTransaction implements AlphaTransaction {
    private final AlphaStmStatistics statistics;
    private final AtomicLong clock;

    private long readVersion;
    private TransactionStatus status;

    public ReadonlyTransaction(AlphaStmStatistics statistics, AtomicLong clock) {
        this.clock = clock;

        this.statistics = statistics;

        init();
    }

    public ReadonlyTransaction(AlphaStmStatistics statistics, long readVersion) {
        this.clock = null;
        this.statistics = statistics;
        this.status = TransactionStatus.active;
        this.readVersion = readVersion;
    }

    @Override
    public void executePostCommit(Runnable r) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LockManager getLockManager() {
        return DeactivatedLockManager.INSTANCE;
    }

    private void init() {
        if (clock == null) {
            throw new ResetFailureException("Can't reset a flashback query");
        }

        status = TransactionStatus.active;
        this.readVersion = clock.get();
        if (statistics != null) {
            statistics.incReadonlyTransactionStartedCount();
        }
    }

    @Override
    public void retry() {
        throw new TodoException();
    }

    @Override
    public void reset() {
        switch (status) {
            case active:
                throw new ResetFailureException();
            case aborted:
                init();
                break;
            case committed:
                init();
                break;
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public long getReadVersion() {
        return readVersion;
    }

    @Override
    public Tranlocal load(Object item) {
        switch (status) {
            case active:
                if (item == null) {
                    return null;
                }

                if (!(item instanceof AlphaAtomicObject)) {
                    throw new IllegalArgumentException();
                }

                AlphaAtomicObject atomicObject = (AlphaAtomicObject) item;
                //todo: the load method.
                Tranlocal result = atomicObject.load(readVersion);
                if (result == null) {
                    throw new LoadUncommittedException();
                }
                return result;
            case committed:
                throw new DeadTransactionException("Can't load from an already committed transaction");
            case aborted:
                throw new DeadTransactionException("Can't load from an already aborted transaction");
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public Tranlocal privatize(Object item) {
        throw new ReadonlyException();
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
                return readVersion;
            case committed:
                return readVersion;
            case aborted:
                throw new DeadTransactionException("Can't commit an already aborted transaction");
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public void abort() {
        switch (status) {
            case active:
                status = TransactionStatus.aborted;
                break;
            case committed:
                throw new DeadTransactionException("Can't abort an already committed transaction");
            case aborted:
                //ignore
                break;
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public void attachNew(Tranlocal tranlocal) {
        throw new ReadonlyException();
    }

    @Override
    public void abortAndRetry() {
        throw new ReadonlyException();
    }

    @Override
    public void startOr() {
        //since no changes can bemade, the orelse machnism can be completely ignored
    }

    @Override
    public void endOr() {
        //since no changes can bemade, the orelse machnism can be completely ignored
    }

    @Override
    public void endOrAndStartElse() {
        //since no changes can bemade, the orelse machnism can be completely ignored
    }
}
