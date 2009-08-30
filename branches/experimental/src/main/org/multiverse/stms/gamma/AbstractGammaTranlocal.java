package org.multiverse.stms.gamma;

import org.multiverse.api.Transaction;
import org.multiverse.stms.alpha.DirtinessStatus;
import org.multiverse.utils.commitlock.CommitLockResult;

import static java.lang.Math.abs;

public abstract class AbstractGammaTranlocal<A extends GammaAtomicObject> implements GammaTranlocal<A> {

    protected final A atomicObject;

    //the version field not only contains the version, but also contains if the field is committed.
    //a negative value indicates that it isn't committed, and a posive value does.
    private long version;

    public AbstractGammaTranlocal(A atomicObject) {
        this.atomicObject = atomicObject;
    }

    public AbstractGammaTranlocal(AbstractGammaTranlocal<A> origin) {
        this.atomicObject = origin.atomicObject;
        this.version = -origin.version;
    }

    @Override
    public A getAtomicObject() {
        return atomicObject;
    }

    @Override
    public DirtinessStatus getDirtinessStatus() {
        if (version == 0) {
            return DirtinessStatus.fresh;
        } else if (version > 0) {
            return DirtinessStatus.committed;
        } else {
            GammaTranlocal origin = atomicObject.load();
            if (origin.getVersion() > -version) {
                return DirtinessStatus.conflict;
            } else {
                return isDirty(origin) ? DirtinessStatus.dirty : DirtinessStatus.clean;
            }
        }
    }

    protected abstract boolean isDirty(GammaTranlocal origin);

    @Override
    public void storeAndReleaseLock(long commitVersion) {
        this.version = commitVersion;
        atomicObject.storeAndReleaseLock(this);
    }

    @Override
    public long getVersion() {
        return abs(version);
    }

    @Override
    public CommitLockResult tryLockAndDetectConflicts(Transaction lockOwner) {
        boolean lockedAcquired = atomicObject.tryLock((GammaTransaction) lockOwner);
        if (!lockedAcquired) {
            return CommitLockResult.failure;
        }

        GammaTranlocal mostRecentlyWritten = atomicObject.load();
        if (mostRecentlyWritten == null) {
            return CommitLockResult.success;
        }

        boolean noConflict = mostRecentlyWritten.getVersion() <= lockOwner.getReadVersion();
        if (noConflict) {
            return CommitLockResult.success;
        } else {
            atomicObject.releaseLock((GammaTransaction) lockOwner);
            return CommitLockResult.conflict;
        }
    }

    @Override
    public void releaseLock(Transaction expectedLockOwner) {
        atomicObject.releaseLock((GammaTransaction) expectedLockOwner);
    }
}
