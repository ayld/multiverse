package org.multiverse.stms.gamma;

import org.multiverse.api.exceptions.FailedToObtainLocksException;
import org.multiverse.api.exceptions.WriteConflictException;
import org.multiverse.utils.TodoException;
import org.multiverse.utils.commitlock.CommitLockPolicy;
import org.multiverse.utils.profiling.Profiler;
import org.multiverse.utils.spinning.SpinPolicy;

import java.util.concurrent.atomic.AtomicLong;

/**
 * An update transaction for the GammaStm that allows only a single element to attached.
 *
 * @author Peter Veentjer.
 */
public class TinyLengthUpdateGammaTransaction extends AbstractGammaUpdateTransaction {

    private GammaTranlocal attachedTranlocal;

    private final SpinPolicy spinPolicy;

    public TinyLengthUpdateGammaTransaction(
            String familyName, AtomicLong clock, CommitLockPolicy lockPolicy, SpinPolicy spinPolicy, Profiler profiler) {
        super(familyName, clock, lockPolicy, profiler);
        this.spinPolicy = spinPolicy;
        init();
    }

    @Override
    protected void onInit() {
        attachedTranlocal = null;
    }

    @Override
    protected GammaTranlocal onPrivatize(GammaAtomicObject atomicObject) {
        if (atomicObject == null) {
            return null;
        } else if (attachedTranlocal == null) {
            attachedTranlocal = atomicObject.privatize(readVersion, spinPolicy);
            return attachedTranlocal;
        } else if (attachedTranlocal.getAtomicObject() == atomicObject) {
            return attachedTranlocal;
        } else {
            throw new TodoException();
        }
    }

    @Override
    protected void onAttachAsNew(GammaTranlocal tranlocal) {
        if (tranlocal == null) {
            throw new NullPointerException();
        } else if (attachedTranlocal == null) {
            attachedTranlocal = tranlocal;
        } else if (attachedTranlocal != tranlocal) {
            throw new TodoException();
        }
    }

    @Override
    protected long onCommit() {
        GammaTranlocal writeSet = createSingleElementWriteSet();

        if (writeSet == null) {
            //there is nothing to commit.
            return readVersion;
        }

        switch (commitLockPolicy.tryLockAndDetectConflict(writeSet, this)) {
            case success:
                boolean releaseLocksNeeded = true;
                try {
                    long commitVersion = clock.incrementAndGet();
                    writeSet.storeAndReleaseLock(commitVersion);
                    releaseLocksNeeded = false;
                    return commitVersion;
                } finally {
                    if (releaseLocksNeeded) {
                        writeSet.releaseLock(this);
                    }
                }
            case failure:
                throw FailedToObtainLocksException.create();
            case conflict:
                throw WriteConflictException.create();
            default:
                throw new IllegalStateException();
        }
    }


    protected GammaTranlocal createSingleElementWriteSet() {
        if (attachedTranlocal == null) {
            return null;
        }

        switch (attachedTranlocal.getDirtinessStatus()) {
            case clean:
                return null;
            case committed:
                return null;
            case fresh:
                //fall through
            case dirty:
                return attachedTranlocal;
            case conflict:
                throw WriteConflictException.create();
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    protected void onAbort() {
        attachedTranlocal = null;
    }

    @Override
    protected GammaTranlocal[] createWriteSet() {
        throw new UnsupportedOperationException("This method should not be called");
    }
}
