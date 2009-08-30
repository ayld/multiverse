package org.multiverse.stms.gamma;

import org.multiverse.api.exceptions.LoadLockedException;
import org.multiverse.api.exceptions.LoadTooOldVersionException;
import org.multiverse.api.exceptions.LoadUncommittedException;
import org.multiverse.utils.spinning.SpinPolicy;
import org.multiverse.utils.spinning.SpinTask;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public abstract class AbstractGammaAtomicObject implements GammaAtomicObject {

    private final static AtomicReferenceFieldUpdater<AbstractGammaAtomicObject, GammaTranlocal> tranlocalUpdater =
            AtomicReferenceFieldUpdater.newUpdater(AbstractGammaAtomicObject.class, GammaTranlocal.class, "tranlocal");

    private final static AtomicReferenceFieldUpdater<AbstractGammaAtomicObject, GammaTransaction> lockOwnerUpdater =
            AtomicReferenceFieldUpdater.newUpdater(AbstractGammaAtomicObject.class, GammaTransaction.class, "lockOwner");

    private volatile GammaTranlocal tranlocal;

    private volatile GammaTransaction lockOwner;

    @Override
    public GammaTranlocal load() {
        return tranlocalUpdater.get(this);
    }

    @Override
    public GammaTranlocal load(long version, SpinPolicy spinPolicy) {
        GammaTranlocal tranlocalT1 = tranlocalUpdater.get(this);

        if (tranlocalT1 == null) {
            throw new LoadUncommittedException();
        } else if (tranlocalT1.getVersion() == version) {
            //we are lucky, the version we found is exactly the version we are looking for.
            return tranlocalT1;
        } else if (tranlocal.getVersion() > version) {
            //the version found is too new this would be the location to look in the history
            //for an older version
            throw LoadTooOldVersionException.create();
        } else {
            if (isLocked()) {
                if (!spinPolicy.execute(new SpinOnLockTask())) {
                    throw LoadLockedException.create();
                }
            }

            //read the tranlocalT1 content again.
            GammaTranlocal tranlocalT2 = tranlocalUpdater.get(this);

            if (tranlocalT2 == tranlocalT1) {
                //the tranlocalT1 we have read is unlocked, so it is the one we want
                return tranlocalT1;
            } else if (tranlocalT2.getVersion() == version) {
                //we are lucky because tranlocalT2 is the version we want .. it could be that there
                //are pending writes or that a lot of writes slipped through.. but tranlocalT2 is exactly
                //the one we want..
                return tranlocalT2;
            } else {
                //we are not able to determine if the read tranlocals are usable.
                //todo: different transaction
                throw LoadLockedException.create();
            }
        }
    }

    class SpinOnLockTask implements SpinTask {

        @Override
        public boolean run() {
            return !isLocked();
        }
    }

    public boolean isLocked() {
        return lockOwnerUpdater.get(this) != null;
    }

    @Override
    public void storeAndReleaseLock(GammaTranlocal tranlocal) {
        this.tranlocal = tranlocal;
        lockOwnerUpdater.set(this, null);
    }

    @Override
    public boolean tryLock(GammaTransaction lockOwner) {
        return lockOwnerUpdater.compareAndSet(this, null, lockOwner);
    }

    @Override
    public void releaseLock(GammaTransaction expectedLockOwner) {
        lockOwnerUpdater.compareAndSet(this, expectedLockOwner, null);
    }
}