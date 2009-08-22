package org.multiverse.utils.atomicobjectlocks;

import org.multiverse.api.Transaction;
import org.multiverse.utils.TodoException;

/**
 * @author Peter Veentjer
 */
public final class RetrySpinningAtomicObjectLockPolicy implements AtomicObjectLockPolicy {

    private final int spinAttemptsPerLockCount;
    private final int retryCount;

    public RetrySpinningAtomicObjectLockPolicy(int spinAttemptsPerLockCount, int retryCount) {
        if (spinAttemptsPerLockCount < 0 || retryCount < 0) {
            throw new IllegalArgumentException();
        }

        this.spinAttemptsPerLockCount = spinAttemptsPerLockCount;
        this.retryCount = retryCount;
    }

    @Override
    public boolean tryLock(AtomicObjectLock lock, Transaction lockOwner) {
        if (lockOwner == null) {
            throw new NullPointerException();
        }

        throw new TodoException();
    }

    public int getSpinAttemptsPerLockCount() {
        return spinAttemptsPerLockCount;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public boolean tryLocks(AtomicObjectLock[] locks, Transaction owner) {
        if (owner == null) {
            throw new NullPointerException();
        } else if (locks == null || locks.length == 0) {
            return true;
        } else {
            throw new TodoException();
        }
    }
}
