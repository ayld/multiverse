package org.multiverse.stms.beta;

import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.FailedToObtainLocksException;
import org.multiverse.api.exceptions.PanicError;
import org.multiverse.stms.AbstractTransaction;
import org.multiverse.utils.commitlock.CommitLockPolicy;
import static org.multiverse.utils.commitlock.CommitLockUtils.releaseLocks;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link BetaTransaction}
 *
 * @author Peter Veentjer.
 */
public final class UpdateBetaTransaction extends AbstractTransaction implements BetaTransaction {

    private Map<BetaRef, BetaRefTranlocal> readwriteSet = new IdentityHashMap<BetaRef, BetaRefTranlocal>(2);

    public UpdateBetaTransaction(String familyName, AtomicLong clock, CommitLockPolicy lockPolicy) {
        super(familyName, clock, lockPolicy);

        init();
    }

    @Override
    protected void onInit() {
        if (readwriteSet != null) {
            readwriteSet.clear();
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
                BetaRefTranlocal found = readwriteSet.get(ref);
                if (found == null) {
                    readwriteSet.put(ref, refTranlocal);
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

                BetaRefTranlocal tranlocal = readwriteSet.get(ref);
                if (tranlocal != null) {
                    return tranlocal;
                }

                tranlocal = ref.privatize(readVersion);
                readwriteSet.put(ref, tranlocal);
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

                BetaRefTranlocal tranlocal = readwriteSet.get(ref);
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
        if (readwriteSet.isEmpty()) {
            return readVersion;
        } else {
            BetaRefTranlocal[] writeSet = acquireAllLocksAndCheckForConflicts();

            long commitVersion = clock.incrementAndGet();
            writeAllChanges(writeSet, commitVersion);
            releaseLocks(writeSet, this);
            return commitVersion;
        }
    }

    /*
    protected BetaRefTranlocal[] createWriteSet() {
        BetaRefTranlocal[] writeSet = null;

        if (!readwriteSet.isEmpty()) {
            int writeSetIndex = 0;
            int skipped = 0;
            for (Map.Entry<BetaRef, BetaRefTranlocal> entry : readwriteSet.entrySet()) {
                BetaRefTranlocal tranlocal = entry.getValue();
                switch (tranlocal.getDirtinessStatus()) {
                    case clean:
                        skipped++;
                        break;
                    case committed:
                        skipped++;
                        break;
                    case fresh:
                        //fall through
                    case dirty:
                        if (writeSet == null) {
                            writeSet = new BetaRefTranlocal[readwriteSet.size() - skipped];
                        }
                        writeSet[writeSetIndex] = tranlocal;
                        writeSetIndex++;
                        break;
                    case conflict:
                        throw WriteConflictException.create();
                    default:
                        throw new IllegalStateException();
                }
            }

        }

        return writeSet;
    } */


    /**
     * Acquires the locks and checks for writeconflicts.
     *
     * @return the writeset. An array containing the tranlocal (even index), and dirtinessstate (odd index). The
     *         array is 'empty' after the first null element is found or the end of the array is reached. Null is
     *         returned to indicate that the locks could not be obtained.
     */
    private BetaRefTranlocal[] acquireAllLocksAndCheckForConflicts() {
        BetaRefTranlocal[] writeSet = new BetaRefTranlocal[readwriteSet.size()];

        boolean success = false;
        try {
            int k = 0;
            for (BetaRefTranlocal tranlocal : readwriteSet.values()) {
                switch (tranlocal.getDirtinessStatus()) {
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
        readwriteSet.clear();
    }
}
