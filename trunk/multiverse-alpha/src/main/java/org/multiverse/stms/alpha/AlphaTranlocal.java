package org.multiverse.stms.alpha;

import org.multiverse.api.Transaction;
import org.multiverse.utils.commitlock.CommitLock;
import org.multiverse.utils.commitlock.CommitLockResult;

/**
 * The Tranlocal is the Transaction local content of a AtomicObject, since the state from the
 * AtomicObject is removed. So for every AtomicObject there are 1 or more Tranlocals
 * (or zero when the AtomicObject is being constructed).
 * <p/>
 * Semantics of version:
 * after the Tranlocal is committed, the version contains the write version. Before the commit it contains
 * the current read version.
 * <p/>
 * To support nested transactions there are partial rollbacks, each Tranlocal is able to make a snapshot of
 * itself, so that it can be restored when a (nested) transaction rolls back.
 * <p/>
 * Once the Tranlocal has been committed, the fields should only be read and not written. Since the
 * publication (commit) introduces some happens before relation, the fields in this object will also lift
 * on that happens before relation.
 *
 * @author Peter Veentjer.
 */
public abstract class AlphaTranlocal implements CommitLock {

    public long version = Long.MIN_VALUE;

    /**
     * True if this Tranlocal is committed and therefor is completely immutable.
     * This field should only be set by the STM. It can be used to make the object
     * immutable after it has been committed.
     */
    public boolean committed = false;

    /**
     * Is called just before this tranlocal commits. It allows the Tranlocal to do needed cleanup.
     * <p/>
     * An implementation needs to do at least 2 things:
     * <ol>
     * <li>change committed to true</li>
     * <li>set the version to the writeVersion</li>
     * </ol>
     * <p/>
     * Detection if the writeVersion makes sense is not mandatory for the implementation.
     *
     * @param writeVersion the version of the commit. This is the version this tranlocal
     *                     from now on will be known. It is never going to change anymore.
     */
    public abstract void prepareForCommit(long writeVersion);

    /**
     * Returns the AlphaAtomicObject that belongs to this AlphaTranlocal.
     *
     * @return the AlphaAtomicObject that belongs to this AlphaTranlocal
     */
    public abstract AlphaAtomicObject getAtomicObject();

    /**
     * Creates the TranlocalSnapshot of the Tranlocal. A snapshot should only be made if
     * this Tranlocal is not committed.
     *
     * @return the snapshot.
     */
    public abstract AlphaTranlocalSnapshot takeSnapshot();

    /**
     * Returns the DirtinessStatus for this Tranlocal. Based on this value the stm is able to decide
     * what to do with an Tranlocal.
     * <p/>
     * Value could be stale as soon as it is returned. It also depends on the implementation if
     * the DirtinessState.writeconflict is returned. If it isn't returned here, the transaction
     * will abort eventually.
     *
     * @return The NeedToCommitState
     * @throws org.multiverse.api.exceptions.LoadException
     *          if failed to load the data needed to perform the check on dirtiness state.
     *          Depends on the implementation if this is thrown.
     */
    public abstract DirtinessStatus getDirtinessStatus();

    @Override
    public CommitLockResult tryLockAndDetectConflicts(Transaction lockOwner) {
        AlphaAtomicObject atomicObject = getAtomicObject();

        boolean lockedAcquired = atomicObject.tryLock(lockOwner);
        if (!lockedAcquired) {
            return CommitLockResult.failure;
        }

        AlphaTranlocal mostRecentlyWritten = atomicObject.load();
        if (mostRecentlyWritten == null) {
            return CommitLockResult.success;
        }

        boolean noConflict = mostRecentlyWritten.version <= lockOwner.getReadVersion();
        if (noConflict) {
            return CommitLockResult.success;
        }

        atomicObject.releaseLock(lockOwner);
        return CommitLockResult.conflict;
    }

    @Override
    public void releaseLock(Transaction expectedLockOwner) {
        getAtomicObject().releaseLock(expectedLockOwner);
    }
}
