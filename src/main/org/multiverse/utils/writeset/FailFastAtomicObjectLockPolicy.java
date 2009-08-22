package org.multiverse.utils.writeset;

import org.multiverse.api.Transaction;

/**
 * A {@link AtomicObjectLockPolicy} that fails immediately when the locks can't be acquired. No spinning
 * or retrying whatsoever.
 *
 * @author Peter Veentjer
 */
public final class FailFastAtomicObjectLockPolicy implements AtomicObjectLockPolicy {

    public final static FailFastAtomicObjectLockPolicy INSTANCE = new FailFastAtomicObjectLockPolicy();

    @Override
    public boolean tryLock(AtomicObjectLock lock, Transaction lockOwner) {
        return lock.tryLock(lockOwner);
    }

    @Override
    public boolean tryLocks(AtomicObjectLock[] locks, Transaction lockOwner) {
        if (locks == null) {
            return true;
        }

        for (int k = 0; k < locks.length; k++) {
            AtomicObjectLock lock = locks[k];
            if (lock == null) {
                return true;
            } else if (!tryLock(lock, lockOwner)) {
                return false;
            }
        }

        return true;
    }
}
