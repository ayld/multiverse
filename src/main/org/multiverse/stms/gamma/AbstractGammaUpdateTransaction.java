package org.multiverse.stms.gamma;

import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.FailedToObtainLocksException;
import org.multiverse.api.exceptions.WriteConflictException;
import org.multiverse.stms.AbstractTransaction;
import org.multiverse.utils.commitlock.CommitLockPolicy;
import static org.multiverse.utils.commitlock.CommitLockUtils.nothingToLock;
import static org.multiverse.utils.commitlock.CommitLockUtils.releaseLocks;
import org.multiverse.utils.profiling.Profiler;

import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractGammaUpdateTransaction extends AbstractTransaction implements GammaTransaction {

    private final Profiler profiler;

    protected AbstractGammaUpdateTransaction(String familyName, AtomicLong clock, CommitLockPolicy lockPolicy, Profiler profiler) {
        super(familyName, clock, lockPolicy);

        this.profiler = profiler;
    }

    @Override
    public GammaTranlocal privatize(GammaAtomicObject atomicObject) {
        switch (status) {
            case active:
                return onPrivatize(atomicObject);
            case committed:
                throw new DeadTransactionException();
            case aborted:
                throw new DeadTransactionException();
            default:
                throw new IllegalStateException();
        }
    }

    protected abstract GammaTranlocal onPrivatize(GammaAtomicObject atomicObject);

    @Override
    public void attachAsNew(GammaTranlocal tranlocal) {
        switch (status) {
            case active:
                onAttachAsNew(tranlocal);
                break;
            case committed:
                throw new DeadTransactionException();
            case aborted:
                throw new DeadTransactionException();
            default:
                throw new RuntimeException();
        }
    }

    protected abstract void onAttachAsNew(GammaTranlocal tranlocal);

    protected void storeChangesAndReleaseLocks(GammaTranlocal[] writeset, long commitVersion) {
        for (int k = 0; k < writeset.length; k++) {
            GammaTranlocal tranlocal = writeset[k];
            if (tranlocal == null) {
                return;
            }

            tranlocal.storeAndReleaseLock(commitVersion);
        }
    }

    @Override
    protected long onCommit() {
        GammaTranlocal[] writeSet = createWriteSet();

        if (nothingToLock(writeSet)) {
            if (profiler != null) {
                profiler.getCounter(getFamilyName(), "updatetransaction.emptycommitcount").incrementAndGet();
            }
            //there is nothing to commit.
            return readVersion;
        }

        switch (commitLockPolicy.tryLockAllAndDetectConflicts(writeSet, this)) {
            case success:
                boolean releaseLocksNeeded = true;
                try {
                    long commitVersion = clock.incrementAndGet();
                    storeChangesAndReleaseLocks(writeSet, commitVersion);
                    releaseLocksNeeded = false;

                    if (profiler != null) {
                        profiler.getCounter(getFamilyName(), "updatetransaction.committed").incrementAndGet();
                    }

                    return commitVersion;
                } finally {
                    if (releaseLocksNeeded) {
                        releaseLocks(writeSet, this);
                    }
                }
            case failure:
                if (profiler != null) {
                    profiler.getCounter(getFamilyName(), "updatetransaction.lockfailed").incrementAndGet();
                }
                throw FailedToObtainLocksException.create();
            case conflict:
                if (profiler != null) {
                    profiler.getCounter(getFamilyName(), "updatetransaction.conflictcount").incrementAndGet();
                }
                throw WriteConflictException.create();
            default:
                throw new IllegalStateException();
        }
    }

    protected abstract GammaTranlocal[] createWriteSet();
}
