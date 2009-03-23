package org.codehaus.multiverse.api;

import org.codehaus.multiverse.utils.Pair;

/**
 * A PessimisticLock provides control on the pessimistic locking behavior of an object in the Stm. A
 * PessimisticLock is reentrant, so acquiring the same PessimisticLock multiple times without releasing it
 * doesn't lead to a deadlock. Whatever happens, when a transaction commits or aborts, all locks are released.
 * <p/>
 * It depends on the Stm implementation if (pessimistic) locking is supported.
 * <p/>
 * <p/>
 * A lock can be in three different levels:
 * <ol>
 * <li>none: nobody has the lock</li>
 * <li>shared: the lock is shared. It could be that the lock is shared between multiple transactions
 * with a shared lock</li>
 * <li>exclusive: the lock is exclusively owned by a transaction. </li>
 * </ol>
 * <p/>
 * Upgrading from sharedlock to
 * <p/>
 * The Lock object has no 'identity'. So equals and hashcode are not implemented. It could be that
 * different Lock instances of the same logical lock are returned.
 * <p/>
 * A PessimisticLock is not threadsafe and should not be shared between transactions. A PessimisticLock only has
 * meaning with a transaction and should never be used outside it. If the transaction is not started (so aborted,
 * committed) all methods will fail with an IllegalStateException.
 * <p/>
 * Blocking is not supported at the moment, but will be in the future. Idea: make it possible that
 * a java.util.concurrent.locks.Lock is used, so stuff like biased locking, adaptive spinning
 * etc will be supported. When blocking is added, there is a risk to get into a deadlock. It should be
 * the responsibility to detect deadlocks.
 *
 * @author Peter Veentjer.
 */
public interface PessimisticLock {

    /**
     * Acquires the LockMode.shared for this Lock. If the Lock already is in exclusive mode by a different
     * transaction, the call fails with a LockAlreadOwnedByOtherTransactionException. If the Lock is already
     * owned in LockMode.shared or LockMode.exclusive by the current Transaction, the call is ignored. If the
     * Lock already is hold in LockMode.exclusive, the call is ignored.
     * <p/>
     * This call is not responsive to interrupts, so it won't throw an InterruptedException.
     *
     * @throws org.codehaus.multiverse.api.exceptions.LockOwnedByOtherTransactionException
     *                               if the lock already is exclusivly owned by a different transaction.
     * @throws IllegalStateException if the transaction isn't in the started state anymore.
     */
    void acquireSharedNoWait();

    /**
     * Acquires the LockMode.exclusive for this Lock. If the Lock owned is acquired by a different
     * transaction, the call fails with a LockAlreadOwnedByOtherTransactionException. If the Lock is already
     * owned in exclusive mode by the current Transaction, the call is ignored.
     * <p/>
     * This call is not responsive to interrupts, so it won't thrown an InterruptedException.
     *
     * @throws org.codehaus.multiverse.api.exceptions.LockOwnedByOtherTransactionException
     *                               if the lock already is owned by a different transaction.
     * @throws IllegalStateException if the transaction isn't in the started state anymore.
     */
    void acquireExclusiveNoWait();

    /**
     * Checks if the Lock is free. If any transaction acquired the lock (even the current transaction)
     * false is returned.
     *
     * @return true if the Lock is free, false otherwise.
     * @throws IllegalStateException if the transaction isn't in the started state anymore.
     */
    boolean isFree();

    /**
     * Returns the LockMode the Lock for the specified handle is in.
     * <p/>
     * <p/>
     * todo: a lock in shared mode can have multiple owners... so the single transactionid is not correct.
     * <p/>
     * todo: allowing to return the owners, also makes using java.util.concurrent.locks.Lock more difficult to
     * use
     *
     * @return a pair containing the owner of the lock and the lockmode. If the lockmode is none, the owner
     *         will be null.
     * @throws IllegalStateException if the transaction isn't in the started state anymore.
     */
    Pair<TransactionId, LockMode> getLockInfo();

    /**
     * Releases the Lock depending on the state the lock is in:
     * <ol>
     * <li>LockMode.none: the call is ignored</li>
     * <li>LockMode.shared: if there are no other Transactions that have a shared lock, the Lock is put in
     * LockMode.none. If there are other Transations that have a shared lock, the Lock remains in LockMode.shared
     * but the Transaction is removed from the shared owners list.
     * </li>
     * <li>LockMode.exclusive: the Lock is put in LockMode.none</li>
     * </ol>
     * If the Lock is shared and the current transaction is not the owner, or if the Lock is in LockMode.exclusive
     * and the current Transaction is not the owner, a LockOwnedByOtherTransaction is thrown. So transactions
     * can't tamper with locks of other transaction.
     * <p/>
     * todo: If a lock is acquired multiple times.. should the release free the lock? or count down?
     *
     * @throws org.codehaus.multiverse.api.exceptions.LockOwnedByOtherTransactionException
     *                               if the Lock is owned by another transaction.
     * @throws IllegalStateException if the transaction isn't in the started state anymore.
     */
    void release();
}
