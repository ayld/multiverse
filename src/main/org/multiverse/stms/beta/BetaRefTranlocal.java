package org.multiverse.stms.beta;

import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.utils.TodoException;
import org.multiverse.utils.atomicobjectlocks.AtomicObjectLock;

/**
 * The Transaction local content of a {@link BetaRef}. So one BetaRef has one or more BetaRefTranlocals (or zero when
 * no commit is made).
 *
 * @author Peter Veentjer.
 * @param <E>
 */
public final class BetaRefTranlocal<E> implements AtomicObjectLock {

    //the original BetaRef
    private final BetaRef<E> betaRef;

    private BetaRefTranlocal<E> original;

    private long commitVersion = Long.MIN_VALUE;

    private E reference;

    public BetaRefTranlocal(BetaRef<E> betaRef) {
        this.betaRef = betaRef;
    }

    public BetaRefTranlocal(BetaRef<E> betaRef, E reference) {
        this.betaRef = betaRef;
        this.reference = reference;
    }

    public BetaRefTranlocal(BetaRefTranlocal<E> original) {
        this.original = original;
        this.betaRef = original.getBetaRef();
        this.reference = original.reference;
    }

    public BetaRef<E> getBetaRef() {
        return betaRef;
    }

    public E get() {
        return reference;
    }

    public Boolean isNull() {
        return reference == null;
    }

    public E clear() {
        return set(null);
    }

    public E set(E newReference) {
        if (commitVersion != Long.MIN_VALUE) {
            throw new ReadonlyException();
        }

        E oldReference = reference;
        reference = newReference;
        return oldReference;
    }


    public DirtinessStatus getDirtinessState() {
        if (original == null) {
            return DirtinessStatus.fresh;
        } else if (original.reference != reference) {
            return DirtinessStatus.dirty;
        } else {
            return DirtinessStatus.clean;
        }
    }

    public long getCommitVersion() {
        return commitVersion;
    }

    public void signalCommit(long commitVersion) {
        this.original = null;
        this.commitVersion = commitVersion;
    }

    @Override
    public boolean tryLock(Transaction lockOwner) {
        //return betaRef.acquireLockAndDetectWriteConflict();
        throw new TodoException();
    }

    @Override
    public void releaseLock(Transaction expectedLockOwner) {
        betaRef.releaseLock((BetaTransaction) expectedLockOwner);
    }
}
