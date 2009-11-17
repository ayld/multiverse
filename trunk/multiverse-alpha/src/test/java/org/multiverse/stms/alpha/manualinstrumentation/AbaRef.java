package org.multiverse.stms.alpha.manualinstrumentation;

import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.stms.alpha.*;
import org.multiverse.stms.alpha.mixins.FastAtomicObjectMixin;
import org.multiverse.templates.AtomicTemplate;

/**
 * Ref that fixes the Aba problem.
 * <p/>
 * See the Ref for more documentation about the Aba problem.
 *
 * @author Peter Veentjer
 */
public class AbaRef<E> extends FastAtomicObjectMixin {

    public AbaRef() {
        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                AbaRefTranlocal<E> tranlocal = (AbaRefTranlocal) ((AlphaTransaction) t).load(AbaRef.this);
                return null;
            }
        }.execute();
    }

    public AbaRef(final E value) {
        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                AbaRefTranlocal<E> tranlocal = (AbaRefTranlocal) ((AlphaTransaction) t).load(AbaRef.this);
                tranlocal.value = value;
                return null;
            }
        }.execute();
    }

    public E get() {
        return new AtomicTemplate<E>(true) {
            @Override
            public E execute(Transaction t) throws Exception {
                AbaRefTranlocal<E> tranlocal = (AbaRefTranlocal) ((AlphaTransaction) t).load(AbaRef.this);
                return get(tranlocal);
            }
        }.execute();
    }

    public void set(final E newValue) {
        new AtomicTemplate<E>() {
            @Override
            public E execute(Transaction t) throws Exception {
                AbaRefTranlocal<E> tranlocal = (AbaRefTranlocal) ((AlphaTransaction) t).load(AbaRef.this);
                set(tranlocal, newValue);
                return null;
            }
        }.execute();
    }

    public boolean isNull() {
        return new AtomicTemplate<Boolean>(true) {
            @Override
            public Boolean execute(Transaction t) throws Exception {
                AbaRefTranlocal<E> tranlocal = (AbaRefTranlocal) ((AlphaTransaction) t).load(AbaRef.this);
                return isNull(tranlocal);
            }
        }.execute();
    }

    public E clear() {
        return new AtomicTemplate<E>() {
            @Override
            public E execute(Transaction t) throws Exception {
                AbaRefTranlocal<E> tranlocal = (AbaRefTranlocal) ((AlphaTransaction) t).load(AbaRef.this);
                return clear(tranlocal);
            }
        }.execute();
    }

    @Override
    public AbaRefTranlocal<E> ___loadUpdatable(long readVersion) {
        AbaRefTranlocal<E> origin = (AbaRefTranlocal) ___load(readVersion);
        if (origin == null) {
            return new AbaRefTranlocal<E>(this);
        } else {
            return new AbaRefTranlocal<E>(origin);
        }
    }

    public E clear(AbaRefTranlocal<E> tranlocal) {
        E oldValue = tranlocal.value;
        set(tranlocal, null);
        return oldValue;
    }

    public boolean isNull(AbaRefTranlocal<E> tranlocal) {
        return tranlocal.value == null;
    }

    public E get(AbaRefTranlocal<E> tranlocal) {
        return tranlocal.value;
    }

    public void set(AbaRefTranlocal<E> tranlocal, E newValue) {
        if (tranlocal.___committed) {
            throw new ReadonlyException();
        }
        tranlocal.value = newValue;
        tranlocal.writeVersion++;
    }
}

class AbaRefTranlocal<E> extends AlphaTranlocal {
    AbaRef<E> atomicObject;
    E value;
    long writeVersion;
    AbaRefTranlocal origin;

    AbaRefTranlocal(AbaRefTranlocal<E> origin) {
        this.___version = origin.___version;
        this.atomicObject = origin.atomicObject;
        this.value = origin.value;
        this.writeVersion = origin.writeVersion;
        this.origin = origin;
    }

    AbaRefTranlocal(AbaRef<E> atomicObject) {
        this(atomicObject, null);
    }

    AbaRefTranlocal(AbaRef<E> owner, E value) {
        this.___version = Long.MIN_VALUE;
        this.atomicObject = owner;
        this.value = value;
        this.writeVersion = Long.MIN_VALUE;
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
        return new AbaRefTranlocalSnapshot<E>(this);
    }

    @Override
    public DirtinessStatus getDirtinessStatus() {
        if (___committed) {
            return DirtinessStatus.committed;
        } else if (origin == null) {
            return DirtinessStatus.fresh;
        } else if (origin.value != this.value) {
            return DirtinessStatus.dirty;
        } else if (origin.writeVersion != this.writeVersion) {
            return DirtinessStatus.dirty;
        } else {
            return DirtinessStatus.clean;
        }
    }
}

class AbaRefTranlocalSnapshot<E> extends AlphaTranlocalSnapshot {

    final AbaRefTranlocal<E> tranlocal;
    final E value;
    final long writeVersion;

    AbaRefTranlocalSnapshot(AbaRefTranlocal<E> tranlocalAbaRef) {
        this.tranlocal = tranlocalAbaRef;
        this.value = tranlocalAbaRef.value;
        this.writeVersion = tranlocalAbaRef.writeVersion;
    }

    @Override
    public AlphaTranlocal getTranlocal() {
        return tranlocal;
    }

    @Override
    public void restore() {
        tranlocal.writeVersion = writeVersion;
        tranlocal.value = value;
    }
}
