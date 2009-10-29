package org.multiverse.datastructures.refs.manual;

import org.multiverse.api.GlobalStmInstance;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import org.multiverse.api.Stm;
import static org.multiverse.api.StmUtils.retry;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.LoadUncommittedException;
import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.datastructures.refs.ManagedRef;
import org.multiverse.stms.alpha.*;
import static org.multiverse.stms.alpha.AlphaStmUtils.getLoadUncommittedMessage;
import org.multiverse.stms.alpha.mixins.FastAtomicObjectMixin;
import org.multiverse.templates.AtomicTemplate;

import static java.lang.String.format;

/**
 * A manual instrumented {@link org.multiverse.datastructures.refs.ManagedRef} implementation.
 * If this class is used, you don't need to worry about instrumentation/javaagents and
 * stuff like this.
 * <p/>
 * It is added to get the Akka project up and running, but probably will removed when the instrumentation
 * is 100% up and running and this can be done compiletime instead of messing with javaagents.
 *
 * @author Peter Veentjer
 */
public final class Ref<E> extends FastAtomicObjectMixin implements ManagedRef<E> {


    /**
     * Creates a committed ref with a null value using the Stm in the
     * {@link GlobalStmInstance}.
     *
     * @return the created ref.
     * @see #createCommittedRef(Stm, Object)
     */
    public static <E> Ref<E> createCommittedRef() {
        return createCommittedRef(getGlobalStmInstance(), null);
    }

    /**
     * Creates a committed ref with a null value.
     *
     * @param stm the {@link Stm} used for committing the ref.
     * @return the created ref.
     * @see #createCommittedRef(Stm, Object)
     */
    public static <E> Ref<E> createCommittedRef(Stm stm) {
        return createCommittedRef(stm, null);
    }

    /**
     * Creates a committed ref with the given value using the Stm in the
     * {@link GlobalStmInstance}.
     *
     * @param value the initial value of the Ref.
     * @return the created ref.
     * @see #createCommittedRef(Stm, Object)
     */
    public static <E> Ref<E> createCommittedRef(E value) {
        return createCommittedRef(getGlobalStmInstance(), value);
    }

    /**
     * Creates a committed ref with the given value and using the given Stm.
     * <p/>
     * This factory method should be called when one doesn't want to lift on the current
     * transaction, but you want something to be committed whatever happens. In the future
     * behavior will be added propagation levels. But for the time being this is the 'expect_new'
     * implementation of this propagation level.
     * <p/>
     * If the value is an atomicobject or has a reference to it (perhaps indirectly), and
     * the transaction this atomicobject is created in is aborted (or hasn't committed) yet,
     * you will get the dreaded {@link org.multiverse.api.exceptions.LoadUncommittedException}.
     *
     * @param stm   the {@link Stm} used for committing the ref.
     * @param value the initial value of the ref. The value is allowed to be null.
     * @return the created ref.
     */
    public static <E> Ref<E> createCommittedRef(Stm stm, E value) {
        Transaction t = stm.startUpdateTransaction("createRef");
        Ref<E> ref = new Ref<E>(t, value);
        t.commit();
        return ref;
    }

    public Ref() {
        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                ((AlphaTransaction) t).attachNew(new RefTranlocal(Ref.this));
                return null;
            }
        }.execute();
    }

    public Ref(Transaction t) {
        ((AlphaTransaction) t).attachNew(new RefTranlocal(Ref.this));
    }

    public Ref(final E value) {
        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                ((AlphaTransaction) t).attachNew(new RefTranlocal(Ref.this, value));
                return null;
            }
        }.execute();
    }

    public Ref(Transaction t, final E value) {
        ((AlphaTransaction) t).attachNew(new RefTranlocal(Ref.this, value));
    }

    public E get() {
        return new AtomicTemplate<E>(true) {
            @Override
            public E execute(Transaction t) {
                return get(t);
            }
        }.execute();
    }

    public E get(Transaction t) {
        RefTranlocal<E> tranlocalRef = (RefTranlocal) ((AlphaTransaction) t).load(Ref.this);
        return tranlocalRef.get();
    }

    @Override
    public E getOrAwait() {
        return new AtomicTemplate<E>(true) {
            @Override
            public E execute(Transaction t) throws Exception {
                return getOrAwait(t);
            }
        }.execute();
    }

    public E getOrAwait(Transaction t) {
        RefTranlocal<E> tranlocalRef = (RefTranlocal) ((AlphaTransaction) t).load(Ref.this);
        return tranlocalRef.getOrAwait();
    }


    @Override
    public E set(final E newRef) {
        return new AtomicTemplate<E>() {
            @Override
            public E execute(Transaction t) throws Exception {
                return set(t, newRef);
            }
        }.execute();
    }

    public E set(Transaction t, final E newRef) {
        RefTranlocal<E> tranlocalRef = (RefTranlocal) ((AlphaTransaction) t).load(Ref.this);
        return tranlocalRef.set(newRef);
    }

    @Override
    public boolean isNull() {
        return new AtomicTemplate<Boolean>(true) {
            @Override
            public Boolean execute(Transaction t) throws Exception {
                return isNull(t);
            }
        }.execute();
    }

    public boolean isNull(Transaction t) {
        RefTranlocal<E> tranlocalRef = (RefTranlocal) ((AlphaTransaction) t).load(Ref.this);
        return tranlocalRef.isNull();
    }

    @Override
    public E clear() {
        return new AtomicTemplate<E>() {
            @Override
            public E execute(Transaction t) throws Exception {
                return clear(t);
            }
        }.execute();
    }

    public E clear(Transaction t) {
        RefTranlocal<E> tranlocalRef = (RefTranlocal) ((AlphaTransaction) t).load(Ref.this);
        return tranlocalRef.clear();
    }

    @Override
    public String toString() {
        return new AtomicTemplate<String>(true) {
            @Override
            public String execute(Transaction t) throws Exception {
                return Ref.this.toString(t);
            }

        }.execute();
    }

    public String toString(Transaction t) {
        RefTranlocal<E> tranlocalRef = (RefTranlocal) ((AlphaTransaction) t).load(Ref.this);
        return tranlocalRef.toString();
    }

    @Override
    public RefTranlocal<E> privatize(long readVersion) {
        RefTranlocal<E> origin = (RefTranlocal<E>) load(readVersion);
        if (origin == null) {
            throw new LoadUncommittedException(getLoadUncommittedMessage(this));
        }
        return new RefTranlocal<E>(origin);
    }
}

class RefTranlocal<E> extends AlphaTranlocal {
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
    public AlphaAtomicObject getAtomicObject() {
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
    public String toString() {
        if (ref == null) {
            return "Ref(reference=null)";
        } else {
            return format("Ref(reference=%s)", ref);
        }
    }

    @Override
    public void prepareForCommit(long writeVersion) {
        this.version = writeVersion;
        this.committed = true;
        this.origin = null;
    }

    @Override
    public AlphaTranlocalSnapshot takeSnapshot() {
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

class RefTranlocalSnapshot<E> extends AlphaTranlocalSnapshot {
    final RefTranlocal tranlocal;
    final E value;

    RefTranlocalSnapshot(RefTranlocal<E> tranlocal) {
        this.tranlocal = tranlocal;
        this.value = tranlocal.ref;
    }

    @Override
    public AlphaTranlocal getTranlocal() {
        return tranlocal;
    }

    @Override
    public void restore() {
        tranlocal.ref = value;
    }
}