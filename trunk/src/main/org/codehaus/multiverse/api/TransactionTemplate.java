package org.codehaus.multiverse.api;

import org.codehaus.multiverse.api.exceptions.AbortedTransactionException;
import org.codehaus.multiverse.api.exceptions.RetryError;
import org.codehaus.multiverse.api.exceptions.TooManyRetriesException;
import org.codehaus.multiverse.api.exceptions.WriteConflictError;

/**
 * The TransactionTemplate  is a template that contains all plumbing logic for the start, retry etc of
 * Transactions. TransactionTemplate is the prefered way to deal with a {@link Transaction} and {@link Stm}.
 * <p/>
 * It could be compared to one of the Spring templates like the JdbcTemplate.
 * <p/>
 * example:
 * <pre>
 * String result = new TransactionTemplate(stm){
 *     public E execute(Transaction t){
 *          Stack<E> stack = (Stack<E>)t.read(stackHandle);
 *          return stack.take();
 *     }
 * }.execute();
 * </pre>
 * <p/>
 * It really is a shame that closures are not going to be part of Java 7, so we still need to use this
 * ugly anonymous innerclass syntax.
 * <p/>
 * The Transaction used in this TransactionTemplate also can be accessed under the TransactionThreadLocal, so
 * you don't need to drag the instanceof of the Transaction around.
 *
 * @author Peter Veentjer.
 * @param <E> the type of the object to return.
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
        if (stm == null) throw new NullPointerException();
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
        if (maximumRetryCount < 0) throw new IllegalArgumentException();
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

    /**
     * @return
     * @throws AbortedTransactionException if the transaction is aborted.
     * @throws org.codehaus.multiverse.api.exceptions.TooManyRetriesException
     *                                     if the transaction needs to be retried too many times.
     * @throws RuntimeException            all
     */
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
                    if (transaction.getStatus().equals(TransactionStatus.aborted))
                        throw new AbortedTransactionException();

                    transaction.commit();
                    abort = false;
                    return result;
                } catch (RetryError ex) {
                    //with a retryerror, you need to set the predecessor, so that you have access to the
                    //handles read, and make use of the stm version of condition variables. A retry error
                    //indicates that the transaction can't make any progress.
                    predecessor = transaction;
                    retryCount++;
                } catch (WriteConflictError ex) {
                    //with a writeconflict, you don't need a predecessor because you are not interested in
                    //the handles that have been read for the stm-version of condition variables. The transacties
                    //can be retried.
                    retryCount++;
                } finally {
                    TransactionThreadLocal.remove();

                    if (abort)
                        transaction.abort();
                }
            } while (retryCount < maximumRetryCount || maximumRetryCount == 0);

            throw new TooManyRetriesException();
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
            return stm.startRetriedTransaction(predecessor);
    }
}
