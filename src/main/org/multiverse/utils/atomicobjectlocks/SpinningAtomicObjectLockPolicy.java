package org.multiverse.utils.atomicobjectlocks;

import org.multiverse.api.Transaction;
import org.multiverse.utils.TodoException;

/**
 * A {@link AtomicObjectLockPolicy} that uses spinning to acquire the locked. The number of retries is
 * bounded (it is equal to spinAttemptsPerLockCount * number objects to lock). If the locks could
 * not be acquired the method returns false.
 * <p/>
 * Since no blocking is done, locking is deadlock free. But if the spinAttemptsPerLockCount is set very
 * high, it could appear to deadlock (a livelock to be precise).
 *
 * @author Peter Veentjer.
 */
public final class SpinningAtomicObjectLockPolicy implements AtomicObjectLockPolicy {

    //the 100 value needs to be researched if this really is a good performing value
    public final static SpinningAtomicObjectLockPolicy INSTANCE = new SpinningAtomicObjectLockPolicy(100);

    private final int spinAttemptsPerLockCount;

    public SpinningAtomicObjectLockPolicy(int spinAttemptsPerLockCount) {
        if (spinAttemptsPerLockCount < 0) {
            throw new IllegalArgumentException();
        }
        this.spinAttemptsPerLockCount = spinAttemptsPerLockCount;
    }

    public int getSpinAttemptPerLockCount() {
        return spinAttemptsPerLockCount;
    }

    @Override
    public boolean tryLock(AtomicObjectLock lock, Transaction lockOwner) {
        throw new TodoException();
    }

    public boolean tryLocks(AtomicObjectLock[] locks, Transaction lockOwner) {
        if (lockOwner == null) {
            throw new NullPointerException();
        } else if (locks == null || locks.length == 0) {
            return true;
        } else {
            //todo: not completely fair
            int remainingAttempts = spinAttemptsPerLockCount * locks.length;
            for (int k = 0; k < locks.length; k++) {
                AtomicObjectLock item = locks[k];
                if (item == null) {
                    return true;
                } else {
                    boolean locked;
                    do {
                        if (remainingAttempts == 0) {
                            return false;
                        }
                        remainingAttempts--;

                        locked = item.tryLock(lockOwner);
                    } while (!locked);
                }
            }

            return true;
        }
    }
}
