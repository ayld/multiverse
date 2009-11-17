package org.multiverse.stms.alpha.manualinstrumentation;

import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.stms.alpha.*;
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
                RefTranlocal<E> tranlocal = (RefTranlocal) ((AlphaTransaction) t).load(Ref.this);
                return null;
            }
        }.execute();
    }

    public Ref(final E value) {
        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                RefTranlocal<E> tranlocal = (RefTranlocal) ((AlphaTransaction) t).load(Ref.this);
                tranlocal.value = value;
                return null;
            }
        }.execute();
    }

    public E get() {
        return new AtomicTemplate<E>(true) {
            @Override
            public E execute(Transaction t) throws Exception {
                RefTranlocal<E> tranlocal = (RefTranlocal) ((AlphaTransaction) t).load(Ref.this);
                return get(tranlocal);
            }
        }.execute();
    }

    public void set(final E newValue) {
        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                RefTranlocal<E> tranlocal = (RefTranlocal) ((AlphaTransaction) t).load(Ref.this);
                set(tranlocal, newValue);
                return null;
            }
        }.execute();
    }

    public boolean isNull() {
        return new AtomicTemplate<Boolean>(true) {
            @Override
            public Boolean execute(Transaction t) throws Exception {
                RefTranlocal<E> tranlocal = (RefTranlocal) ((AlphaTransaction) t).load(Ref.this);
                return isNull(tranlocal);
            }
        }.execute();
    }

    public E clear() {
        return new AtomicTemplate<E>() {
            @Override
            public E execute(Transaction t) throws Exception {
                RefTranlocal<E> tranlocal = (RefTranlocal) ((AlphaTransaction) t).load(Ref.this);
                return clear(tranlocal);
            }
        }.execute();
    }

    @Override
    public RefTranlocal<E> ___loadUpdatable(long readVersion) {
        RefTranlocal<E> origin = (RefTranlocal) ___load(readVersion);
        if (origin == null) {
            return new RefTranlocal<E>(this);
        } else {
            return new RefTranlocal(origin);
        }
    }

    public E clear(RefTranlocal<E> tranlocal) {
        E oldValue = tranlocal.value;
        tranlocal.value = null;
        return oldValue;
    }

    public boolean isNull(RefTranlocal<E> tranlocal) {
        return tranlocal.value == null;
    }

    public E get(RefTranlocal<E> tranlocal) {
        return tranlocal.value;
    }

    public void set(RefTranlocal<E> tranlocal, E newValue) {
        if (tranlocal.___committed) {
            throw new ReadonlyException();
        }
        tranlocal.value = newValue;
    }
}

class RefTranlocal<E> extends AlphaTranlocal {
    Ref atomicObject;
    E value;
    RefTranlocal origin;

    RefTranlocal(RefTranlocal<E> origin) {
        this.___version = origin.___version;
        this.atomicObject = origin.atomicObject;
        this.value = origin.value;
        this.origin = origin;
    }

    RefTranlocal(Ref<E> owner) {
        this.___version = Long.MIN_VALUE;
        this.atomicObject = owner;

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
        return new RefTranlocalSnapshot<E>(this);
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

class RefTranlocalSnapshot<E> extends AlphaTranlocalSnapshot {
    final RefTranlocal tranlocal;
    final E value;

    RefTranlocalSnapshot(RefTranlocal<E> tranlocal) {
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
