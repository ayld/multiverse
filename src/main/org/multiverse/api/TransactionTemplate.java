package org.multiverse.api;

import org.multiverse.api.exceptions.*;

/**
 * idea: Could also be created by a factory method of the stm.
 *
 * @author Peter Veentjer.
 * @param <E>
 */
public abstract class TransactionTemplate<E> {

    private final Stm stm;
    private int maximumRetryCount = 0;

    /**
     * Creates a new TransactionTemplate.
     *
     * @param stm the Stm this TransactionTemplate uses.
     * @throws NullPointerException if stm is null.
     */
    protected TransactionTemplate(Stm stm) {
        if (stm == null) {
            throw new NullPointerException();
        }
        this.stm = stm;
    }

    /**
     * Gets the maximum retry count. 0 indicates never stop retrying.
     *
     * @return the maximum retry count.
     */
    public int getMaximumRetryCount() {
        return maximumRetryCount;
    }

    /**
     * Sets the maximum retry count. A value of 0 means never stop retrying.
     *
     * @param maximumRetryCount the value to set.
     * @throws IllegalArgumentException if maximumRetryCount smaller than 0.
     */
    public void setMaximumRetryCount(int maximumRetryCount) {
        if (maximumRetryCount < 0) {
            throw new IllegalArgumentException();
        }
        this.maximumRetryCount = maximumRetryCount;
    }

    /**
     * Returns the Stm used by this TransactionTemplate.
     *
     * @return the Stm used by this TransactionTemplate.
     */
    public Stm getStm() {
        return stm;
    }

    abstract protected E execute(Transaction t) throws Exception;

    public final E execute() {
        try {
            Transaction predecessor = null;
            int retryCount = 0;
            do {
                Transaction transaction = startTransaction(predecessor);
                predecessor = null;
                boolean abort = true;
                TransactionThreadLocal.set(transaction);
                try {
                    E result = execute(transaction);
                    if (transaction.getState().equals(TransactionState.aborted))
                        throw new AbortedTransactionException();

                    transaction.commit();
                    abort = false;
                    return result;
                } catch (RetryError ex) {
                    //with a retryerror, you need to set the predecessor, so that you have access to the
                    //handles read, and make use of the multiversionedstm version of condition variables. A retry error
                    //indicates that the transaction can't make any progress.
                    predecessor = transaction;
                    retryCount++;
                    abort = false;
                } catch (WriteConflictException ex) {
                    //with a writeconflict, you don't need a predecessor because you are not interested in
                    //the handles that have been read for the multiversionedstm-version of condition variables. The transacties
                    //can be retried.
                    retryCount++;
                } catch (SnapshotTooOldException ex) {
                    //with a writeconflict, you don't need a predecessor because you are not interested in
                    //the handles that have been read for the multiversionedstm-version of condition variables. The transacties
                    //can be retried.
                    retryCount++;
                } catch (StarvationException ex) {
                    retryCount++;
                }
                finally {
                    TransactionThreadLocal.remove();

                    if (abort)
                        transaction.abort();
                }
            } while (retryCount < maximumRetryCount || maximumRetryCount == 0);

            throw new StarvationException();
        } catch (RuntimeException ex) {
            //we don't want unchecked exceptions to be wrapped again.
            throw ex;
        } catch (InterruptedException ex) {
            Thread.interrupted();
            throw new RuntimeException(ex);
        } catch (Exception ex) {
            //wrap the checked exception in an unchecked one.
            throw new RuntimeException(ex);
        }
    }

    private Transaction startTransaction(Transaction predecessor) throws InterruptedException {
        if (predecessor == null)
            return stm.startTransaction();
        else
            return predecessor.abortAndRetry();
    }
}
