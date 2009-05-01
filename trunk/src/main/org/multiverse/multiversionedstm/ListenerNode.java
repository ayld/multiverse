package org.multiverse.multiversionedstm;

import org.multiverse.util.latches.Latch;

/**
 * A immutable list for storing listener-latches.
 * <p/>
 * Structure is threadsafe and designed to be used in a CAS-loop.
 *
 * @author Peter Veentjer.
 */
public final class ListenerNode {
    public final Latch listener;
    public final ListenerNode next;

    public ListenerNode(Latch listener, ListenerNode next) {
        assert listener != null;
        this.listener = listener;
        this.next = next;
    }

    public Latch getListener() {
        return listener;
    }

    public ListenerNode getNext() {
        return next;
    }

    /**
     * Opens all listeners.
     */
    public void openAll() {
        ListenerNode listenerNode = this;
        do {
            listenerNode.listener.open();
            listenerNode = listenerNode.next;
        } while (listenerNode != null);
    }
}