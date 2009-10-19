package org.multiverse.stms.alpha;

import org.multiverse.MultiverseConstants;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.FailedToObtainLocksException;
import org.multiverse.api.exceptions.LoadLockedException;
import org.multiverse.api.exceptions.LoadTooOldVersionException;
import org.multiverse.api.exceptions.NoProgressPossibleException;
import org.multiverse.api.exceptions.PanicError;
import org.multiverse.api.exceptions.WriteConflictException;
import org.multiverse.stms.AbstractTransaction;
import org.multiverse.utils.clock.Clock;
import org.multiverse.utils.commitlock.CommitLockPolicy;
import static org.multiverse.utils.commitlock.CommitLockUtils.nothingToLock;
import static org.multiverse.utils.commitlock.CommitLockUtils.releaseLocks;
import org.multiverse.utils.latches.CheapLatch;
import org.multiverse.utils.latches.Latch;
import org.multiverse.utils.profiling.ProfileRepository;

import static java.lang.String.format;
import java.util.IdentityHashMap;
import java.util.Map;

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
public class UpdateAlphaTransaction extends AbstractTransaction
        implements AlphaTransaction, MultiverseConstants {

    private final static AlphaTranlocal[] EMPTY_WRITESET = new AlphaTranlocal[0];

    private final ProfileRepository profiler;

    //the attached set contains the Translocals loaded and attached.
    private final Map<AlphaAtomicObject, AlphaTranlocal> attached = new IdentityHashMap<AlphaAtomicObject, AlphaTranlocal>(2);

    private SnapshotStack snapshotStack;

    public UpdateAlphaTransaction(
            String familyName, ProfileRepository profiler, Clock clock,
            CommitLockPolicy writeSetLockPolicy
    ) {
        super(familyName, clock, writeSetLockPolicy);
        this.profiler = profiler;
        init();
    }

    protected void onInit() {
        this.snapshotStack = null;
        this.attached.clear();

        if (profiler != null) {
            profiler.incCounter("updatetransaction.started.count", getFamilyName());
        }
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

                AlphaAtomicObject atomicObject = tranlocal.getAtomicObject();
                attached.put(atomicObject, tranlocal);
                if (profiler != null) {
                    profiler.incCounter("updatetransaction.attachAsNew.count", getFamilyName());
                    profiler.incCounter("atomicobject.attachAsNew.count", atomicObject.getClass().getName());
                }
                break;
            case committed: {
                String msg = format("Can't call attachNew with on committed transaction '%s'.", familyName);
                throw new DeadTransactionException(msg);
            }
            case aborted: {
                String msg = format("Can't call attachNew on an aborted transaction '%s'.", familyName);
                throw new DeadTransactionException(msg);
            }
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
            case committed: {
                String msg = format("Can't call isAttached on committed transaction '%s'.", getFamilyName());
                throw new DeadTransactionException(msg);
            }
            case aborted: {
                String msg = format("Can't call isAttached on aborted transaction '%s'.", getFamilyName());
                throw new DeadTransactionException(msg);
            }
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
                            profiler.incCounter("atomicobject.snapshottooold.count", atomicObject.getClass().getName());
                            profiler.incCounter("updatetransaction.snapshottooold.count", getFamilyName());
                            throw e;
                        } catch (LoadLockedException e) {
                            profiler.incCounter("atomicobject.lockedload.count", atomicObject.getClass().getName());
                            profiler.incCounter("updatetransaction.failedtolock.count", getFamilyName());
                            throw e;
                        }
                    }

                    attached.put(atomicObject, tranlocal);

                    if (profiler != null) {
                        profiler.incCounter("atomicobject.load.count", atomicObject.getClass().getName());
                        profiler.incCounter("updatetransaction.load.count", getFamilyName());
                    }
                } else {
                    if (profiler != null) {
                        profiler.incCounter("atomicobject.uselessload.count", atomicObject.getClass().getName());
                        profiler.incCounter("updatetransaction.uselessload.count", getFamilyName());
                    }
                }

                return tranlocal;
            case committed: {
                String msg = format("Can't call load with atomicobject of class '%s' on committed transaction '%s'.",
                        atomicObject.getClass().getName(), familyName);
                throw new DeadTransactionException(msg);
            }
            case aborted: {
                String msg = format("Can't call load with atomicObject of class '%s' aborted transaction '%s'.",
                        atomicObject.getClass().getName(), familyName);
                throw new DeadTransactionException(msg);
            }
            default:
                throw new RuntimeException();
        }
    }

    @Override
    protected long onCommit() {
        long commitVersion = doCommit();
        if (profiler != null) {
            profiler.incCounter("updatetransaction.committed.count", getFamilyName());
        }
        attached.clear();
        return commitVersion;
    }

    private long doCommit() {
        AlphaTranlocal[] writeSet = createWriteSet();
        if (nothingToLock(writeSet)) {
            //if there is nothing to commit, we are done.
            if (profiler != null) {
                profiler.incCounter("updatetransaction.emptycommit.count", getFamilyName());
            }
            return readVersion;
        }

        boolean success = false;
        try {
            acquireLocksAndCheckForConflicts(writeSet);
            long writeVersion = clock.tick();

            if(SANITY_CHECKS_ENABLED){
                if(writeVersion<=readVersion){
                    throw new PanicError("The clock went back in time");
                }
            }

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
                        profiler.incCounter("atomicobject.dirty.count", tranlocal.getAtomicObject().getClass().getName());
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
                        profiler.incCounter("atomicobject.conflict.count", tranlocal.getAtomicObject().getClass().getName());
                        profiler.incCounter("updatetransaction.writeconflict.count", getFamilyName());
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
                    profiler.incCounter("updatetransaction.acquirelocks.count", getFamilyName());
                }
                break;
            case failure:
                if (profiler != null) {
                    profiler.incCounter("updatetransaction.failedtoacquirelocks.count", getFamilyName());
                }

                throw FailedToObtainLocksException.create();
            case conflict:
                if (profiler != null) {
                    profiler.incCounter("updatetransaction.writeconflict.count", getFamilyName());
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
                profiler.incCounter("updatetransaction.individualwrite.count", getFamilyName(), attached.size());
            }
        }
    }

    @Override
    protected void onAbort() {
        attached.clear();
        if (profiler != null) {
            profiler.incCounter("updatetransaction.aborted.count", getFamilyName());
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
                profiler.incCounter("updatetransaction.retried.count", getFamilyName());
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
            profiler.incCounter("updatetransaction.waiting.count", getFamilyName());
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
                profiler.decCounter("updatetransaction.waiting.count", getFamilyName());
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