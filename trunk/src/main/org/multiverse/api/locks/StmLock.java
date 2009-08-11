package org.multiverse.api.locks;

import org.multiverse.api.Transaction;

/**
 *
 * Represents a exclusive lock.
 *
 * Different objects could represent the same Lock. If the transaction needs to manage this all
 * it is going to add overhead. The transaction has to make sure that the locks will be released
 * when the 
 *
 * @author Peter Veentjer
 */
public interface StmLock {

    /**
     * Returns the status of the lock. The returned value could be stale as soon
     * as it is returned.
     *
     * @return the status of lock.
     */
    LockStatus getLockStatus();

    /**
     * Aquires this lock exclusive
     */
    void acquireExclusive();

    /**
     * Checks if the lock is hold by calling transaction.
     *
     * @return
     */
    boolean isLockedByMe();

    /**
     * Returns the Transaction that currently owns the lock. If null is returned
     * the lock is free. But this value could be stale as soon as it is returned.
     *
     * @return the Transaction that currently owns the lock.
     */
    Transaction getOwner();
}
