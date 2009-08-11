package org.multiverse.api.locks;

import org.multiverse.stms.alpha.Tranlocal;

/**
 *
 * To prevent complicating the Transaction interface, all the lock related functionality
 * can be found here.
 *
 * @author Peter Veentjer
 */
public interface LockManager {


    /**
     * Privatizes and locks an atomic object immediately. The lock can be retrieved
     * later using the todo
     *
     * @param atomicObject the AtomicObject to privatize and lock.
     * @param lockStatus how to lock the atomicObject.
     * @return the privatized and locked Tranlocal.
     * @throws IllegalArgumentException
     * @throws NullPointerException if atomicObject or LockStatus is null.
     * @throws org.multiverse.api.exceptions.DeadTransactionException if the transaction
     * is dead (so not active).
     */
    Tranlocal privatize(Object atomicObject, LockStatus lockStatus);

    /**
     * Returns the status of the lock of an atomic object.
     *
     * @param atomicObject
     * @return the Lock mode of the atomic object.
     * @throws IllegalArgumentException
     * @throws org.multiverse.api.exceptions.DeadTransactionException if the transaction
     * is dead.
     */
    LockStatus getLockStatus(Object atomicObject);

     /**
     *
     * @param atomicObject
     * @return
     * @throws IllegalArgumentException
     * @throws org.multiverse.api.exceptions.DeadTransactionException if the transaction
     * already is aborted.
     */
    StmLock getLock(Object atomicObject, LockStatus lockStatus);
}
