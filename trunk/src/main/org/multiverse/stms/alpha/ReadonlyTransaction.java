package org.multiverse.stms.alpha;

import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.LoadUncommittedException;
import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.api.exceptions.ResetFailureException;
import org.multiverse.api.locks.DeactivatedLockManager;
import org.multiverse.api.locks.LockManager;
import org.multiverse.stms.AbstractTransaction;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A readonly {@link org.multiverse.api.Transaction} implementation. Unlike the {@link UpdateTransaction}
 * a readonly transaction doesn't need track any reads done. This has the advantage that a
 * readonly transaction consumes a lot less resources.
 *
 * @author Peter Veentjer.
 */
final class ReadonlyTransaction extends AbstractTransaction implements AlphaTransaction {
    private final AlphaStmStatistics statistics;

    public ReadonlyTransaction(AlphaStmStatistics statistics, AtomicLong clock) {
        super(clock, null);
        this.statistics = statistics;

        init();
    }

    @Override
    public LockManager getLockManager() {
        return DeactivatedLockManager.INSTANCE;
    }

    protected void onInit() {
        if (clock == null) {
            throw new ResetFailureException("Can't reset a flashback query");
        }

        if (statistics != null) {
            statistics.incReadonlyTransactionStartedCount();
        }
    }

    @Override
    public Tranlocal load(AlphaAtomicObject atomicObject) {
        switch (status) {
            case active:
                if (atomicObject == null) {
                    return null;
                }

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
    public Tranlocal privatize(AlphaAtomicObject item) {
        throw new ReadonlyException();
    }

    @Override
    public void attachNew(Tranlocal tranlocal) {
        throw new ReadonlyException();
    }

    @Override
    public void onAbortAndRetry() {
        throw new ReadonlyException();
    }
}
