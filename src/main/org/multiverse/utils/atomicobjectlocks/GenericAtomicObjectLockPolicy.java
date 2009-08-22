package org.multiverse.utils.atomicobjectlocks;

import org.multiverse.api.Transaction;
import org.multiverse.utils.TodoException;
import static org.multiverse.utils.atomicobjectlocks.AtomicObjectLockUtils.nothingToLock;

/**
 * An {@link AtomicObjectLockPolicy} that spins when it can't acquire a lock. When the lock can't
 * be acquired, all locks are released and the locks are tries to be acquired again. The number
 * of spins and retries can be configured. So you can create a version that doesn't spin, but doesn't
 * retry, or a version that does spin but doesn't retry, etc.
 * <p/>
 * Because spinning increases the time a lock is hold, it could prevent other transactions from
 * making progress. So be careful. Setting the retry level too high, could lead to livelocking, but
 * on the other side it could also cause an increase in failure rates of transactions and also
 * cause livelocking on transaction level. So finding good value's is something that needs to be
 * determined.
 * <p/>
 * This GenericAtomicObjectLockPolicy is immutable and threadsafe to use.
 *
 * @author Peter Veentjer
 */
public final class GenericAtomicObjectLockPolicy implements AtomicObjectLockPolicy {

    public static final AtomicObjectLockPolicy FAIL_FAST = new GenericAtomicObjectLockPolicy(0, 0);
    public static final AtomicObjectLockPolicy FAIL_FAST_BUT_RETRY = new GenericAtomicObjectLockPolicy(0, 10);
    public static final AtomicObjectLockPolicy SPIN_AND_RETRY = new GenericAtomicObjectLockPolicy(10, 10);

    private final int spinAttemptsPerLock;
    private final int retryCount;

    public GenericAtomicObjectLockPolicy(int spinAttemptsPerLockCount, int retryCount) {
        if (spinAttemptsPerLockCount < 0 || retryCount < 0) {
            throw new IllegalArgumentException();
        }

        this.spinAttemptsPerLock = spinAttemptsPerLockCount;
        this.retryCount = retryCount;
    }

    @Override
    public boolean tryLock(AtomicObjectLock lock, Transaction lockOwner) {
        if (lockOwner == null) {
            throw new NullPointerException();
        }

        throw new TodoException();
    }

    public int getSpinAttemptsPerLock() {
        return spinAttemptsPerLock;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public boolean tryLockAll(AtomicObjectLock[] locks, Transaction lockOwner) {
        if (lockOwner == null) {
            throw new NullPointerException();
        } else if (nothingToLock(locks)) {
            return true;
        } else {
            int maxAttempts = 1 + retryCount;
            int attempt = 1;

            while (attempt <= maxAttempts) {
                boolean success = attempt(locks, lockOwner);

                if (success) {
                    return true;
                } else {
                    attempt++;
                }
            }

            return false;
        }
    }

    /**
     * A single attempt to acquire all the locks.
     *
     * @param locks     the AtomicObjectLocks to acquire.
     * @param lockOwner the Transaction that wants to own the locks.
     * @return true if it was a success, false otherwise.
     */
    private boolean attempt(AtomicObjectLock[] locks, Transaction lockOwner) {
        int money = spinAttemptsPerLock;

        for (int k = 0; k < locks.length; k++) {
            AtomicObjectLock lock = locks[k];
            if (lock == null) {
                break;
            } else {
                do {
                    if (!lock.tryLock(lockOwner)) {
                        money--;
                    } else {
                        break;
                    }
                } while (money >= 0);

                if (money < 0) {
                    releaseLocks(locks, lockOwner, k - 1);
                    break;
                } else {
                    money += spinAttemptsPerLock;
                }
            }
        }

        return money >= 0;
    }

    private void releaseLocks(AtomicObjectLock[] locks, Transaction owner, int lastIndexOfLock) {
        for (int k = 0; k <= lastIndexOfLock; k++) {
            AtomicObjectLock lock = locks[k];
            lock.releaseLock(owner);
        }
    }
}

