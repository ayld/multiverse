package org.codehaus.multiverse.multiversionedstm.growingheap;

import org.codehaus.multiverse.util.latches.Latch;

/**
 * Support for listening to changes for writes on cells.
 *
 * All implementations are threadsafe.
 *
 * @author Peter Veentjer
 */
public interface ListenerSupport {

    /**
     * Adds a Listener.
     *
     * todo:
     * what to do if handles is empty? Sleeping on an empty list could lead to a 'deadlock'. Waiting for an event
     * that doesn't happen.
     *
     * @param version
     * @param handles
     * @param latch  the Latch to open when one of the cells has the desired update.
     * @throws NullPointerException if handles or latch is null
     */
    void addListener(long version, long[] handles, Latch latch);

    /**
     * Wakes up all Listeners that are sleeping on the handles.
     *
     * @param version
     * @param handles
     * @throws NullPointerException if handles is null.
     */
    void wakeupListeners(long version, long[] handles);
}
