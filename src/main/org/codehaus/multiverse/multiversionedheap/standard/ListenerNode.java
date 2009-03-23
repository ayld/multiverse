package org.codehaus.multiverse.multiversionedheap.standard;

import org.codehaus.multiverse.utils.latches.Latch;

/**
 * @author Peter Veentjer.
 */
public class ListenerNode {
    private final Latch latch;
    private final ListenerNode previous;

    public ListenerNode(Latch listener, ListenerNode previous) {
        assert listener != null;
        this.latch = listener;
        this.previous = previous;
    }

    public ListenerNode getPrevious() {
        return previous;
    }

    public Latch getListener() {
        return latch;
    }
}
