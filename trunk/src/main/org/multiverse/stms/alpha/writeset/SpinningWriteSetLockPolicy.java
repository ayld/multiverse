package org.multiverse.stms.alpha.writeset;

import org.multiverse.api.Transaction;
import org.multiverse.stms.alpha.AlphaAtomicObject;

/**
 * A {@link WriteSetLockPolicy} that uses spinning to acquire the locked. The number of retries is
 * bounded (it is equal to spinAttemptsPerLockCount * number objects to lock). If the locks could
 * not be acquired the method returns false.
 * <p/>
 * Since no blocking is done, locking is deadlock free. But if the spinAttemptsPerLockCount is set very
 * high, it could appear to deadlock (a livelock to be precise).
 *
 * @author Peter Veentjer.
 */
public final class SpinningWriteSetLockPolicy implements WriteSetLockPolicy {

    private final int spinAttemptsPerLockCount;

    public SpinningWriteSetLockPolicy(int spinAttemptsPerLockCount) {
        if (spinAttemptsPerLockCount < 0) {
            throw new IllegalArgumentException();
        }
        this.spinAttemptsPerLockCount = spinAttemptsPerLockCount;
    }

    public int getSpinAttemptPerLockCount() {
        return spinAttemptsPerLockCount;
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

            node = node.next;
        }

        return true;
    }
}
