package org.multiverse.stms.alpha;

import org.multiverse.api.TransactionStatus;
import org.multiverse.api.exceptions.*;
import org.multiverse.api.locks.LockManager;
import org.multiverse.api.locks.LockStatus;
import org.multiverse.api.locks.StmLock;
import org.multiverse.utils.TodoException;
import org.multiverse.utils.atomicobjectlocks.AtomicObjectLockPolicy;
import static org.multiverse.utils.atomicobjectlocks.AtomicObjectLockUtils.releaseLocks;
import org.multiverse.utils.latches.CheapLatch;
import org.multiverse.utils.latches.Latch;

import static java.lang.String.format;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link org.multiverse.api.Transaction} implementation that is used to do updates. It can also be used for
 * reaonly transaction, but a {@link ReadonlyTransaction} would be a better candidate
 * for that.
 * <p/>
 * Comment about design:
 * A state design pattern would have been a solution to reduce the switch statements,
 * but to prevent object creation, this is not done.
 * <p/>
 * This class also implements the LockManager interface instead of using some inner class.
 * This is done to reduce object creation overhead at the cost of more complicated code.
 *
 * @author Peter Veentjer.
 */
public class UpdateTransaction implements AlphaTransaction, LockManager {

    private final AtomicLong clock;
    private final AlphaStmStatistics statistics;

    //the attached set contains the Translocals loaded and attached.
    private Map<AlphaAtomicObject, Tranlocal> attached = new IdentityHashMap(2);

    private SnapshotStack snapshotStack;
    private AtomicObjectLockPolicy writeSetLockPolicy;
    private List<Runnable> postCommitTasks;
    private long readVersion;
    private TransactionStatus status;

    public UpdateTransaction(AlphaStmStatistics statistics, AtomicLong clock, AtomicObjectLockPolicy writeSetLockPolicy) {
        this.statistics = statistics;
        this.clock = clock;
        this.writeSetLockPolicy = writeSetLockPolicy;

        init();
    }

    private void init() {
        this.postCommitTasks = null;
        this.readVersion = clock.get();
        this.status = TransactionStatus.active;
        this.snapshotStack = null;
        this.attached.clear();

        if (statistics != null) {
            statistics.incTransactionStartedCount();
        }
    }

    @Override
    public void executePostCommit(Runnable task) {
        switch (status) {
            case active:
                if (task == null) {
                    throw new NullPointerException();
                }
                if (postCommitTasks == null) {
                    postCommitTasks = new LinkedList<Runnable>();
                }
                postCommitTasks.add(task);
                break;
            case committed:
                throw new DeadTransactionException("Can't add afterCommit task on a committed transaction");
            case aborted:
                throw new DeadTransactionException("Can't add afterCommit task on an aborted transaction");
            default:
                throw new RuntimeException();
        }
    }

    public AtomicObjectLockPolicy getAcquireLocksPolicy() {
        return writeSetLockPolicy;
    }

    public void setAcquireLocksPolicy(AtomicObjectLockPolicy writeSetLockPolicy) {
        this.writeSetLockPolicy = writeSetLockPolicy;
    }

    @Override
    public long getReadVersion() {
        return readVersion;
    }

