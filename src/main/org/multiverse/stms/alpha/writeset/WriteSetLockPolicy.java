package org.multiverse.stms.alpha.writeset;

import org.multiverse.api.Transaction;

/**
 * When a Transaction commits, it needs to acquire locks on the objects of the writeset.
 * With this LockPolicy this behavior can be influenced.
 * <p/>
 * If the locks could not be acquired, the LockPolicy is not required to release the locks.
 *
 * @author Peter Veentjer.
 */
public interface WriteSetLockPolicy {

    /**
     * Acquires the locks for a writeset.
     *
     * @param writeSet the WriteSet to lock.
     * @param owner    the Transaction that wants to acquire the locks.
     * @return true if the locks are acquired successfully, false otherwise.
     */
    boolean acquireLocks(WriteSet writeSet, Transaction owner);
}
