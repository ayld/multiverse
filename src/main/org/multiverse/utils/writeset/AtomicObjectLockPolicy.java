package org.multiverse.utils.writeset;

import org.multiverse.api.Transaction;

/**
 * When a Transaction commits, it needs to acquire locks on the objects of the writeset.
 * With this LockPolicy this behavior can be influenced.
 * <p/>
 * If the locks could not be acquired, the LockPolicy is not required to release the locks.
 * <p/>
 * The reason that the WriteSetLockPolicy works with an array, is that it doesn't create
 * a lot of litter
 *
 * @author Peter Veentjer.
 */
public interface AtomicObjectLockPolicy {

    /**
     * Tries to acquire the lock. This method should behave just the same as all the
     * other tryLock methods. The reason of its existence is that it prevents creating
     * an array if you only need to lock a single element.
     *
     * @param lock
     * @param lockOwner
     * @return true if the lock as acquired successfully, false otherwise.
     */
    boolean tryLock(AtomicObjectLock lock, Transaction lockOwner);

    /**
     * Tries to acquire all the locks.
     * <p/>
     * Acquires the locks for a writeset. The semantics of the array:
     * - if the writeset is null or length 0, the result = true.
     * - as soon as the writeset contains a null, there are no other items and
     * - true can be returned.
     * - for all non null elements the lock needs to be tried to acquire.
     *
     * @param locks     the WriteSet to lock.
     * @param lockOwner the Transaction that wants to own the lock.
     * @return true if the locks are acquired successfully, false otherwise.
     */
    boolean tryLocks(AtomicObjectLock[] locks, Transaction lockOwner);
}
