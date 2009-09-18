package org.multiverse.stms.alpha;

import org.multiverse.MultiverseConstants;
import org.multiverse.api.exceptions.*;
import org.multiverse.stms.AbstractTransaction;
import org.multiverse.utils.commitlock.CommitLockPolicy;
import static org.multiverse.utils.commitlock.CommitLockUtils.nothingToLock;
import static org.multiverse.utils.commitlock.CommitLockUtils.releaseLocks;
import org.multiverse.utils.latches.CheapLatch;
import org.multiverse.utils.latches.Latch;
import org.multiverse.utils.profiling.Profiler;

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
public class UpdateAlphaTransaction extends AbstractTransaction implements AlphaTransaction, MultiverseConstants {

    private final static AlphaTranlocal[] EMPTY_WRITESET = new AlphaTranlocal[0];

    private final Profiler profiler;

    //the attached set contains the Translocals loaded and attached.
    private final Map<AlphaAtomicObject, AlphaTranlocal> attached = new IdentityHashMap<AlphaAtomicObject, AlphaTranlocal>(2);

    private SnapshotStack snapshotStack;

    public UpdateAlphaTransaction(String familyName, Profiler profiler, AtomicLong clock, CommitLockPolicy writeSetLockPolicy) {
        super(familyName, clock, writeSetLockPolicy);
        this.profiler = profiler;
        init();
    }

