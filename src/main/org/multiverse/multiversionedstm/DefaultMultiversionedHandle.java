package org.multiverse.multiversionedstm;

import org.multiverse.api.TransactionId;
import org.multiverse.api.exceptions.NoCommittedDataFoundException;
import org.multiverse.api.exceptions.SnapshotTooOldException;
import org.multiverse.api.exceptions.StarvationException;
import org.multiverse.api.exceptions.WriteConflictException;
import org.multiverse.util.ListenerNode;
import org.multiverse.util.RetryCounter;
import org.multiverse.util.latches.Latch;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Default {@link MultiversionedHandle} implementation.
 * <p/>
 * This object is responsible for storing the content of objects that participate in stm space.
 * So if you have a Person object, you also have a Person handle. This handle can be asked
 * for the committed versions of that Person (so the handle contains the history of changes
 * made to a specific person). This history at the moment is limited to only the last committed
 * version, but in the future a longer history will be added. The big question here is how to
 * prevent old histories from filling up space.
 *
 * @author Peter Veentjer.
 * @param <T>
 */
public final class DefaultMultiversionedHandle<T> implements MultiversionedHandle<T> {

    private final AtomicReference<State> stateRef = new AtomicReference<State>();

    public DefaultMultiversionedHandle() {
    }

    public State getState() {
        return stateRef.get();
    }

    @Override
    public void tryToAcquireLockForWritingAndDetectForConflicts(TransactionId lockOwner, long expectedVersion, RetryCounter retryCounter) {
        assert lockOwner != null && retryCounter != null;

        do {
            State currentState = stateRef.get();
            State tobeState;
            if (currentState == null) {
                tobeState = new State(lockOwner);
            } else {
                if (currentState.dematerializedVersion > expectedVersion) {
                    throw WriteConflictException.INSTANCE;
                }

                tobeState = currentState.acquireLock(lockOwner);
            }

            if (tobeState != null) {
                boolean success = stateRef.compareAndSet(currentState, tobeState);
                if (success) {
                    return;
                }
            }
        } while (retryCounter.decrease());

        throw StarvationException.INSTANCE;
    }

    @Override
    public ListenerNode writeAndReleaseLock(TransactionId lockOwner, DematerializedObject dematerialized,
                                            long dematerializedVersion) {
        assert lockOwner != null;

        State currentState;
        boolean success;
        do {
            currentState = stateRef.get();
            State tobeState = currentState.writeAndReleaseLock(dematerialized, dematerializedVersion);
            success = stateRef.compareAndSet(currentState, tobeState);
        } while (!success);

        return currentState.listenerHead;
    }

    @Override
    public void releaseLockForWriting(TransactionId expectedLockOwner) {
        assert expectedLockOwner != null;

        boolean success;
        do {
            State currentState = stateRef.get();
            if (currentState == null) {
                return;
            }

            State tobeState = currentState.releaseLockForWriting(expectedLockOwner);
            if (tobeState == currentState) {
                return;
            }

            success = stateRef.compareAndSet(currentState, tobeState);
        } while (!success);
    }

    @Override
    public boolean tryAddLatch(Latch listener, long minimalVersion, RetryCounter retryCounter) {
        assert listener != null;
        assert retryCounter != null;

        do {
            State currentState = stateRef.get();

            if (currentState == null || currentState.dematerializedObject == null) {
                //throw new IllegalStateException("Can't add a Latch on uncommitted state");
                return true;//ignore
            }

            if (currentState.dematerializedVersion >= minimalVersion) {
                listener.open();
                return true;
            } else {
                if (!currentState.isLockedForWriting()) {
                    State tobeState = currentState.addListener(listener);
                    if (stateRef.compareAndSet(currentState, tobeState)) {
                        return true;
                    }
                }
            }

            //so as long as the tobeState could not be set, or if we need to wait for a pending write,
            //repeat the casloop.
        } while (retryCounter.decrease());

        return false;
    }

    @Override
    public DematerializedObject tryGetLastCommitted(RetryCounter retryCounter) {
        do {
            State state = stateRef.get();

            if (state == null) {
                return null;
            }

            if (!state.isLockedForWriting()) {
                return state.dematerializedObject;
            }
        } while (retryCounter.decrease());

        throw StarvationException.INSTANCE;
    }

    @Override
    public DematerializedObject tryRead(long maximumVersion, RetryCounter retryCounter) {
        do {
            State state = stateRef.get();

            if (state == null) {
                throw new NoCommittedDataFoundException("No commits have executed, so nothing to get");
            }

            if (!state.isLockedForWriting()) {
                if (state.dematerializedObject == null) {
                    throw new RuntimeException();//todo: what to do here
                }

                if (state.dematerializedVersion > maximumVersion) {
                    throw new SnapshotTooOldException();
                }

                return state.dematerializedObject;
            }
        } while (retryCounter.decrease());

        throw StarvationException.INSTANCE;
    }


    public final static class State {
        final ListenerNode listenerHead;
        final DematerializedObject dematerializedObject;
        final long dematerializedVersion;
        final TransactionId lockOwner;

        State(TransactionId lockOwner) {
            this.listenerHead = null;
            this.dematerializedObject = null;
            this.dematerializedVersion = -1;
            this.lockOwner = lockOwner;
        }

        State(ListenerNode listenerHead, DematerializedObject dematerializedObject, long dematerializedVersion,
              TransactionId lockOwner) {
            this.listenerHead = listenerHead;
            this.dematerializedObject = dematerializedObject;
            this.dematerializedVersion = dematerializedVersion;
            this.lockOwner = lockOwner;
        }

        State addListener(Latch latch) {
            return new State(
                    new ListenerNode(latch, listenerHead),
                    dematerializedObject,
                    dematerializedVersion,
                    lockOwner);
        }

        State acquireLock(TransactionId lockOwner) {
            if (isLockedForWriting())
                return null;

            return new State(
                    this.listenerHead,
                    this.dematerializedObject,
                    this.dematerializedVersion,
                    lockOwner);
        }

        State writeAndReleaseLock(DematerializedObject dematerialized, long version) {
            return new State(null, dematerialized, version, null);
        }

        State releaseLockForWriting(TransactionId expectedLockOwner) {
            if (lockOwner == null) {
                return this;
            }

            if (expectedLockOwner != lockOwner) {
                return this;
            }

            return new State(listenerHead, dematerializedObject, dematerializedVersion, null);
        }

        public boolean isLockedForWriting() {
            return lockOwner != null;
        }
    }
}
