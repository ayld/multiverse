package org.codehaus.multiverse.multiversionedheap.listenersupport;

import org.codehaus.multiverse.util.latches.Latch;

import java.util.concurrent.atomic.AtomicReference;

public final class NonBlockingVersionedLatchGroup implements VersionedLatchGroup {

    private final AtomicReference<State> stateRef = new AtomicReference<State>();

    public NonBlockingVersionedLatchGroup(long activeVersion) {
        stateRef.set(new State(activeVersion));
    }

    public void activateVersion(long newActiveVersion) {
        LatchNode latchesToOpen;

        boolean success;
        do {
            State currentState = stateRef.get();

            if (newActiveVersion <= currentState.activeVersion)
                return;

            latchesToOpen = currentState.head;
            State newState = new State(newActiveVersion);

            success = stateRef.compareAndSet(currentState, newState);
        } while (!success);

        if (latchesToOpen != null)
            latchesToOpen.openAll();
    }

    public void addLatch(long newActiveVersion, Latch latch) {
        LatchNode latchesToOpen;

        boolean success;
        do {
            latchesToOpen = null;

            State currentState = stateRef.get();

            //if the activeVersion we are waiting for already is activated,
            //we can open the latch and we are done./
            if (currentState.activeVersion > newActiveVersion) {
                latch.open();
                return;
            }

            State newState;
            if (currentState.activeVersion == newActiveVersion) {
                newState = currentState.addLatch(latch);
            } else {
                newState = new State(newActiveVersion, new LatchNode(null, latch));
                latchesToOpen = currentState.head;
            }

            success = stateRef.compareAndSet(currentState, newState);
        } while (!success);

        if (latchesToOpen != null)
            latchesToOpen.openAll();
    }

    final static class State {
        final long activeVersion;
        final LatchNode head;

        State(long activeVersion) {
            this.activeVersion = activeVersion;
            this.head = null;
        }

        State(long activeVersion, Latch latch) {
            this(activeVersion, new LatchNode(latch));
        }

        State(long activeVersion, LatchNode head) {
            this.activeVersion = activeVersion;
            this.head = head;
        }

        State addLatch(Latch latch) {
            return new State(activeVersion, new LatchNode(head, latch));
        }
    }

    final static class LatchNode {
        final LatchNode next;
        final Latch latch;

        LatchNode(Latch latch) {
            this(null, latch);
        }

        LatchNode(LatchNode next, Latch latch) {
            if (latch == null) throw new NullPointerException();
            this.next = next;
            this.latch = latch;
        }

        void openAll() {
            LatchNode node = this;
            do {
                node.latch.open();
                node = node.next;
            } while (node != null);
        }
    }
}
