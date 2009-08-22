package org.multiverse.stms.alpha.manualinstrumentation;

import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.LoadUncommittedException;
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
                ((AlphaTransaction) t).attachNew(new AbaRefTranlocal(AbaRef.this));
                return null;
            }
        }.execute();
    }

    public AbaRef(final E value) {
        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                ((AlphaTransaction) t).attachNew(new AbaRefTranlocal(AbaRef.this, value));
                return null;
            }
        }.execute();
    }

    public E get() {
        return new AtomicTemplate<E>() {
            @Override
            public E execute(Transaction t) throws Exception {
                AbaRefTranlocal<E> tranlocal = (AbaRefTranlocal) ((AlphaTransaction) t).privatize(AbaRef.this);
                return tranlocal.get();
            }
        }.execute();
    }

    public void set(final E newValue) {
        new AtomicTemplate<E>() {
            @Override
            public E execute(Transaction t) throws Exception {
                AbaRefTranlocal<E> tranlocal = (AbaRefTranlocal) ((AlphaTransaction) t).privatize(AbaRef.this);
                tranlocal.set(newValue);
                return null;
            }
        }.execute();
    }

    public boolean isNull() {
        return new AtomicTemplate<Boolean>() {
            @Override
            public Boolean execute(Transaction t) throws Exception {
                AbaRefTranlocal<E> tranlocal = (AbaRefTranlocal) ((AlphaTransaction) t).privatize(AbaRef.this);
                return tranlocal.isNull();
            }
        }.execute();
    }

    public E clear() {
        return new AtomicTemplate<E>() {
            @Override
            public E execute(Transaction t) throws Exception {
                AbaRefTranlocal<E> tranlocal = (AbaRefTranlocal) ((AlphaTransaction) t).privatize(AbaRef.this);
                tranlocal.clear();
                return null;
            }
        }.execute();
    }

    @Override
    public AbaRefTranlocal<E> privatize(long readVersion) {
        AbaRefTranlocal<E> origin = (AbaRefTranlocal) load(readVersion);
        if (origin == null) {
            throw new LoadUncommittedException();
        }
        return new AbaRefTranlocal(origin);
    }
}

class AbaRefTranlocal<E> extends Tranlocal {
    AbaRef<E> atomicObject;
    E value;
    long writeVersion;
    AbaRefTranlocal origin;

    AbaRefTranlocal(AbaRefTranlocal<E> origin) {
        this.version = origin.version;
        this.atomicObject = origin.atomicObject;
        this.value = origin.value;
        this.writeVersion = origin.writeVersion;
        this.origin = origin;
    }

    AbaRefTranlocal(AbaRef<E> atomicObject) {
        this(atomicObject, null);
    }

    AbaRefTranlocal(AbaRef<E> owner, E value) {
        this.version = Long.MIN_VALUE;
        this.atomicObject = owner;
        this.value = value;
        this.writeVersion = Long.MIN_VALUE;
    }

    @Override
    public AlphaAtomicObject getAtomicObject() {
        return atomicObject;
    }

    public E clear() {
        E oldValue = value;
        set(null);
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
        this.writeVersion++;
    }

    @Override
    public void prepareForCommit(long writeVersion) {
        this.version = writeVersion;
        this.committed = true;
        this.origin = null;
    }

    @Override
    public TranlocalSnapshot takeSnapshot() {
        return new AbaRefTranlocalSnapshot<E>(this);
    }

    @Override
    public DirtinessStatus getDirtinessStatus() {
        if (committed) {
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

class AbaRefTranlocalSnapshot<E> extends TranlocalSnapshot {

    final AbaRefTranlocal<E> tranlocal;
    final E value;
    final long writeVersion;

    AbaRefTranlocalSnapshot(AbaRefTranlocal<E> tranlocalAbaRef) {
        this.tranlocal = tranlocalAbaRef;
        this.value = tranlocalAbaRef.value;
        this.writeVersion = tranlocalAbaRef.writeVersion;
    }

    @Override
    public Tranlocal getTranlocal() {
        return tranlocal;
    }

    @Override
    public void restore() {
        tranlocal.writeVersion = writeVersion;
        tranlocal.value = value;
    }
}
