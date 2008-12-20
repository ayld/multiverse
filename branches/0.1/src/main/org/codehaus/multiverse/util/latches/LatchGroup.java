package org.codehaus.multiverse.util.latches;

import static java.lang.String.format;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A LatchGroup is a group of latches. It makes it possible to open to multiple latches simultaniously.
 * <p/>
 * todo:
 * this structure could be made completely non blocking.
 *
 * @author Peter Veentjer.
 */
public final class LatchGroup {

    private final Object latchesLock = new Object();
    private Set<Latch> latches;
    private volatile boolean isOpen = false;

    /**
     * Opens this LatchGroup. All Latches that are added to this LatchGroup are opened as well.
     * If this LatchGroup already is open, the call is gnored.
     */
    public void open() {
        if (isOpen)
            return;

        isOpen = true;

        synchronized (latchesLock) {
            if (latches != null) {
                //lets open all latches
                for (Latch latch : latches)
                    latch.open();

                //since all latches are opened, we are not interested in them anymore.
                latches = null;
            }
        }
    }

    /**
     * Checks if this LatchGroup is open.
     *
     * @return true if this LatchGroup is open, false otherwise.
     */
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * Returns a set containing all latches that are registered to this LatchGroup.
     *
     * @return a containing all latches that are registered to this LatchGroup.
     */
    public Set<Set> getLatches() {
        synchronized (latchesLock) {
            return latches == null ? Collections.EMPTY_SET : new HashSet<Latch>(latches);
        }
    }

    /**
     * Adds a Latch to this LatchGroup. If this Latch already is open, the call is ignored. If tihs LatchGroup is open
     * the Latch is opened and not registered. If this Latch already is added, the call is ignored.
     *
     * @param latch the Latch to add.
     * @throws NullPointerException if latch is null.
     */
    public void add(Latch latch) {
        if (latch == null)
            throw new NullPointerException();

        //latches that already are open, don't need to be registered to this LatchGroup. Would only increase
        //memory usage, without any additional benefit.
        if (latch.isOpen())
            return;

        synchronized (latchesLock) {
            if (isOpen) {
                latch.open();
                return;
            }

            //lets remove the already opened latches.. no need to keep references to them.
            if (latches != null) {
                for (Iterator<Latch> it = latches.iterator(); it.hasNext();) {
                    if (it.next().isOpen())
                        it.remove();
                }
            }

            if (latches == null)
                latches = new HashSet();

            latches.add(latch);
        }
    }

    @Override
    public String toString() {
        return format("StandardLatch(open=%s)", isOpen);
    }
}
