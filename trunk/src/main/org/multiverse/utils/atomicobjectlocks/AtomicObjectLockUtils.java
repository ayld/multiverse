package org.multiverse.utils.atomicobjectlocks;

import org.multiverse.api.Transaction;

/**
 * @author Peter Veentjer.
 */
public final class AtomicObjectLockUtils {

    /**
     * Releases the locks. It is important that all locks are released. If this is not done,
     * objects could remain locked and en get inaccessible.
     * <p/>
     * If locks is null, the method completes.
     * the locks needs to be tried from the begin to the end.
     * if a null element is found, all following elements are ignored.
     *
     * @param locks contains the items to release the locks of.
     * @throws NullPointerException if lockOwner is null.
     */
    public static void releaseLocks(AtomicObjectLock[] locks, Transaction lockOwner) {
        if (lockOwner == null) {
            throw new NullPointerException();
        } else if (locks != null) {
            for (int k = 0; k < locks.length; k++) {
                AtomicObjectLock element = locks[k];
                if (element == null) {
                    return;
                } else {
                    element.releaseLock(lockOwner);
                }
            }
        }
    }

    //we don't want instances
    private AtomicObjectLockUtils() {
    }
}
