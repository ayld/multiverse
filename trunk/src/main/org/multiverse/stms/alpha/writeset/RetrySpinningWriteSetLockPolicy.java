package org.multiverse.stms.alpha.writeset;

import org.multiverse.api.Transaction;
import org.multiverse.stms.alpha.AlphaAtomicObject;

/**
 * @author Peter Veentjer
 */
public final class RetrySpinningWriteSetLockPolicy implements WriteSetLockPolicy {

    private final int spinAttemptsPerLockCount;
    private final int retryCount;

    public RetrySpinningWriteSetLockPolicy(int spinAttemptsPerLockCount, int retryCount) {
        if (spinAttemptsPerLockCount < 0 || retryCount < 0) {
            throw new IllegalArgumentException();
        }

        this.spinAttemptsPerLockCount = spinAttemptsPerLockCount;
        this.retryCount = retryCount;
    }

    public int getSpinAttemptsPerLockCount() {
        return spinAttemptsPerLockCount;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public boolean acquireLocks(WriteSet writeSet, Transaction owner) {
        if (writeSet == null) {
            return true;
        }

        WriteSet node = writeSet;

        int remainingAttempts = writeSet.size * spinAttemptsPerLockCount;

        while (node != null) {
            AlphaAtomicObject atomicObject = node.tranlocal.getAtomicObject();
            boolean locked;
            do {
                if (remainingAttempts == 0) {
                    return false;
                }
                remainingAttempts--;

                locked = atomicObject.acquireLock(owner);
            } while (!locked);


            if (!atomicObject.acquireLock(owner)) {
                return false;
            }
            node = node.next;
        }

        return true;
    }
}
