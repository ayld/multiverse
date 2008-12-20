package org.codehaus.multiverse.multiversionedstm.growingheap;

import org.codehaus.multiverse.util.latches.Latch;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A VersionedLatchGroup is a group of Latches listening on a specific handle, but perhaps waiting for different versions.
 * One transaction started listening from version 10 for example, and another one could start listening from version 20.
 * <p/>
 * todo:
 * Could it ever happen that a thread starts to listen to a version that hasn't occurred yet?
 *
 * @author Peter Veentjer.
 */
public final class VersionedLatchGroup {

    private final AtomicLong activeVersionReference;
    //in the future some kind of treemap could be used so to make it easier which versions are interesting.
    private final ConcurrentMap<Long, LatchGroupByVersion> map = new ConcurrentHashMap<Long, LatchGroupByVersion>();

    public VersionedLatchGroup(long activeVersion) {
        activeVersionReference = new AtomicLong(activeVersion);
    }

    public long getActiveVersion() {
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

        //at this point we know that we were the one that upgrades the active version from
        //oldActiveVersion to newActiveVersion. We now have the responsibility of waking up all
        //latches that are waiting for this version.

        for (long version = oldActiveVersion; version <= newActiveVersion; version++) {
            LatchGroupByVersion latchGroupByVersion = map.remove(version);
            if (latchGroupByVersion != null)
                latchGroupByVersion.open();
        }
    }

    /**
     * @param minimalTriggerVersion
     * @param latch
     * @throws NullPointerException if latch is null.
     */
    public void addLatch(long minimalTriggerVersion, Latch latch) {
        if (latch == null) throw new NullPointerException();

        //if the latch already is opened, there is no need to register it
        if (latch.isOpen())
            return;

        long activeVersion = activeVersionReference.get();

        //if the version the latch is waiting for already has been activated, the latch can
        //be opened, and we can return. There is no need to register it.
        if (activeVersion >= minimalTriggerVersion) {
            latch.open();
            return;
        }

        //lets get the latchgroup
        LatchGroupByVersion latchGroupByVersion = map.get(minimalTriggerVersion);
        if (latchGroupByVersion == null) {
            latchGroupByVersion = new LatchGroupByVersion();
            LatchGroupByVersion found = map.putIfAbsent(minimalTriggerVersion, latchGroupByVersion);
            if (found != null)
                latchGroupByVersion = found;
        }


        latchGroupByVersion.add(latch);

        //it could be that a commit on that specific version happended before we added it to the latchgroup.
        //the consequence could be that a listener is not woken up. To make sure that doesn't happen, we
        //have to chech
        long newestActiveVersion;
        do {
            newestActiveVersion = activeVersionReference.get();

            if (newestActiveVersion >= minimalTriggerVersion) {
                //if there was an interesting update, we can open the latch and return. It could be that the
                //latch is opened by the writing thread and the addLatch thread, but that doesn't matter since
                //multiple Latch.opens are ignored.
                latch.open();
                return;
            }

            //as long as other transaction have updates activeVersion, we need to retry so that we don't forget
            //to release a listener.
        } while (activeVersionReference.get() != newestActiveVersion);

        //it is now the responsibility of the writer to open the latch.
    }

    /**
     * Contians all latches that are waiting for a specific version.
     */
    private static class LatchGroupByVersion {
        Set<Latch> latches = new HashSet<Latch>();
        volatile boolean isOpen = false;

        public void open() {
            if (isOpen)
                return;

            Set<Latch> localizedLatches;
            synchronized (this) {
                isOpen = true;
                localizedLatches = latches;
                latches = null;
            }

            for (Latch latch : localizedLatches)
                latch.open();
        }

        public void add(Latch latch) {
            if (latch.isOpen())
                return;

            if (isOpen) {
                latch.open();
                return;
            }

            synchronized (this) {
                if (isOpen) {
                    latch.open();
                    return;
                }

                latches.add(latch);
            }
        }
    }
}
