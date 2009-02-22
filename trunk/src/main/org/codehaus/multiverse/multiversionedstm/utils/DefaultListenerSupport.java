package org.codehaus.multiverse.multiversionedstm.utils;

import org.codehaus.multiverse.multiversionedstm.growingheap.VersionedLatchGroup;
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

    public void addListener(long minimalTriggerVersion, long handles[], Latch latch) {
        if (handles == null || latch == null) throw new NullPointerException();

        for (long handle : handles) {
            VersionedLatchGroup latchGroup = getOrCreateLatchGroup(minimalTriggerVersion, handle);
            latchGroup.addLatch(minimalTriggerVersion, latch);
        }
    }

    private VersionedLatchGroup getOrCreateLatchGroup(long version, long handle) {
        VersionedLatchGroup latchGroup = latchGroups.get(handle);
        if (latchGroup == null) {
            VersionedLatchGroup newLatchGroup = new VersionedLatchGroup(version - 1);
            latchGroup = latchGroups.putIfAbsent(handle, newLatchGroup);
            if (latchGroup == null)
                latchGroup = newLatchGroup;
        }
        return latchGroup;
    }

    public void wakeupListeners(long version, long[] handles) {
        if (handles == null) throw new NullPointerException();

        if (latchGroups.isEmpty())
            return;

        for (long handle : handles) {
            VersionedLatchGroup latchGroup = latchGroups.get(handle);
            if (latchGroup != null)
                latchGroup.activateVersion(version);
        }
    }
}
