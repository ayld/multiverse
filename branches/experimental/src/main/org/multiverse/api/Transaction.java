package org.multiverse.api;

import org.multiverse.api.locks.LockManager;

/**
 * All changes on AtomicObjects must be done through a Transaction. The transaction make sure that the changes
 * are:
 * <ol>
 * <li>Atomic: all or nothing gets committed (Failure atomicity)</li>
 * <li>Consistent : all or nothing gets committed</li>
 * <li>Isolated: a transaction is executed isolated from other transactions. Meaning that a transaction won't
 * see changed made by transactions executed concurrently, but it will see changes made by transaction executed
 * before it.</li>
 * </ol>
 * <p/>
 * A Transaction is not threadsafe to use (just like a Hibernate Session is not threadsafe to use). It can
 * be handed over from transaction to transaction, but one needs to be really careful for ThreadLocals.
 * Although the Stm/Transaction implementation don't care about threadlocals, the stuff in front (templates,
 * instrumentation etc) do care about threadlocals.
 *
 * @author Peter Veentjer.
 */
public interface Transaction {

    /**
     * Returns the family name of this Transaction. Every transaction in principle should have
     * a family name. This information can be used for debugging purposes, but also other techniques
     * that rely to know something about similar types of transactions like profiling.
     *
     * @return the familyName. The returned value can be null.
     */
    String getFamilyName();

    /**
     * Returns the clock version of the stm when this Transaction started. This version is
     * needed to provide a transaction level read consistent view. The returned version will
     * always be larger than Long.MIN_VALUE.
     *
     * @return the version of the stm when this Transaction started.
     */
    long getReadVersion();

    /**
     * Returns the status of this Transaction.
     *
     * @return the status of this Transaction.
     */
    TransactionStatus getStatus();

    /**
     * Commits this Transaction. If the Transaction already is committed, the call is ignored.
     * <p/>
     * Transaction will be aborted if the commit does not succeed.
     *
     * @return the new version if the commit was a success, Long.MIN_VALUE otherwise.
     * @throws org.multiverse.api.exceptions.WriteConflictException
     *
     * @throws org.multiverse.api.exceptions.DeadTransactionException
     *          if this transaction already is aborted.
     */
    long commit();

    /**
     * Aborts this Transaction. If the Transaction already is aborted, the call is ignored.
     *
     * @throws org.multiverse.api.exceptions.DeadTransactionException
     *          if this transaction already is committed
     */
    void abort();

    /**
     * Resets this Transaction. Can only be done if the transaction has committed or aborted.
     *
     * @throws org.multiverse.api.exceptions.ResetFailureException
     *          if the reset has failed (for example
     *          because the transaction is still active).
     */
    void reset();

    /**
     * Retries the transaction. This functionality is required for te retry mechanism. The
     * retry mechanism should be used like this:
     * first ask the transaction to retry. It will decide what needs to be done. One of the things
     * that could happen is that a RetryError is thrown. When this happens this error should be
     * caught and the #abortAndRetry should be called.
     *
     * @throws org.multiverse.api.exceptions.NoProgressPossibleException
     *
     * @throws org.multiverse.api.exceptions.DeadTransactionException
     *
     * @throws org.multiverse.api.exceptions.RetryError
     *
     */
    void retry();

    /**
     * Aborts and retries the transaction. This functionality is required for the retry
     * mechanism.
     *
     * @throws org.multiverse.api.exceptions.NoProgressPossibleException
     *          if the retry can't make progress, e.g.
     *          because the transaction has not loaded any object.
     * @throws org.multiverse.api.exceptions.DeadTransactionException
     *          if this transaction already is committed or aborted.
     */
    void abortAndRetry();

    /**
     * Starts the 'or' from the 'orelse' block.
     * <p/>
     * The orelse block is needed for the orelse functionality:
     *
     * @throws org.multiverse.api.exceptions.DeadTransactionException
     *          if this transaction already is committed or aborted.
     * @see #endOr()
     * @see #endOrAndStartElse()
     */
    void startOr();

    /**
     * End the 'or' from the orelse block successfully. No rollbacks are done.
     *
     * @throws org.multiverse.api.exceptions.DeadTransactionException
     *          if this transaction already is committed or aborted.
     * @see #startOr()
     * @see #endOrAndStartElse()
     */
    void endOr();

    /**
     * Ends the 'or' from the orelse block, rolls back all changes made within this block,
     * and starts the else block.
     *
     * @throws org.multiverse.api.exceptions.DeadTransactionException
     *          if this transaction already is committed or aborted.
     * @see #endOr()
     * @see #startOr()
     */
    void endOrAndStartElse();

    /**
     * Returns the LockManager that belongs to the transaction. It depends on the Transaction
     * implementation if a working LockManager is returned.
     *
     * @return the lock manager.
     * @throws org.multiverse.api.exceptions.DeadTransactionException
     *          if this transaction already is committed or aborted.
     */
    LockManager getLockManager();

    /**
     * Registers a task to be executed after the transaction completes. If the transaction
     * aborts, all tasks are discarded. Atm there is no support for post abort tasks.
     * <p/>
     * If the execution of one of the tasks fails, the others won't be executed.
     * <p/>
     * There is no guaranteed order of execution of the tasks.
     * <p/>
     * If the same tasks is added multiple times, it could be executed multiple
     * times (no guarantee).
     * <p/>
     * The tasks will be executed on the current thread.
     * <p/>
     * If the task accesses the STM, it could see changes made after the commit
     * of the current transaction. So they will not be running on the same transaction.
     * <p/>
     * A good usage of this feature is starting up threads. If you need to
     * start threads, you don't want to start them immediately because eventually
     * the transaction could be rolled back. And another problem is that transaction
     * are not able to see the changes already made in the current transaction, because
     * it hasn't completed yet.
     *
     * @param task the task to execute after the transaction completes.
     * @throws NullPointerException if task is null.
     * @throws org.multiverse.api.exceptions.DeadTransactionException
     *                              if transaction already is aborted or committed.
     */
    void executePostCommit(Runnable task);
}