    protected void onInit() {
        this.snapshotStack = null;
        this.attached.clear();

        if (profiler != null) {
            profiler.incCounter(getFamilyName(), "updatetransaction.started");
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
                if (tranlocal == null) {
                    throw new NullPointerException();
                }

                if (SANITY_CHECKS_ENABLED) {
                    AlphaAtomicObject atomicObject = tranlocal.getAtomicObject();

                    if (atomicObject == null) {
                        throw new PanicError();
                    }

                    if (tranlocal.committed) {
                        throw new PanicError();
                    }

                    DirtinessStatus dirtinessStatus = tranlocal.getDirtinessStatus();
                    if (dirtinessStatus != DirtinessStatus.fresh && dirtinessStatus != DirtinessStatus.clean) {
                        throw new PanicError("Found dirtinessState: " + dirtinessStatus);
                    }

                    AlphaTranlocal found = attached.get(atomicObject);
                    if (found != null && found != tranlocal) {
                        throw new PanicError("Duplicate attachment for atomicobject " + atomicObject.getClass());
                    }
                }

                attached.put(tranlocal.getAtomicObject(), tranlocal);
                if (profiler != null) {
                    profiler.incCounter(getFamilyName(), "updatetransaction.attachAsNew");
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
    public boolean isAttached(AlphaAtomicObject atomicObject) {
        switch (status) {
            case active:
                if (atomicObject == null) {
                    throw new NullPointerException();
                }

                return attached.containsKey(atomicObject);
            case committed:
                throw new DeadTransactionException("Can't call isAttached on a committed transaction: " + getFamilyName());
            case aborted:
                throw new DeadTransactionException("Can't call isAttached on an aborted transaction: " + getFamilyName());
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

                AlphaTranlocal tranlocal = attached.get(atomicObject);
                if (tranlocal == null) {
                    if (profiler == null) {
                        tranlocal = atomicObject.privatize(readVersion);
                    } else {
                        try {
                            tranlocal = atomicObject.privatize(readVersion);
                        } catch (LoadTooOldVersionException e) {
                            profiler.incCounter("class.snapshottooold.count", atomicObject.getClass().getName());
                            profiler.incCounter(getFamilyName(), "updatetransaction.snapshottooold.count");
                            throw e;
                        } catch (LoadLockedException e) {
                            profiler.incCounter("class.lockedload.count", atomicObject.getClass().getName());
                            profiler.incCounter(getFamilyName(), "updatetransaction.failedtolock.count");
                            throw e;
                        }
                    }

                    attached.put(atomicObject, tranlocal);

                    if (profiler != null) {
                        profiler.incCounter("class.loadedinstance.count", atomicObject.getClass().getName());
                        profiler.incCounter(getFamilyName(), "updatetransaction.loaded.count");
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
        if (profiler != null) {
            profiler.incCounter(getFamilyName(), "updatetransaction.committed.count");
        }
        attached.clear();
        return commitVersion;
    }

    private long doCommit() {
        AlphaTranlocal[] writeSet = createWriteSet();
        if (nothingToLock(writeSet)) {
            //if there is nothing to commit, we are done.
            if (profiler != null) {
                profiler.incCounter(getFamilyName(), "updatetransaction.emptycommit.count");
            }
            return readVersion;
        }

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

    /**
     * Creates the writeset; a set of objects which state needs to be committed.
     *
     * @return the created WriteSet. The returned value will never be null.
     * @throws org.multiverse.api.exceptions.WriteConflictException
     *          if can be determined that another transaction did a conflicting write.
     */
    private AlphaTranlocal[] createWriteSet() {
        if (attached.isEmpty()) {
            return EMPTY_WRITESET;
        }

        AlphaTranlocal[] writeSet = null;

        int skipped = 0;
        int index = 0;
        for (AlphaTranlocal tranlocal : attached.values()) {
            switch (tranlocal.getDirtinessStatus()) {
                case clean:
                    //fall through
                case committed:
                    skipped++;
                    break;
                case fresh:
                    //fall through
                case dirty:
                    if (profiler != null) {
                        profiler.incCounter("class.dirty.count", tranlocal.getAtomicObject().getClass().getName());
                    }

                    if (writeSet == null) {
                        writeSet = new AlphaTranlocal[attached.size() - skipped];
                    }
                    writeSet[index] = tranlocal;
                    index++;
                    break;
                case conflict:
                    //if we can already determine that the write can never happen, start a write conflict
                    //and fail immediately.
                    if (profiler != null) {
                        profiler.incCounter("class.conflict.count", tranlocal.getAtomicObject().getClass().getName());
                        profiler.incCounter(getFamilyName(), "updatetransaction.writeconflict.count");
                    }
                    throw WriteConflictException.create();
                default:
                    throw new RuntimeException();
            }
        }

        return writeSet == null ? EMPTY_WRITESET : writeSet;
    }

    private void acquireLocksAndCheckForConflicts(AlphaTranlocal[] writeSet) {
        switch (commitLockPolicy.tryLockAllAndDetectConflicts(writeSet, this)) {
            case success:
                //todo: problem is that if the locks are not acquired successfully, it isn't clear
                //how many locks were acquired.
                if (profiler != null) {
                    profiler.incCounter(getFamilyName(), "updatetransaction.acquirelocks.count");
                }
                break;
            case failure:
                if (profiler != null) {
                    profiler.incCounter(getFamilyName(), "updatetransaction.failedtoacquirelocks.count");
                }

                throw FailedToObtainLocksException.create();
            case conflict:
                if (profiler != null) {
                    profiler.incCounter(getFamilyName(), "updatetransaction.writeconflict.count");
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
            if (profiler != null) {
                profiler.incCounter(getFamilyName(), "updatetransaction.individualwrite.count", attached.size());
            }
        }
    }

    @Override
    protected void onAbort() {
        attached.clear();
        if (profiler != null) {
            profiler.incCounter(getFamilyName(), "updatetransaction.aborted.count");
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

            if (profiler != null) {
                profiler.incCounter(getFamilyName(), "updatetransaction.retried.count");
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

        if (profiler != null) {
            profiler.incCounter(getFamilyName(), "updatetransaction.waiting.count");
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
            }
            //wait for the other transactions to do a write we are interested in.
            listener.awaitUninterruptible();
        } finally {
            if (profiler != null) {
                profiler.decCounter(getFamilyName(), "updatetransaction.waiting.count");
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