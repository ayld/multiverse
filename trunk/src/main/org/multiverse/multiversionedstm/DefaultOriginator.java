package org.multiverse.multiversionedstm;

import org.multiverse.api.Originator;
import org.multiverse.api.TransactionId;
import org.multiverse.api.exceptions.NoCommittedDataFoundException;
import org.multiverse.api.exceptions.SnapshotTooOldException;
import org.multiverse.api.exceptions.TooManyRetriesException;
import org.multiverse.api.exceptions.WriteConflictException;
import org.multiverse.util.Bag;
import org.multiverse.util.RetryCounter;
import org.multiverse.util.latches.Latch;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Default {@link Originator} implementation.
 *
 * @author Peter Veentjer.
 * @param <T>
 */
public final class DefaultOriginator<T> implements Originator<T> {

    private final AtomicReference<State> stateRef = new AtomicReference<State>();

    public DefaultOriginator() {
    }

    public State getState() {
        return stateRef.get();
    }

    @Override
    public boolean tryAcquireLockForWriting(TransactionId lockOwner, long expectedVersion, RetryCounter retryCounter) {
        assert lockOwner != null && retryCounter != null;

        do {
            State currentState = stateRef.get();
            State tobeState;
            if (currentState == null) {
                tobeState = new State(lockOwner);
            } else {
                tobeState = currentState.acquireLock(lockOwner);

                if (tobeState != null) {
                    //todo: condition can be simplfied, remove dematerialized != null, -1 shoud indicate no commit
                    if (currentState.dematerialized != null && currentState.dematerializedVersion > expectedVersion) {
                        //a writeconflict is encountered.
                        throw new WriteConflictException();
                    }
                }
            }

            if (tobeState != null) {
                boolean success = stateRef.compareAndSet(currentState, tobeState);
                if (success) {
                    return true;
                }
            }
        } while (retryCounter.decrease());

        return false;
    }

    @Override
    public void writeAndReleaseLock(TransactionId lockOwner, DematerializedObject dematerialized,
                                    long dematerializedVersion, Bag<ListenerNode> listenerNodeBag) {
        assert lockOwner != null;

        boolean success;
        do {
            State currentState = stateRef.get();
            State tobeState = currentState.writeAndReleaseLock(dematerialized, dematerializedVersion);
            success = stateRef.compareAndSet(currentState, tobeState);

            if (success && currentState.listenerHead != null) {
                listenerNodeBag.add(currentState.listenerHead);
            }
        } while (!success);
    }

    @Override
    public void releaseLockForWriting(TransactionId expectedLockOwner) {
        assert expectedLockOwner != null;

        boolean success;
        do {
            State currentState = stateRef.get();
            if (currentState == null)
                return;

            State tobeState = currentState.releaseLockForWriting(expectedLockOwner);
            if (tobeState == currentState)
                return;

            success = stateRef.compareAndSet(currentState, tobeState);
        } while (!success);
    }

    @Override
    public boolean tryAddLatch(Latch listener, long minimalVersion, RetryCounter retryCounter) {
        assert listener != null;
        assert retryCounter != null;

        do {
            State currentState = stateRef.get();

            if (currentState == null || currentState.dematerialized == null) {
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
                return state.dematerialized;
            }
        } while (retryCounter.decrease());

        throw new TooManyRetriesException();
    }

    @Override
    public DematerializedObject tryRead(long maximumVersion, RetryCounter retryCounter) {
        do {
            State state = stateRef.get();

            if (state == null) {
                throw new NoCommittedDataFoundException("No commits have executed, so nothing to get");
            }

            if (!state.isLockedForWriting()) {
                if (state.dematerialized == null) {
                    throw new RuntimeException();//todo: what to do here
                }

                if (state.dematerializedVersion > maximumVersion) {
                    throw new SnapshotTooOldException();
                }

                return state.dematerialized;
            }
        } while (retryCounter.decrease());

        throw new TooManyRetriesException();
    }


    public final static class State {
        final ListenerNode listenerHead;
        final DematerializedObject dematerialized;
        final long dematerializedVersion;
        final TransactionId lockOwner;

        State(TransactionId lockOwner) {
            this.listenerHead = null;
            this.dematerialized = null;
            this.dematerializedVersion = -1;
            this.lockOwner = lockOwner;
        }

        State(ListenerNode listenerHead, DematerializedObject dematerialized, long dematerializedVersion, TransactionId lockOwner) {
            this.listenerHead = listenerHead;
            this.dematerialized = dematerialized;
            this.dematerializedVersion = dematerializedVersion;
            this.lockOwner = lockOwner;
        }

        State addListener(Latch latch) {
            return new State(new ListenerNode(latch, listenerHead), dematerialized, dematerializedVersion, lockOwner);
        }

        State acquireLock(TransactionId lockOwner) {
            if (isLockedForWriting())
                return null;

            return new State(this.listenerHead, this.dematerialized, this.dematerializedVersion, lockOwner);
        }

        State writeAndReleaseLock(DematerializedObject dematerialized, long version) {
            return new State(null, dematerialized, version, null);
        }

        State releaseLockForWriting(TransactionId expectedLockOwner) {
            if (lockOwner == null)
                return this;

            if (expectedLockOwner != lockOwner)
                return this;

            return new State(listenerHead, dematerialized, dematerializedVersion, null);
        }

        public boolean isLockedForWriting() {
            return lockOwner != null;
        }
    }
}
