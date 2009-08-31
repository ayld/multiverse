package org.multiverse.stms.alpha;

import org.multiverse.MultiverseConstants;
import org.multiverse.api.exceptions.*;
import org.multiverse.stms.AbstractTransaction;
import org.multiverse.utils.commitlock.CommitLockPolicy;
import static org.multiverse.utils.commitlock.CommitLockUtils.nothingToLock;
import static org.multiverse.utils.commitlock.CommitLockUtils.releaseLocks;
import org.multiverse.utils.latches.CheapLatch;
import org.multiverse.utils.latches.Latch;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link org.multiverse.api.Transaction} implementation that is used to do updates. It can also be used for
 * reaonly transaction, but a {@link ReadonlyAlphaTransaction} would be a better candidate
 * for that.
 * <p/>
 * Comment about design:
 * A state design pattern would have been a solution to reduce the switch statements,
 * but to prevent object creation, this is not done.
 *
 * @author Peter Veentjer.
 */
public class UpdateAlphaTransaction extends AbstractTransaction implements AlphaTransaction {

    private final AlphaStmStatistics statistics;

    //the attached set contains the Translocals loaded and attached.
    private Map<AlphaAtomicObject, AlphaTranlocal> attached = new IdentityHashMap(2);

    private SnapshotStack snapshotStack;

    public UpdateAlphaTransaction(String familyName, AlphaStmStatistics statistics, AtomicLong clock, CommitLockPolicy writeSetLockPolicy) {
        super(familyName, clock, writeSetLockPolicy);
        this.statistics = statistics;
        init();
    }

    protected void onInit() {
        this.snapshotStack = null;
        this.attached.clear();

        if (statistics != null) {
            statistics.incTransactionStartedCount();
        }
    }

    @Override
    public void onRetry() {
        throw RetryError.create();
    }

