package org.multiverse.stms.beta;

import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.FailedToObtainLocksException;
import org.multiverse.api.exceptions.PanicError;
import org.multiverse.stms.AbstractTransaction;
import org.multiverse.utils.atomicobjectlocks.AtomicObjectLockPolicy;
import static org.multiverse.utils.atomicobjectlocks.AtomicObjectLockUtils.releaseLocks;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link BetaTransaction}
 *
 * @author Peter Veentjer.
 */
public final class UpdateBetaTransaction extends AbstractTransaction implements BetaTransaction {

    private Map<BetaRef, BetaRefTranlocal> privatized = new IdentityHashMap<BetaRef, BetaRefTranlocal>(2);

    public UpdateBetaTransaction(AtomicLong clock, AtomicObjectLockPolicy lockPolicy) {
        super(clock, lockPolicy);

        init();
    }

    @Override
    protected void onInit() {
        if (privatized != null) {
            privatized.clear();
        }
    }

    @Override
    public <E> void attachNew(BetaRefTranlocal<E> refTranlocal) {
        switch (status) {
            case active:
                if (refTranlocal == null) {
                    throw new NullPointerException();
                }

                BetaRef ref = refTranlocal.getBetaRef();
                BetaRefTranlocal found = privatized.get(ref);
                if (found == null) {
                    privatized.put(ref, refTranlocal);
                } else if (found != refTranlocal) {
                    throw new PanicError();
                }
                break;
            case committed:
                throw new DeadTransactionException("Can't attachAsNew on a committed transaction");
            case aborted:
                throw new DeadTransactionException("Can't attachAsNew on an aborted transaction");
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public <E> BetaRefTranlocal<E> privatize(BetaRef<E> ref) {
        switch (status) {
            case active:
                if (ref == null) {
                    throw new NullPointerException();
                }

                BetaRefTranlocal tranlocal = privatized.get(ref);
                if (tranlocal != null) {
                    return tranlocal;
                }

                tranlocal = ref.privatize(readVersion);
                privatized.put(ref, tranlocal);
                return tranlocal;
            case committed:
                throw new DeadTransactionException("Can't privatize on a committed transaction");
            case aborted:
                throw new DeadTransactionException("Can't pribatize on an aborted transaction");
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public <E> BetaRefTranlocal<E> load(BetaRef<E> ref) {
        switch (status) {
            case active:
                if (ref == null) {
                    throw new NullPointerException();
                }

                BetaRefTranlocal tranlocal = privatized.get(ref);
                if (tranlocal != null) {
                    return tranlocal;
                }

                return ref.load(readVersion);
            case committed:
                throw new DeadTransactionException("Can't load on a committed transaction");
            case aborted:
                throw new DeadTransactionException("Can't load on an aborted transaction");
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    protected long onCommit() {
        if (privatized.isEmpty()) {
            return readVersion;
        } else {
            BetaRefTranlocal[] writeSet = acquireAllLocksAndCheckForConflicts();

            long commitVersion = clock.incrementAndGet();
            writeAllChanges(writeSet, commitVersion);
            releaseLocks(writeSet, this);
            return commitVersion;
        }
    }

    /**
     * Acquires the locks and checks for writeconflicts.
     *
     * @return the writeset. An array containing the tranlocal (even index), and dirtinessstate (odd index). The
     *         array is 'empty' after the first null element is found or the end of the array is reached. Null is
     *         returned to indicate that the locks could not be obtained.
     */
    private BetaRefTranlocal[] acquireAllLocksAndCheckForConflicts() {
        BetaRefTranlocal[] writeSet = new BetaRefTranlocal[privatized.size()];

        boolean success = false;
        try {
            int k = 0;
            for (BetaRefTranlocal tranlocal : privatized.values()) {
                switch (tranlocal.getDirtinessState()) {
                    case clean:
                        break;
                    case dirty:
                        if (!tranlocal.getBetaRef().acquireLockAndDetectWriteConflict(this)) {
                            throw FailedToObtainLocksException.create();
                        } else {
                            writeSet[k] = tranlocal;
                            k++;
                        }
                        break;
                    case fresh:
                        writeSet[k] = tranlocal;
                        k++;
                        break;
                    default:
                        throw new IllegalStateException();
                }
            }

            success = true;
            return writeSet;
        } finally {
            if (!success) {
                releaseLocks(writeSet, this);
            }
        }
    }

    private void writeAllChanges(BetaRefTranlocal[] writeSet, long commitVersion) {
        for (int k = 0; k < writeSet.length; k++) {
            BetaRefTranlocal tranlocal = writeSet[k];

            if (tranlocal == null) {
                return;
            }

            tranlocal.signalCommit(commitVersion);
            tranlocal.getBetaRef().write(tranlocal);
        }
    }

    @Override
    protected void onAbort() {
        privatized.clear();
    }
}
