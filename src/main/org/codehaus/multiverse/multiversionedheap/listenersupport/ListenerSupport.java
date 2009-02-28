package org.codehaus.multiverse.multiversionedheap.listenersupport;

import org.codehaus.multiverse.util.iterators.PLongIterator;
import org.codehaus.multiverse.util.latches.Latch;

/**
 * Support for listening to changes for writes on handles. This is needed for the STM version of a condition
 * variable. If an update is done on a specific handle, all Latches that are listening to a change, are
 * openend (and eventually the transaction that placed the latch continues).
 * <p/>
 * All implementations should be threadsafe.
 *
 * @author Peter Veentjer
 */
public interface ListenerSupport {

    /**
     * Adds a listener (the Latch) to a set of handles of a specific version. If handles doesn't contain
     * any element, the Latch is openend.
     *
     * @param activeVersion the minimal version to wake up for.
     * @param handles       the handles to listen to for change.
     * @param latch         the Latch to open when one of the cells has the desired update.
     * @throws NullPointerException if handles or latch is null
     */
    void addListener(long activeVersion, PLongIterator handles, Latch latch);

    /**
     * Wakes up all Listeners that are sleeping on the handles.
     *
     * @param activeVersion the version that has been activated (has been written).
     * @param handles       the handles that have been written
     * @throws NullPointerException if handles is null.
     */
    void wakeupListeners(long activeVersion, PLongIterator handles);
}
