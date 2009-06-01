package org.multiverse.util;

import org.multiverse.util.latches.Latch;

/**
 * A immutable single-linked list for storing listener-latches.
 * <p/>
 * Structure is thread-safe and designed to be used in a CAS-loop.
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

    /**
     * Returns the listener Latch stored in this ListenerNode.
     *
     * @return the listener Latch.
     */
    public Latch getListener() {
        return listener;
    }

    /**
     * Gets the next ListenerNode or null if this ListerNode is the end of the line.
     *
     * @return the next ListenerNode
     */
    public ListenerNode getNext() {
        return next;
    }

    /**
     * Opens all listeners. Method is not recursive but iterative.
     */
    public void openAll() {
        ListenerNode listenerNode = this;
        do {
            listenerNode.listener.open();
            listenerNode = listenerNode.next;
        } while (listenerNode != null);
    }
}