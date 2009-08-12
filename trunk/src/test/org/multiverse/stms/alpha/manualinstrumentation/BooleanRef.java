package org.multiverse.stms.alpha.manualinstrumentation;

import org.multiverse.api.Transaction;
import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.api.exceptions.LoadUncommittedAtomicObjectException;
import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.stms.alpha.AlphaStmUtils;
import org.multiverse.stms.alpha.DirtinessStatus;
import org.multiverse.stms.alpha.Tranlocal;
import org.multiverse.stms.alpha.TranlocalSnapshot;
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
                BooleanRefTranlocal tranlocalThis = new BooleanRefTranlocal(BooleanRef.this, value);
                t.attachNew(tranlocalThis);
                return null;
            }
        }.execute();
    }

    @AtomicMethod
    public void set(boolean value) {
        ((BooleanRefTranlocal) AlphaStmUtils.privatize(this)).set(value);
    }

    @AtomicMethod
    public boolean get() {
        return ((BooleanRefTranlocal) AlphaStmUtils.privatize(this)).get();
    }

    @Override
    public Tranlocal privatize(long version) {
        BooleanRefTranlocal origin = (BooleanRefTranlocal) load(version);
        if (origin == null) {
            throw new LoadUncommittedAtomicObjectException();
        }
        return new BooleanRefTranlocal(origin);
    }
}

class BooleanRefTranlocal extends Tranlocal {
    final BooleanRef atomicObject;
    boolean value;
    BooleanRefTranlocal origin;

    public BooleanRefTranlocal(BooleanRefTranlocal origin) {
        this.origin = origin;
        this.value = origin.value;
        this.atomicObject = origin.atomicObject;
    }

    public BooleanRefTranlocal(BooleanRef atomicObject, boolean value) {
        this.value = value;
        this.atomicObject = atomicObject;
    }

    @Override
    public Object getAtomicObject() {
        return atomicObject;
    }

    public void set(boolean newValue) {
        if (committed) {
            throw new ReadonlyException();
        } else {
            this.value = newValue;
        }
    }

    public boolean get() {
        return value;
    }

    @Override
    public void prepareForCommit(long writeVersion) {
        this.version = writeVersion;
        this.committed = true;
        this.origin = null;
    }

    @Override
    public TranlocalSnapshot takeSnapshot() {
        return new BooleanRefTranlocalSnapshot(this);
    }

    @Override
    public DirtinessStatus getDirtinessStatus() {
        if (committed) {
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

class BooleanRefTranlocalSnapshot extends TranlocalSnapshot {
    final BooleanRefTranlocal tranlocal;
    final boolean value;

    public BooleanRefTranlocalSnapshot(BooleanRefTranlocal tranlocal) {
        this.tranlocal = tranlocal;
        value = tranlocal.value;
    }

    @Override
    public Tranlocal getTranlocal() {
        return tranlocal;
    }

    @Override
    public void restore() {
        tranlocal.value = value;
    }
}
