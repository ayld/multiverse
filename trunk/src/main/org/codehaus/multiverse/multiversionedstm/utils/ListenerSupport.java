package org.codehaus.multiverse.multiversionedstm.utils;

import org.codehaus.multiverse.util.latches.Latch;

/**
 * Support for listening to changes for writes on handles. This is needed for the STM version of a condition
 * variable. If an update is done on a specific handle, all Latches that are listening to a change, are
 * openend (and eventually the transaction that placed the latch continues).
 * <p/>
 * All implementations are threadsafe.
 *
 * @author Peter Veentjer
 */
public interface ListenerSupport {

    /**
     * Adds a Listener.
     * <p/>
     * todo:
     * what to do if handles is empty? Sleeping on an empty list could lead to a 'deadlock'. Waiting for an event
     * that doesn't happen.
     *
     * @param version
     * @param handles
     * @param latch   the Latch to open when one of the cells has the desired update.
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
