package org.multiverse.stms.alpha.manualinstrumentation;

import org.multiverse.api.Transaction;
import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.stms.alpha.*;
import org.multiverse.stms.alpha.mixins.FastAtomicObjectMixin;
import org.multiverse.templates.AtomicTemplate;

public class BooleanRef extends FastAtomicObjectMixin {

    public BooleanRef() {
        this(false);
    }

    @AtomicMethod
    public BooleanRef(final boolean value) {
        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) {
                BooleanRefTranlocal tranlocal = (BooleanRefTranlocal) ((AlphaTransaction) t).load(BooleanRef.this);
                tranlocal.value = value;
                return null;
            }
        }.execute();
    }

    @AtomicMethod
    public void set(boolean value) {
        BooleanRefTranlocal tranlocal = ((BooleanRefTranlocal) AlphaStmUtils.load(this));
        set(tranlocal, value);
    }

    @AtomicMethod(readonly = true)
    public boolean get() {
        BooleanRefTranlocal tranlocal = ((BooleanRefTranlocal) AlphaStmUtils.load(this));
        return get(tranlocal);
    }


    public void set(BooleanRefTranlocal tranlocal, boolean newValue) {
        if (tranlocal.___committed) {
            throw new ReadonlyException();
        } else {
            tranlocal.value = newValue;
        }
    }

    public boolean get(BooleanRefTranlocal tranlocal) {
        return tranlocal.value;
    }

    @Override
    public AlphaTranlocal ___loadUpdatable(long version) {
        BooleanRefTranlocal origin = (BooleanRefTranlocal) ___load(version);
        if (origin == null) {
            return new BooleanRefTranlocal(this);
        } else {
            return new BooleanRefTranlocal(origin);
        }

    }
}

class BooleanRefTranlocal extends AlphaTranlocal {

    final BooleanRef atomicObject;
    boolean value;
    BooleanRefTranlocal origin;

    public BooleanRefTranlocal(BooleanRefTranlocal origin) {
        this.origin = origin;
        this.value = origin.value;
        this.atomicObject = origin.atomicObject;
    }

    public BooleanRefTranlocal(BooleanRef atomicObject) {
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
    public AlphaTranlocalSnapshot takeSnapshot() {
        return new BooleanRefTranlocalSnapshot(this);
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
}

class BooleanRefTranlocalSnapshot extends AlphaTranlocalSnapshot {

    final BooleanRefTranlocal tranlocal;
    final boolean value;

    public BooleanRefTranlocalSnapshot(BooleanRefTranlocal tranlocal) {
        this.tranlocal = tranlocal;
        value = tranlocal.value;
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
