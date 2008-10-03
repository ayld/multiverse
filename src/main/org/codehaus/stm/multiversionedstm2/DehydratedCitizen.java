package org.codehaus.stm.multiversionedstm2;

import org.codehaus.stm.util.Latch;

import java.util.concurrent.atomic.AtomicReference;

public abstract class DehydratedCitizen {

    private final AtomicReference<LatchNode> headReference = new AtomicReference<LatchNode>();

    public abstract Citizen hydrate(MultiversionedStm.MultiversionedTransaction transaction);

    public abstract long getVersion();

    public final void addListener(Latch latch) {
        assert latch != null;

        LatchNode oldHead;
        LatchNode newHead;
        do {
            oldHead = headReference.get();
            newHead = new LatchNode(oldHead, latch);
        } while (headReference.compareAndSet(oldHead, newHead));
    }

    public void notifyListeners() {
        LatchNode head = headReference.getAndSet(null);
        while (head != null) {
            head.latch.open();
            head = head.parent;
        }
    }

    static class LatchNode {
        final LatchNode parent;
        final Latch latch;

        LatchNode(LatchNode parent, Latch latch) {
            this.parent = parent;
            this.latch = latch;
        }
    }
}
