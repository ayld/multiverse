package org.codehaus.multiverse.multiversionedheap.listenersupport;

import org.codehaus.multiverse.util.iterators.PLongIterator;
import org.codehaus.multiverse.util.latches.Latch;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Default {@link ListenerSupport} implementation. It contains a Map with VersionedLatchGroup objects as value
 * and the handle as key. This contains the information about all listeners for a specific address.
 *
 * @author Peter Veentjer.
 */
public final class DefaultListenerSupport implements ListenerSupport {

    //key is handle
    private final ConcurrentMap<Long, VersionedLatchGroup> latchGroups = new ConcurrentHashMap<Long, VersionedLatchGroup>();

    public void addListener(long activeVersion, PLongIterator handles, Latch latch) {
        if (handles == null || latch == null) throw new NullPointerException();

        if (!handles.hasNext()) {
            latch.open();
            return;
        }

        for (; handles.hasNext();) {
            long handle = handles.next();
            VersionedLatchGroup latchGroup = getOrCreateLatchGroup(activeVersion, handle);
            latchGroup.addLatch(activeVersion, latch);
        }
    }

    private VersionedLatchGroup getOrCreateLatchGroup(long version, long handle) {
        VersionedLatchGroup latchGroup = latchGroups.get(handle);
        if (latchGroup == null) {
            VersionedLatchGroup newLatchGroup = new NonBlockingVersionedLatchGroup(version);
            latchGroup = latchGroups.putIfAbsent(handle, newLatchGroup);
            if (latchGroup == null)
                latchGroup = newLatchGroup;
        }
        return latchGroup;
    }

    public void wakeupListeners(long version, PLongIterator handles) {
        if (handles == null) throw new NullPointerException();

        if (latchGroups.isEmpty())
            return;

        for (; handles.hasNext();) {
            long handle = handles.next();
            VersionedLatchGroup latchGroup = latchGroups.get(handle);
            if (latchGroup != null)
                latchGroup.activateVersion(version);
        }
    }
}
