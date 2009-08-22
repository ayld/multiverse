package org.multiverse.stms.beta;

import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.LoadLockedException;
import org.multiverse.api.exceptions.LoadTooOldVersionException;
import org.multiverse.api.exceptions.LoadUncommittedException;
import org.multiverse.api.exceptions.WriteConflictException;
import org.multiverse.datastructures.refs.ManagedRef;
import org.multiverse.templates.AtomicTemplate;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * A {@link ManagedRef} specific for the {@link BetaStm}. Other ManagedRef implementation can not be used
 * in combination with the {@link BetaStm}.
 *
 * @param <E>
 */
public final class BetaRef<E> implements ManagedRef<E> {

    private final static AtomicReferenceFieldUpdater<BetaRef, BetaRefTranlocal> tranlocalUpdater =
            AtomicReferenceFieldUpdater.newUpdater(BetaRef.class, BetaRefTranlocal.class, "tranlocal");

    private final static AtomicReferenceFieldUpdater<BetaRef, BetaTransaction> lockOwnerUpdater =
            AtomicReferenceFieldUpdater.newUpdater(BetaRef.class, BetaTransaction.class, "lockOwner");

    private volatile BetaRefTranlocal<E> tranlocal;

    private volatile BetaTransaction lockOwner;

    public BetaRef() {
        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                ((BetaTransaction) t).attachNew(new BetaRefTranlocal(BetaRef.this));
                return null;
            }
        }.execute();
    }

    public BetaRef(BetaTransaction t) {
        t.attachNew(new BetaRefTranlocal(this));
    }

    public BetaRef(final E reference) {
        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                ((BetaTransaction) t).attachNew(new BetaRefTranlocal(BetaRef.this, reference));
                return null;
            }
        }.execute();
    }

    public BetaRef(BetaTransaction t, E reference) {
        t.attachNew(new BetaRefTranlocal(this, reference));
    }

    @Override
    public E get() {
        return new AtomicTemplate<E>() {
            @Override
            public E execute(Transaction t) throws Exception {
                return get((BetaTransaction) t);
            }
        }.execute();
    }

    public E get(BetaTransaction t) {
        BetaRefTranlocal<E> tranlocal = t.load(this);
        return tranlocal.get();
    }

    @Override
    public E getOrAwait() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E set(final E newReference) {
        return new AtomicTemplate<E>() {
            @Override
            public E execute(Transaction t) throws Exception {
                return set((BetaTransaction) t, newReference);
            }
        }.execute();
    }

    public E set(BetaTransaction t, E newReference) {
        BetaRefTranlocal<E> original = t.load(BetaRef.this);
        if (original.get() == newReference) {
            return newReference;
        }

        BetaRefTranlocal<E> tranlocal = t.privatize(BetaRef.this);
        return tranlocal.set(newReference);
    }

    @Override
    public E clear() {
        return new AtomicTemplate<E>() {
            @Override
            public E execute(Transaction t) throws Exception {
                return clear((BetaTransaction) t);
            }
        }.execute();
    }

    public E clear(BetaTransaction t) {
        BetaRefTranlocal<E> original = t.load(BetaRef.this);
        if (original.get() == null) {
            return null;
        }

        BetaRefTranlocal<E> tranlocal = t.privatize(BetaRef.this);
        return tranlocal.clear();
    }

    @Override
    public boolean isNull() {
        return new AtomicTemplate<Boolean>() {
            @Override
            public Boolean execute(Transaction t) throws Exception {
                return isNull((BetaTransaction) t);

            }
        }.execute();
    }

    public boolean isNull(BetaTransaction t) {
        return t.load(BetaRef.this).isNull();
    }

    public BetaRefTranlocal load(long version) {
        BetaRefTranlocal tranlocal = tranlocalUpdater.get(this);

        if (tranlocal == null) {
            throw new LoadUncommittedException();
        } else if (tranlocal.getCommitVersion() == version) {
            return tranlocal;
        } else if (tranlocal.getCommitVersion() < version) {
            if (isLocked()) {
                //we are not able to determine if the tranlocal is the one we want
                throw LoadLockedException.create();
            } else {
                //read the tranlocal content again.
                BetaRefTranlocal tranlocal2 = tranlocalUpdater.get(this);

                if (tranlocal2 == tranlocal) {
                    //the tranlocal we have read is unlocked, so it is the one we want
                    return tranlocal;
                } else if (tranlocal2.getCommitVersion() == version) {
                    //we are lucky because tranlocal2 is the version we want
                    return tranlocal2;
                } else {
                    throw LoadLockedException.create();
                }
            }
        } else {
            throw LoadTooOldVersionException.create();
        }
    }

    private boolean isLocked() {
        return lockOwnerUpdater.get(this) != null;
    }

    public BetaRefTranlocal privatize(long version) {
        BetaRefTranlocal<E> original = load(version);
        return new BetaRefTranlocal(original);
    }

    public void write(BetaRefTranlocal tranlocal) {
        tranlocalUpdater.set(this, tranlocal);
    }

    public boolean acquireLockAndDetectWriteConflict(BetaTransaction owner) {
        if (!acquireLock(owner)) {
            return false;
        } else {
            BetaRefTranlocal betaRefTranlocal = tranlocalUpdater.get(this);

            if (betaRefTranlocal.getCommitVersion() > owner.getReadVersion()) {
                releaseLock(owner);
                throw WriteConflictException.create();
            }

            return true;
        }
    }

    private boolean acquireLock(BetaTransaction owner) {
        return lockOwnerUpdater.compareAndSet(this, null, owner);
    }

    public void releaseLock(BetaTransaction owner) {
        lockOwnerUpdater.compareAndSet(this, owner, null);
    }
}
