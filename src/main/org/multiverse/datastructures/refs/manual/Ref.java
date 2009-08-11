package org.multiverse.datastructures.refs.manual;

import org.multiverse.api.DirtinessStatus;
import static org.multiverse.api.StmUtils.retry;
import org.multiverse.api.Tranlocal;
import org.multiverse.api.TranlocalSnapshot;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.datastructures.refs.ManagedRef;
import org.multiverse.stms.alpha.mixins.FastAtomicObjectMixin;
import org.multiverse.templates.AtomicTemplate;

/**
 * A manual instrumented {@link org.multiverse.datastructures.refs.ManagedRef} implementation.
 * If this class is used, you don't need to worry about instrumentation/javaagents and
 * stuff like this. 
 *
 * @author Peter Veentjer
 */
public final class Ref<E> extends FastAtomicObjectMixin implements ManagedRef<E> {

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
                RefTranlocal<E> tranlocalRef = (RefTranlocal) t.privatize(Ref.this);
                return tranlocalRef.get();
            }
        }.execute();
    }

    @Override
    public E getOrAwait() {
        return new AtomicTemplate<E>() {
            @Override
            public E execute(Transaction t) throws Exception {
                RefTranlocal<E> tranlocalRef = (RefTranlocal) t.privatize(Ref.this);
                return tranlocalRef.getOrAwait();
            }
        }.execute();
    }

    @Override
    public E set(final E newRef) {
        return new AtomicTemplate<E>() {
            @Override
            public E execute(Transaction t) throws Exception {
                RefTranlocal<E> tranlocalRef = (RefTranlocal) t.privatize(Ref.this);
                return tranlocalRef.set(newRef);
            }
        }.execute();
    }

    @Override
    public boolean isNull() {
        return new AtomicTemplate<Boolean>() {
            @Override
            public Boolean execute(Transaction t) throws Exception {
                RefTranlocal<E> tranlocalRef = (RefTranlocal) t.privatize(Ref.this);
                return tranlocalRef.isNull();
            }
        }.execute();
    }

    @Override
    public E clear() {
        return new AtomicTemplate<E>() {
            @Override
            public E execute(Transaction t) throws Exception {
                RefTranlocal<E> tranlocalRef = (RefTranlocal) t.privatize(Ref.this);
                return tranlocalRef.clear();
            }
        }.execute();
    }

    @Override
    public RefTranlocal<E> privatize(long readVersion) {
        RefTranlocal<E> origin = (RefTranlocal<E>) load(readVersion);
        return new RefTranlocal<E>(origin);
    }
}

class RefTranlocal<E> extends Tranlocal {
    //field belonging to the stm.
    Ref atomicObject;
    RefTranlocal origin;

    E ref;
    
    RefTranlocal(RefTranlocal<E> origin) {
        this.version = origin.version;
        this.atomicObject = origin.atomicObject;
        this.ref = origin.ref;
        this.origin = origin;
    }

    RefTranlocal(Ref<E> owner) {
        this(owner, null);
    }

    RefTranlocal(Ref<E> owner, E ref) {
        this.version = Long.MIN_VALUE;
        this.atomicObject = owner;
        this.ref = ref;
    }

    @Override
    public Object getAtomicObject() {
        return atomicObject;
    }

    public E clear() {
        E oldValue = ref;
        ref = null;
        return oldValue;
    }

    public boolean isNull() {
        return ref == null;
    }

    public E get() {
        return ref;
    }

    public E set(E newValue) {
        if (committed) {
            throw new ReadonlyException();
        }
        E oldValue = ref;
        this.ref = newValue;
        return oldValue;
    }

    public E getOrAwait() {
        if (isNull()) {
            retry();
        }

        return ref;
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
        } else if (origin.ref != this.ref) {
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
        this.value = tranlocal.ref;
    }

    @Override
    public Tranlocal getTranlocal() {
        return tranlocal;
    }

    @Override
    public void restore() {
        tranlocal.ref = value;
    }
}
