package org.codehaus.multiverse.multiversionedstm.growingheap;

import org.codehaus.multiverse.util.latches.Latch;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

public class VersionedLatchGroup {

    private final AtomicLong activeVersionReference;
    //in the future some kind of treemap could be used so to make it easier which versions are interesting.
    private final ConcurrentMap<Long, LatchesByVersion> map = new ConcurrentHashMap<Long, LatchesByVersion>();

    public VersionedLatchGroup(long activeVersion) {
        activeVersionReference = new AtomicLong(activeVersion);
    }

    public long getActiveVersion(){
        return activeVersionReference.longValue();
    }

    public void activateVersion(long newActiveVersion) {
        long oldActiveVersion;
        do {
            oldActiveVersion = activeVersionReference.get();

            //if this is a useless activation (so if the active version already is equal or larger to
            //the newActiveVersion) we are done and can end this method.
            if (oldActiveVersion >= newActiveVersion)
                return;
        } while (!activeVersionReference.compareAndSet(oldActiveVersion, newActiveVersion));

        //at this point we now that we were the one that upgrades the active version from
        //oldActiveVersion to newActiveVersion. We now have the responsibility of waking up all
        //latches that are waiting for this version.

        for (long version = oldActiveVersion; version <= newActiveVersion; version++) {
            LatchesByVersion latchesByVersion = map.remove(version);
            if (latchesByVersion != null)
                latchesByVersion.open();
        }
    }

    /**
     * @param minimalTriggerVersion
     * @param latch
     * @throws NullPointerException if latch is null.
     */
    public void addLatch(long minimalTriggerVersion, Latch latch) {
        if (latch == null) throw new NullPointerException();

        if (latch.isOpen())
            return;

        long activeVersion = activeVersionReference.get();

        if (activeVersion >= minimalTriggerVersion) {
            latch.open();
            return;
        }

        LatchesByVersion latchesByVersion = map.get(minimalTriggerVersion);
        if (latchesByVersion == null) {
            latchesByVersion = new LatchesByVersion();
            LatchesByVersion found = map.putIfAbsent(minimalTriggerVersion, latchesByVersion);
            if (found != null)
                latchesByVersion = found;
        }

        latchesByVersion.add(latch);

        long newestActiveVersion = activeVersionReference.get();
        if (newestActiveVersion > activeVersion) {
            if (activeVersion >= minimalTriggerVersion) {
                latch.open();
                map.remove(minimalTriggerVersion);
            }
        }
    }

    private static class LatchesByVersion {
        Set<Latch> latches = new HashSet<Latch>();
        volatile boolean isOpen = false;

        public void open() {
            if (isOpen)
                return;

            synchronized (this) {
                isOpen = true;
                for (Latch latch : latches)
                    latch.open();

                latches = null;
            }
        }

        public void add(Latch latch) {
            if (latch.isOpen())
                return;

            synchronized (this) {
                if(isOpen)
                    return;

                latches.add(latch);
            }
        }
    }
}
