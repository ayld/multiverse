package org.multiverse.api;

/**
 * All changes on AtomicObjects must be done through a Transaction. The transaction make sure that the changes
 * are:
 * <ol>
 * <li>Atomic: all or nothing gets committed (Failure atomicity)</li>
 * <li>Consistent : </li>
 * <li>Isolated: a transaction is executed isolated from other transactions. Meaning that a transaction won't
 * see changed made by transactions executed concurrently, but it will see changes made by transaction completed
 * before.</li>
 * </ol>
 * <p/>
 * A Transaction is not thread-safe to use (just like a Hibernate Session is not thread-safe to use). It can
 * be handed over from transaction to transaction, but one needs to be really careful with threadlocals.
 * Although the Stm/Transaction implementation don't care about threadlocals, the stuff in front (templates,
 * instrumentation etc) could care about threadlocals.
 *
 * @author Peter Veentjer.
 */
public interface Transaction {

    /**
     * Returns the family name of this Transaction. Every transaction in principle should have
     * a family name. This information can be used for debugging/logging purposes but also other
     * techniques that rely to know something about similar types of transactions like profiling.
     *
     * @return the familyName. The returned value can be null.
     */
    String getFamilyName();

    /**
     * Returns the clock version of the stm when this Transaction started. This version is
     * needed to provide a transaction level read consistent view (so a transaction will always
     * see a stable view of the objects at some point in time). The returned version will
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
     * @return the new version if the commit was a success. If the there are no changes, the readVersion
     *         if the transaction is returned. Otherwise the writeVersion of the transaction is returned.
     * @throws org.multiverse.api.exceptions.WriteConflictException
     *
     * @throws org.multiverse.api.exceptions.DeadTransactionException
     *          if this transaction already is aborted.
     */
    long commit();

    /**
     * Aborts this Transaction. This means that the changes made in this transaction are not
     * committed. It depends on the implementation if this operation is simple (ditching objects
     * for example), or if changes need to be rolled back.
     * <p/>
     * If the Transaction already is aborted, the call is ignored.
     * <p/>
     * It is important that the abort never fails.
     *
     * @throws org.multiverse.api.exceptions.DeadTransactionException
     *          if this transaction already is committed
     */
    void abort();

    /**
     * Restarts this Transaction by returning a Transaction. Can only be done if the transaction has been
     * committed or aborted.
     * <p/>
     * It could be that the same instance is returned just by resetting the instance.
     *
     * @throws org.multiverse.api.exceptions.ResetFailureException
     *          if the restart has failed (for example
     *          because the transaction is still active).
     */
    Transaction restart();

    /**
     * Aborts and waits till this transaction can be retried. This functionality is required for the retry
     * mechanism and is something different than 'just' aborting an retrying the transaction. If you want to
     * do that, you need to call an abort followed by a restart.
     *
     * @throws org.multiverse.api.exceptions.NoProgressPossibleException
     *          if the retry can't make progress, e.g.
     *          because the transaction has not loaded any object.
     * @throws org.multiverse.api.exceptions.DeadTransactionException
     *          if this transaction already is committed or aborted.
     */
    void abortAndWaitForRetry();

    /**
     * Starts the 'or' from the 'orelse' block.
     * <p/>
     * The orelse block is needed for the orelse functionality:
     *
     * @throws org.multiverse.api.exceptions.DeadTransactionException
     *                                       if this transaction already is committed or aborted.
     * @throws UnsupportedOperationException if the implementation doesn't support it.
     * @see #endOr()
     * @see #endOrAndStartElse()
     */
    void startOr();

    /**
     * End the 'or' from the orelse block successfully. No rollbacks are done.
     *
     * @throws org.multiverse.api.exceptions.DeadTransactionException
     *                                       if this transaction already is committed or aborted.
     * @throws UnsupportedOperationException if the implementation doesn't support it.
     * @see #startOr()
     * @see #endOrAndStartElse()
     */
    void endOr();

    /**
     * Ends the 'or' from the orelse block, rolls back all changes made within this block,
     * and starts the else block.
     *
     * @throws org.multiverse.api.exceptions.DeadTransactionException
     *                                       if this transaction already is committed or aborted.
     * @throws UnsupportedOperationException if the implementation doesn't support it.
     * @see #endOr()
     * @see #startOr()
     */
    void endOrAndStartElse();

    /**
     * Registers a task to be executed <b>after</b> the transaction completes. If the transaction
     * aborts, all tasks are discarded.
     * <p/>
     * If the execution of one of the tasks fails, the others won't be executed.
     * <p/>
     * There is no guaranteed order of execution of the tasks.
     * <p/>
     * If the same tasks is added multiple times, it could be executed multiple
     * times (no guarantee).
     * <p/>
     * The tasks will be executed on the current thread that calls the commit.
     * <p/>
     * If the task accesses the STM, it could see changes made after the commit
     * of the current transaction. So they will not be running on the same transaction.
     * <p/>
     * A good use case of this feature is starting up threads. If you need to
     * start threads, you don't want to start them immediately because eventually
     * the transaction could be aborted. And another problem is that new transaction started
     * by spawned threads are not able to see the changes already made in the current transaction,
     * because the current transaction hasn't completed yet.
     *
     * @param task the task to execute after the transaction completes.
     * @throws NullPointerException if task is null.
     * @throws org.multiverse.api.exceptions.DeadTransactionException
     *                              if transaction already is aborted or committed.
     */
    void deferredExecute(Runnable task);

    /**
     * Registers a task to be executed <b>after</b> the transaction aborts. If the transaction
     * commits, all tasks are discarded.
     * <p/>
     * If the execution of one of the tasks fails, the others won't be executed.
     * <p/>
     * There is no guaranteed order of execution of the tasks.
     * <p/>
     * If the same tasks is added multiple times, it could be executed multiple
     * times (no guarantee).
     * <p/>
     * The tasks will be executed on the current thread that aborts the transaction.
     * <p/>
     * A good use case of this feature is cleaning up resources.
     * <p/>
     * It is important to remember that the task is not going to see the changes
     * made by the transaction, because the transaction aborts before the task
     * is executed.
     *
     * @param task the task to execute after the transaction aborts.
     * @throws NullPointerException if task is null.
     * @throws org.multiverse.api.exceptions.DeadTransactionException
     *                              if transaction already is aborted or committed.
     */
    void compensatingExecute(Runnable task);
}
