package org.multiverse.stms.alpha.mixins;

import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.LoadTooOldVersionException;
import org.multiverse.api.exceptions.NoProgressPossibleException;
import org.multiverse.stms.alpha.AlphaAtomicObject;
import org.multiverse.stms.alpha.AlphaStmDebugConstants;
import org.multiverse.stms.alpha.AlphaTranlocal;
import org.multiverse.utils.Listeners;
import org.multiverse.utils.TodoException;
import org.multiverse.utils.latches.Latch;

import static java.lang.String.format;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A abstract AtomicObject. It can be used as a mixin (using instrumentation). It contains
 * almost all methods needed for a TL2TransactionalObject to participate.
 * <p/>
 * It is called slow because it has quite a lot of object creation overhead and this limits performance.
 * But it works.. This implementation should not be used for performance improvements, but for having
 * a working implementation.
 * <p/>
 * todo:
 * what happens on the load, if the lock field is not checked?
 * - the clock has increased, but the write has not happened.
 * - between these points, a new transaction could start, using the newest clock. When it does
 * a read, before the write completes, it could see stale data.
 * <p/>
 * This is a lost update.. And that is the reason why the lock should be checked.
 *
 * @author Peter Veentjer
 */
public abstract class SlowAtomicObjectMixin implements AlphaAtomicObject {

    private final AtomicReference<State> stateRef = new AtomicReference<State>();

    /**
     * Could it be that the read doesn't return the most recent maximum version, but an older one?
     * <p/>
     * Could it be that a transaction starts, when the clock has increased but the value has not been written
     * and therefor sees a too old version.
     *
     * @param readVersion
     * @return
     */
    @Override
    public AlphaTranlocal load(long readVersion) {
        State currentState = stateRef.get();

        if (currentState == null || currentState.tranlocal == null) {
            return null;
        }

        if (AlphaStmDebugConstants.SANITY_CHECK_ENABLED) {
            if (!currentState.tranlocal.committed) {
                throw new RuntimeException("Uncommitted state found");
            }
        }

        if (currentState.writeLockOwner != null) {
            //todo: this is not a snapshot to old error, but will do for now.
            throw LoadTooOldVersionException.INSTANCE;
        }

        if (currentState.tranlocal.version > readVersion) {
            throw LoadTooOldVersionException.INSTANCE;
        }

        return currentState.tranlocal;
    }

    @Override
    public boolean ensureConflictFree(long readVersion) {
        State currentState = stateRef.get();
        return currentState.tranlocal == null || currentState.tranlocal.version <= readVersion;
    }

    public Transaction getWriteLockOwner() {
        State currentState = stateRef.get();
        return currentState == null ? null : currentState.writeLockOwner;
    }

    @Override
    public boolean tryLock(Transaction lockOwner) {
        boolean success;
        do {
            State currentState = stateRef.get();
            State tobeState;
            if (currentState == null) {
                tobeState = new State(null, lockOwner, null);
            } else {
                if (AlphaStmDebugConstants.SANITY_CHECK_ENABLED) {
                    if (currentState.tranlocal == null) {
                        throw new IllegalStateException("This should not happen since a lock was already acquired");
                    }

                    if (!currentState.tranlocal.committed) {
                        throw new RuntimeException("Uncommitted state found");
                    }
                }

                if (currentState.writeLockOwner != null) {
                    return false;
                }

                tobeState = new State(currentState.tranlocal, lockOwner, currentState.listeners);
            }

            success = stateRef.compareAndSet(currentState, tobeState);
        } while (!success);

        return true;
    }

    @Override
    public void releaseLock(Transaction expectedLockOwner) {
        boolean success;
        do {
            State currentState = stateRef.get();

            if (currentState == null) {
                throw new TodoException();
            }

            if (currentState.writeLockOwner != expectedLockOwner) {
                return;//we are not going to release write locks of other transactions.
            }

            State tobeState = new State(currentState.tranlocal, null, null);
            success = stateRef.compareAndSet(currentState, tobeState);
        } while (!success);
    }

    @Override
    public void storeAndReleaseLock(AlphaTranlocal tranlocal, long writeVersion) {
        if (AlphaStmDebugConstants.SANITY_CHECK_ENABLED) {
            if (tranlocal.committed) {
                throw new RuntimeException("Can't commit already committed data");
            }
        }

        Listeners listeners;

        tranlocal.committed = true;
        tranlocal.version = writeVersion;

        boolean success;
        do {
            State currentState = stateRef.get();
            State tobeState;

            if (AlphaStmDebugConstants.SANITY_CHECK_ENABLED) {
                if (currentState.tranlocal != null && currentState.tranlocal.version >= writeVersion) {
                    String msg = format("Lost update, commitVersion=%s found version=%s", writeVersion, currentState.tranlocal.version);
                    throw new RuntimeException(msg);
                }

                if (currentState.tranlocal != null) {
                    if (!currentState.tranlocal.committed) {
                        throw new RuntimeException("Uncommitted state found");
                    }
                }
            }

            listeners = currentState.listeners;

            tobeState = new State(tranlocal, null, null);
            success = stateRef.compareAndSet(currentState, tobeState);
        } while (!success);

        if (listeners != null) {
            listeners.openAll();
        }
    }

    @Override
    public boolean registerRetryListener(Latch latch, long minimumVersion) {
        boolean success;

        do {
            State currentState = stateRef.get();
            if (currentState == null || currentState.tranlocal == null) {
                throw new NoProgressPossibleException();
            } else {
                if (currentState.tranlocal.version >= minimumVersion) {
                    latch.open();
                    return true;
                } else {
                    State tobeState = new State(
                            currentState.tranlocal,
                            currentState.writeLockOwner,
                            new Listeners(latch, currentState.listeners));

                    success = stateRef.compareAndSet(currentState, tobeState);
                }
            }

        } while (!success);

        throw new TodoException();
    }

    public static class State {
        public final AlphaTranlocal tranlocal;
        public final Listeners listeners;
        public final Transaction writeLockOwner;

        public State(AlphaTranlocal tranlocal, Transaction writeLockOwner, Listeners listeners) {
            this.tranlocal = tranlocal;
            this.listeners = listeners;
            this.writeLockOwner = writeLockOwner;
        }
    }
}
