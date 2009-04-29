package org.multiverse.multiversionedstm;

import org.multiverse.util.latches.Latch;

public class ListenerNode {
    public final Latch latch;
    public final ListenerNode next;

    public ListenerNode(Latch listener, ListenerNode next) {
        assert listener != null;
        this.latch = listener;
        this.next = next;
    }

    public Latch getLatch() {
        return latch;
    }

    public ListenerNode getNext() {
        return next;
    }
}