package org.multiverse.stms.alpha.manualinstrumentation;

import org.multiverse.api.DirtinessStatus;
import org.multiverse.api.Tranlocal;
import org.multiverse.api.TranlocalSnapshot;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.stms.alpha.mixins.FastAtomicObjectMixin;
import org.multiverse.templates.AtomicTemplate;

/**
 * This implementation suffers from the ABA problem (well.. the stm suffers from it because the
 * isDirty method suffers from it). This can be fixed very easily, just add a counter. So although
 * the references may not have changed in the end but the counter has. And this will cause the
 * writeconfict we are after for the ABA problem.  See the AbaRef.
 *
 * @author Peter Veentjer
 */
public class Ref<E> extends FastAtomicObjectMixin {

    public Ref() {
        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                t.attachNew(new RefTranlocal(Ref.this));
                return null;
            }
        }.execute();
    }

    public Ref(final E value) {
        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                t.attachNew(new RefTranlocal(Ref.this, value));
                return null;
            }
        }.execute();
    }

    public E get() {
        return new AtomicTemplate<E>() {
            @Override
            public E execute(Transaction t) throws Exception {
                RefTranlocal<E> tranlocalRef = (RefTranlocal)t.privatize(Ref.this);
                return tranlocalRef.get();
            }
        }.execute();
    }

    public void set(final E newValue) {
        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                RefTranlocal<E> tranlocalRef = (RefTranlocal)t.privatize(Ref.this);
                tranlocalRef.set(newValue);
                return null;
            }
        }.execute();
    }

    public boolean isNull() {
        return new AtomicTemplate<Boolean>() {
            @Override
            public Boolean execute(Transaction t) throws Exception {
                RefTranlocal<E> tranlocalRef = (RefTranlocal)t.privatize(Ref.this);
                return tranlocalRef.isNull();
            }
        }.execute();
    }

    public E clear() {
        return new AtomicTemplate<E>() {
            @Override
            public E execute(Transaction t) throws Exception {
                RefTranlocal<E> tranlocalRef = (RefTranlocal)t.privatize(Ref.this);
                return tranlocalRef.clear();
            }
        }.execute();
    }

    @Override
    public RefTranlocal<E> privatize(long readVersion) {
        RefTranlocal<E> origin = (RefTranlocal) load(readVersion);
        return new RefTranlocal(origin);
    }
}

class RefTranlocal<E> extends Tranlocal {
    Ref atomicObject;
    E value;
    RefTranlocal origin;

    RefTranlocal(RefTranlocal<E> origin) {
        this.version = origin.version;
        this.atomicObject = origin.atomicObject;
        this.value = origin.value;
        this.origin = origin;
    }

    RefTranlocal(Ref<E> owner) {
        this(owner, null);
    }

    RefTranlocal(Ref<E> owner, E value) {
        this.version = Long.MIN_VALUE;
        this.atomicObject = owner;
        this.value = value;
    }

    @Override
    public Object getAtomicObject() {
        return atomicObject;
    }

    public E clear() {
        E oldValue = value;
        value = null;
        return oldValue;
    }

    public boolean isNull() {
        return value == null;
    }

    public E get() {
        return value;
    }

    public void set(E newValue) {
        if (committed) {
            throw new ReadonlyException();
        }
        this.value = newValue;
    }

    @Override
    public void prepareForCommit(long writeVersion) {
        this.version = writeVersion;
        this.committed = true;
        this.origin = null;
    }

    @Override
    public TranlocalSnapshot takeSnapshot() {
        return new RefTranlocalSnapshot<E>(this);
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

class RefTranlocalSnapshot<E> extends TranlocalSnapshot {
    final RefTranlocal tranlocal;
    final E value;

    RefTranlocalSnapshot(RefTranlocal<E> tranlocal) {
        this.tranlocal = tranlocal;
        this.value = tranlocal.value;
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