    @Override
    public void reset() {
        switch (status) {
            case active:
                throw new ResetFailureException("Can't reset an active transaction, abort or commit first");
            case committed:
                init();
                break;
            case aborted:
                init();
                break;
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public void retry() {
        switch (status) {
            case active:
                throw RetryError.create();
            case committed:
                throw new DeadTransactionException("Can't retry a committed transaction");
            case aborted:
                throw new DeadTransactionException("Can't retry an aborted transaction");
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public void attachNew(Tranlocal tranlocal) {
        switch (status) {
            case active:
                //System.out.println("attachNew is called");

                if (tranlocal == null) {
                    throw new NullPointerException();
                }

                if (AlphaStmDebugConstants.SANITY_CHECK_ENABLED) {
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

                attached.put((AlphaAtomicObject) tranlocal.getAtomicObject(), tranlocal);

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
    public Tranlocal load(Object object) {
        switch (status) {
            case active:
                if (object == null) {
                    return null;
                }

                AlphaAtomicObject atomicObject = asAtomicObject(object);
                Tranlocal existing = attached.get(atomicObject);
                if (existing != null) {
                    return existing;
                }

                if (statistics == null) {
                    Tranlocal loaded = atomicObject.load(readVersion);
                    if (loaded == null) {
                        throw new LoadUncommittedException();
                    }
                    return loaded;
                } else {
                    try {
                        Tranlocal loaded = atomicObject.load(readVersion);
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
    public Tranlocal privatize(Object object) {
        switch (status) {
            case active:
                if (object == null) {
                    return null;
                }

                AlphaAtomicObject atomicObject = asAtomicObject(object);
                Tranlocal tranlocal = attached.get(atomicObject);
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

    private AlphaAtomicObject asAtomicObject(Object object) {
        if (!(object instanceof AlphaAtomicObject)) {
            String msg = format("Object with class %s does not implement AtomicObject interface",
                    object.getClass().getName());
            throw new IllegalArgumentException(msg);
        }

        return (AlphaAtomicObject) object;
    }

    @Override
    public TransactionStatus getStatus() {
        return status;
    }

    @Override
    public long commit() {
        switch (status) {
            case active:
                try {
                    long commitVersion = doCommit();
                    status = TransactionStatus.committed;
                    if (statistics != null) {
                        statistics.incTransactionCommittedCount();
                    }
                    executeAfterCommitTasks();
                    return commitVersion;
                } finally {
                    if (status != TransactionStatus.committed) {
                        doAbort();
                    }
                }
            case committed:
                //ignore
                throw new TodoException();
            case aborted:
                throw new DeadTransactionException("Can't call commit on an aborted transaction");
            default:
                throw new RuntimeException();
        }
    }

    private void executeAfterCommitTasks() {
        if (postCommitTasks != null) {
            try {
                for (Runnable task : postCommitTasks) {
                    try {
                        task.run();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            } finally {
                postCommitTasks = null;
            }
        }
    }

    private long doCommit() {
        //System.out.println("starting commit");
        Tranlocal[] writeSet = createWriteSet();
        if (writeSet == null) {
            //if there is nothing to commit, we are done.
            if (statistics != null) {
                statistics.incTransactionEmptyCommitCount();
            }
            return readVersion;
        }

        boolean success = false;
        try {
            acquireLocks(writeSet);
            ensureConflictFree(writeSet);
            long writeVersion = clock.incrementAndGet();
            writeChanges(writeSet, writeVersion);
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
     * @return the created WriteSet or null if there is nothing that needs to be written.
     * @throws org.multiverse.api.exceptions.WriteConflictException
     *          if can be determined that another transaction did a conflicting write.
     */
    private Tranlocal[] createWriteSet() {
        if (attached.isEmpty()) {
            return null;
        }

        Tranlocal[] writeSet = null;

        int k = 0;
        for (Tranlocal tranlocal : attached.values()) {
            switch (tranlocal.getDirtinessStatus()) {
                case fresh:
                    //fall through
                case dirty:
                    if (writeSet == null) {
                        writeSet = new Tranlocal[attached.size() - k];
                    }
                    writeSet[k] = tranlocal;
                    k++;
                    break;
                case clean:
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

    private void ensureConflictFree(Tranlocal[] writeSet) {
        for (int k = 0; k < writeSet.length; k++) {
            Tranlocal tranlocal = writeSet[k];
            if (tranlocal == null) {
                return;
            } else {
                AlphaAtomicObject owner = tranlocal.getAtomicObject();
                if (!owner.ensureConflictFree(readVersion)) {
                    if (statistics != null) {
                        statistics.incTransactionWriteConflictCount();
                    }
                    throw WriteConflictException.create();
                }
            }
        }
    }

    private void acquireLocks(Tranlocal[] writeSet) {
        if (!writeSetLockPolicy.tryLocks(writeSet, this)) {
            if (statistics != null) {
                statistics.incTransactionFailedToAcquireLocksCount();
            }

            throw FailedToObtainLocksException.create();
        } else {
            //todo: problem is that if the locks are not acquired successfully, it isn't clear
            //how many locks are acquired..
            if (statistics != null) {
                statistics.incLockAcquiredCount(writeSet.length);
            }
        }
    }


    private void writeChanges(Tranlocal[] writeSet, long writeVersion) {
        if (writeSet == null) {
            return;
        }

        try {
            for (int k = 0; k < writeSet.length; k++) {
                Tranlocal tranlocal = writeSet[k];
                if (tranlocal == null) {
                    return;
                } else {
                    AlphaAtomicObject atomicObject = tranlocal.getAtomicObject();
                    atomicObject.storeAndReleaseLock(tranlocal, writeVersion);
                }
            }

        } finally {
            if (statistics != null) {
                statistics.incWriteCount(attached.size());
            }
        }
    }

    @Override
    public void abort() {
        switch (status) {
            case active:
                doAbort();
                break;
            case committed:
                throw new DeadTransactionException("Can't call abort on a committed transaction");
            case aborted:
                //ignore
                break;
            default:
                throw new RuntimeException();
        }
    }

    private void doAbort() {
        attached.clear();
        status = TransactionStatus.aborted;
        postCommitTasks = null;
        if (statistics != null) {
            statistics.incTransactionAbortedCount();
        }
    }

    @Override
    public void abortAndRetry() {
        switch (status) {
            case active:
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
                break;
            case committed:
                throw new DeadTransactionException("Can't call abortAndRetry on a committed transaction");
            case aborted:
                throw new DeadTransactionException("Can't call abortAndRetry on an aborted transaction");
            default:
                throw new RuntimeException();
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
            }

            //wait for the other transactions to do a write we are interested in.
            listener.awaitUninterruptible();
        } finally {
            if (statistics != null) {
                statistics.decTransactionPendingRetryCount();
            }
        }
    }

    @Override
    public LockManager getLockManager() {
        switch (status) {
            case active:
                return this;
            case aborted:
                throw new DeadTransactionException("Can't get the LockManager, transaction already is aborted.");
            case committed:
                throw new DeadTransactionException("Can't get the LockManager, transaction already is committed.");
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public StmLock getLock(Object atomicObject, LockStatus lockStatus) {
        switch (status) {
            case active:
                throw new TodoException();
            case aborted:
                throw new DeadTransactionException("Can't get pessimistic lock, transaction already is aborted.");
            case committed:
                throw new DeadTransactionException("Can't get pessimistic lock, transaction already is committed.");
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public LockStatus getLockStatus(Object atomicObject) {
        throw new TodoException();
    }

    @Override
    public Tranlocal privatize(Object atomicObject, LockStatus lockMode) {
        throw new TodoException();
    }

    @Override
    public void startOr() {
        switch (status) {
            case active:
                snapshotStack = new SnapshotStack(snapshotStack, createSnapshot());
                break;
            case committed:
                throw new DeadTransactionException("Can't call startOr on a committed transaction");
            case aborted:
                throw new DeadTransactionException("Can't call startOr on an aborted transaction");
            default:
                throw new RuntimeException();
        }
    }

    private TranlocalSnapshot createSnapshot() {
        TranlocalSnapshot result = null;
        for (Tranlocal tranlocal : attached.values()) {
            TranlocalSnapshot snapshot = tranlocal.takeSnapshot();
            snapshot.next = result;
            result = snapshot;
        }

        return result;
    }

    @Override
    public void endOr() {
        switch (status) {
            case active:
                if (snapshotStack == null) {
                    throw new IllegalStateException();
                }
                snapshotStack = snapshotStack.next;
                break;
            case committed:
                throw new DeadTransactionException("Can't call endOr on a committed transaction");
            case aborted:
                throw new DeadTransactionException("Can't call endOr on an aborted transaction");
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public void endOrAndStartElse() {
        switch (status) {
            case active:
                if (snapshotStack == null) {
                    throw new IllegalStateException();
                }
                TranlocalSnapshot snapshot = snapshotStack.snapshot;
                snapshotStack = snapshotStack.next;
                restoreSnapshot(snapshot);
                break;
            case committed:
                throw new DeadTransactionException("Can't call endOrAndStartElse on a committed transaction");
            case aborted:
                throw new DeadTransactionException("Can't call endOrAndStartElse on an aborted transaction");
            default:
                throw new RuntimeException();
        }
    }

    private void restoreSnapshot(TranlocalSnapshot snapshot) {
        attached.clear();

        while (snapshot != null) {
            Tranlocal tranlocal = snapshot.getTranlocal();
            attached.put(tranlocal.getAtomicObject(), tranlocal);
            snapshot.restore();
            snapshot = snapshot.next;
        }
    }

    static final class SnapshotStack {
        public final SnapshotStack next;
        public final TranlocalSnapshot snapshot;

        SnapshotStack(SnapshotStack next, TranlocalSnapshot snapshot) {
            this.next = next;
            this.snapshot = snapshot;
        }
    }
}