    @Override
    public void attachNew(AlphaTranlocal tranlocal) {
        switch (status) {
            case active:
                //System.out.println("attachNew is called");

                if (tranlocal == null) {
                    throw new NullPointerException();
                }

                if (MultiverseConstants.SANITY_CHECK_ENABLED) {
                    if (tranlocal.getAtomicObject() == null) {
                        throw new PanicError();
                    }

                    if (tranlocal.committed) {
                        throw new PanicError();
                    }

                    DirtinessStatus dirtinessStatus = tranlocal.getDirtinessStatus();
                    if (dirtinessStatus != DirtinessStatus.fresh && dirtinessStatus != DirtinessStatus.clean) {
                        throw new PanicError("Found dirtinessState: " + dirtinessStatus);
                    }
                }

                attached.put(tranlocal.getAtomicObject(), tranlocal);

                if (statistics != null) {
                    statistics.incAttachNewCount();
                }

                break;
            case committed:
                throw new DeadTransactionException("Can't call attachNew on a committed transaction");
            case aborted:
                throw new DeadTransactionException("Can't call attachNew on an aborted transaction");
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public AlphaTranlocal load(AlphaAtomicObject atomicObject) {
        switch (status) {
            case active:
                if (atomicObject == null) {
                    return null;
                }

                AlphaTranlocal existing = attached.get(atomicObject);
                if (existing != null) {
                    return existing;
                }

                if (statistics == null) {
                    AlphaTranlocal loaded = atomicObject.load(readVersion);
                    if (loaded == null) {
                        throw new LoadUncommittedException();
                    }
                    return loaded;
                } else {
                    try {
                        AlphaTranlocal loaded = atomicObject.load(readVersion);
                        if (loaded == null) {
                            throw new LoadUncommittedException();
                        }
                        return loaded;
                    } catch (LoadTooOldVersionException e) {
                        statistics.incTransactionSnapshotTooOldCount();
                        throw e;
                    } catch (LoadLockedException e) {
                        //todo
                        statistics.incTransactionSnapshotTooOldCount();
                        throw e;
                    }
                }
            case committed:
                throw new DeadTransactionException("Can't call loadReadonly on a committed transaction");
            case aborted:
                throw new DeadTransactionException("Can't call loadReadonly on an aborted transaction");
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public AlphaTranlocal privatize(AlphaAtomicObject atomicObject) {
        switch (status) {
            case active:
                if (atomicObject == null) {
                    return null;
                }

                AlphaTranlocal tranlocal = attached.get(atomicObject);
                if (tranlocal == null) {
                    if (statistics == null) {
                        tranlocal = atomicObject.privatize(readVersion);
                    } else {
                        try {
                            tranlocal = atomicObject.privatize(readVersion);
                        } catch (LoadTooOldVersionException e) {
                            statistics.incTransactionSnapshotTooOldCount();
                            throw e;
                        } catch (LoadLockedException e) {
                            //todo
                            statistics.incTransactionSnapshotTooOldCount();
                            throw e;
                        }
                    }

                    attached.put(atomicObject, tranlocal);

                    if (statistics != null) {
                        statistics.incLoadCount();
                    }
                }

                return tranlocal;
            case committed:
                throw new DeadTransactionException("Can't call load on a committed transaction.");
            case aborted:
                throw new DeadTransactionException("Can't call load on an aborted transaction");
            default:
                throw new RuntimeException();
        }
    }

    @Override
    protected long onCommit() {
        long commitVersion = doCommit();
        if (statistics != null) {
            statistics.incTransactionCommittedCount();
        }
        attached.clear();
        return commitVersion;
    }

    private long doCommit() {
        //System.out.println("starting commit");
        AlphaTranlocal[] writeSet = createWriteSet();
        if (nothingToLock(writeSet)) {
            //if there is nothing to commit, we are done.
            if (statistics != null) {
                statistics.incTransactionEmptyCommitCount();
            }
            return readVersion;
        } else {

            boolean success = false;
            try {
                acquireLocksAndCheckForConflicts(writeSet);
                long writeVersion = clock.incrementAndGet();
                storeAll(writeSet, writeVersion);
                success = true;
                return writeVersion;
            } finally {
                if (!success) {
                    releaseLocks(writeSet, this);
                }
            }
        }
    }

    /**
     * Creates the writeset; a set of objects which state needs to be committed.
     *
     * @return the created WriteSet or null if there is nothing that needs to be written.
     * @throws org.multiverse.api.exceptions.WriteConflictException
     *          if can be determined that another transaction did a conflicting write.
     */
    private AlphaTranlocal[] createWriteSet() {
        if (attached.isEmpty()) {
            return null;
        }

        AlphaTranlocal[] writeSet = null;

        int skipped = 0;
        int index = 0;
        for (AlphaTranlocal tranlocal : attached.values()) {
            switch (tranlocal.getDirtinessStatus()) {
                case committed:
                    skipped++;
                    break;
                case fresh:
                    //fall through
                case dirty:
                    if (writeSet == null) {
                        writeSet = new AlphaTranlocal[attached.size() - skipped];
                    }
                    writeSet[index] = tranlocal;
                    index++;
                    break;
                case clean:
                    skipped++;
                    break;
                case conflict:
                    //if we can already determine that the write can never happen, start a write conflict
                    //and fail immediately.
                    if (statistics != null) {
                        statistics.incTransactionWriteConflictCount();
                    }
                    throw WriteConflictException.create();
                default:
                    throw new RuntimeException();
            }
        }

        return writeSet;
    }

    private void acquireLocksAndCheckForConflicts(AlphaTranlocal[] writeSet) {
        switch (commitLockPolicy.tryLockAllAndDetectConflicts(writeSet, this)) {
            case success:
                //todo: problem is that if the locks are not acquired successfully, it isn't clear
                //how many locks were acquired.
                if (statistics != null) {
                    statistics.incLockAcquiredCount(writeSet.length);
                }
                break;
            case failure:
                if (statistics != null) {
                    statistics.incTransactionFailedToAcquireLocksCount();
                }

                throw FailedToObtainLocksException.create();
            case conflict:
                if (statistics != null) {
                    statistics.incTransactionWriteConflictCount();
                }
                throw WriteConflictException.create();
            default:
                throw new RuntimeException();
        }
    }

    private void storeAll(AlphaTranlocal[] writeSet, long commitVersion) {
        try {
            for (int k = 0; k < writeSet.length; k++) {
                AlphaTranlocal tranlocal = writeSet[k];
                if (tranlocal == null) {
                    return;
                } else {
                    AlphaAtomicObject atomicObject = tranlocal.getAtomicObject();
                    atomicObject.storeAndReleaseLock(tranlocal, commitVersion);
                }
            }

        } finally {
            if (statistics != null) {
                statistics.incWriteCount(attached.size());
            }
        }
    }

    @Override
    protected void onAbort() {
        attached.clear();
        if (statistics != null) {
            statistics.incTransactionAbortedCount();
        }
    }

    @Override
    protected void onAbortAndRetry() {
        boolean success = false;
        try {
            awaitInterestingWrite();

            //we are finished waiting, lets reset the transaction and begin again
            init();
            success = true;

            if (statistics != null) {
                statistics.incTransactionRetriedCount();
            }
        } finally {
            if (!success) {
                doAbort();
            }
        }

    }

    private void awaitInterestingWrite() {
        if (attached.isEmpty()) {
            throw new NoProgressPossibleException();
        }

        if (statistics != null) {
            statistics.incTransactionPendingRetryCount();
        }

        try {
            //lets register the listener
            Latch listener = new CheapLatch();
            long minimalVersion = readVersion + 1;

            boolean atLeastOne = false;
            for (AlphaAtomicObject atomicObject : attached.keySet()) {
                if (atomicObject.registerRetryListener(listener, minimalVersion)) {
                    atLeastOne = true;

                    if (listener.isOpen()) {
                        break;
                    }
                }
            }

            if (!atLeastOne) {
                throw new NoProgressPossibleException();
            } else {

                //wait for the other transactions to do a write we are interested in.
                listener.awaitUninterruptible();
            }
        } finally {
            if (statistics != null) {
                statistics.decTransactionPendingRetryCount();
            }
        }
    }

    @Override
    protected void onStartOr() {
        snapshotStack = new SnapshotStack(snapshotStack, createSnapshot());
    }

    private AlphaTranlocalSnapshot createSnapshot() {
        AlphaTranlocalSnapshot result = null;
        for (AlphaTranlocal tranlocal : attached.values()) {
            AlphaTranlocalSnapshot snapshot = tranlocal.takeSnapshot();
            snapshot.next = result;
            result = snapshot;
        }

        return result;
    }

    @Override
    protected void onEndOr() {
        if (snapshotStack == null) {
            throw new IllegalStateException();
        }
        snapshotStack = snapshotStack.next;
    }

    @Override
    protected void onEndOrAndStartElse() {
        if (snapshotStack == null) {
            throw new IllegalStateException();
        }
        AlphaTranlocalSnapshot snapshot = snapshotStack.snapshot;
        snapshotStack = snapshotStack.next;
        restoreSnapshot(snapshot);

    }

    private void restoreSnapshot(AlphaTranlocalSnapshot snapshot) {
        attached.clear();

        while (snapshot != null) {
            AlphaTranlocal tranlocal = snapshot.getTranlocal();
            attached.put(tranlocal.getAtomicObject(), tranlocal);
            snapshot.restore();
            snapshot = snapshot.next;
        }
    }

    static final class SnapshotStack {
        public final SnapshotStack next;
        public final AlphaTranlocalSnapshot snapshot;

        SnapshotStack(SnapshotStack next, AlphaTranlocalSnapshot snapshot) {
            this.next = next;
            this.snapshot = snapshot;
        }
    }
}