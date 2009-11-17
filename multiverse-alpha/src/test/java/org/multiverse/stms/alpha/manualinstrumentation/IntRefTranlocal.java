package org.multiverse.stms.alpha.manualinstrumentation;

import org.multiverse.stms.alpha.AlphaAtomicObject;
import org.multiverse.stms.alpha.AlphaTranlocal;
import org.multiverse.stms.alpha.AlphaTranlocalSnapshot;
import org.multiverse.stms.alpha.DirtinessStatus;

/**
 * access modifiers for fields are public because this object is used for testing purposes. For the
 * instrumentation the fields don't need to be this public.
 */
public class IntRefTranlocal extends AlphaTranlocal {
    public IntRef atomicObject;
    public int value;
    private IntRefTranlocal origin;

    public IntRefTranlocal(IntRefTranlocal origin) {
        this.origin = origin;
        this.___version = origin.___version;
        this.value = origin.value;
        this.atomicObject = origin.atomicObject;
    }

    public IntRefTranlocal(IntRef atomicObject) {
        this.atomicObject = atomicObject;
    }

    @Override
    public AlphaAtomicObject getAtomicObject() {
        return atomicObject;
    }


    @Override
    public void prepareForCommit(long writeVersion) {
        this.___version = writeVersion;
        this.___committed = true;
        this.origin = null;
    }

    @Override
    public DirtinessStatus getDirtinessStatus() {
        if (___committed) {
            return DirtinessStatus.committed;
        } else if (origin == null) {
            return DirtinessStatus.fresh;
        } else if (origin.value != this.value) {
            return DirtinessStatus.dirty;
        } else {
            return DirtinessStatus.clean;
        }
    }

    @Override
    public IntRefTranlocalSnapshot takeSnapshot() {
        return new IntRefTranlocalSnapshot(this);
    }
}

class IntRefTranlocalSnapshot extends AlphaTranlocalSnapshot {

    final IntRefTranlocal tranlocal;
    final int value;

    public IntRefTranlocalSnapshot(IntRefTranlocal tranlocal) {
        this.tranlocal = tranlocal;
        this.value = tranlocal.value;
    }

    @Override
    public AlphaTranlocal getTranlocal() {
        return tranlocal;
    }

    @Override
    public void restore() {
        tranlocal.value = value;
    }
}
