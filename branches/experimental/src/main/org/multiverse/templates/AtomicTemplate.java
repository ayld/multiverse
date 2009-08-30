package org.multiverse.templates;

import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionStatus;
import org.multiverse.api.exceptions.CommitFailureException;
import org.multiverse.api.exceptions.LoadException;
import org.multiverse.api.exceptions.RetryError;
import org.multiverse.api.exceptions.TooManyRetriesException;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.getThreadLocalTransaction;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

/**
 * A Template that handles the boilerplate code for transactions. A transaction will be placed if
 * none if available around a section and if all goes right, commits at the end.
 * <p/>
 * example:
 * <pre>
 * new AtomicTemplate(){
 *    Object execute(Transaction t){
 *        queue.push(1);
 *        return null;
 *    }
 * }.execute();
 * </pre>
 * <p/>
 * It could also be that the transaction is retried (e.g. caused by optimistic locking failures). This is also a
 * task for template. In the future this retry behavior will be customizable.
 * <p/>
 * If a transaction already is available on the TransactionThreadLocal, no new transaction is started
 * and essentially the whole AtomicTemplate is ignored.
 * <p/>
 * If no transaction is available on the TransactionThreadLocal, a new one will be created and used
 * during the execution of the AtomicTemplate and will be removed once the AtomicTemplate finishes.
 * <p/>
 * All uncaught throwable's lead to a rollback of the transaction.
 * <p/>
 * AtomicTemplates are not threadsafe to use.
 * <p/>
 * AtomicTemplates can completely work without threadlocals. See the
 * {@link AtomicTemplate#AtomicTemplate(org.multiverse.api.Stm , boolean)} for more information.
 *
 * @author Peter Veentjer
 */
public abstract class AtomicTemplate<E> {
    private final Stm stm;
    private final boolean ignoreThreadLocalTransaction;
    private int retryCount = Integer.MAX_VALUE;
    private int attemptCount;

    /**
     * Creates a new AtomicTemplate that uses the STM stored in the GlobalStm and
     * works the the {@link org.multiverse.utils.TransactionThreadLocal}.
     */
    public AtomicTemplate() {
        this(GlobalStmInstance.get());
    }

    /**
     * Creates a new AtomicTemplate using the provided stm. The transaction used
     * is stores/retrieved from the {@link org.multiverse.utils.TransactionThreadLocal}.
     *
     * @param stm the stm to use for transactions.
     * @throws NullPointerException if stm is null.
     */
    public AtomicTemplate(Stm stm) {
        this(stm, false);
    }

    /**
     * Creates a new AtomicTemplate that uses the provided STM. This method is provided
     * to make Multiverse easy to integrate with environment that don't want to depend on
     * threadlocals.
     *
     * @param stm                          the stm to use for transactions.
     * @param ignoreThreadLocalTransaction true if this Template should completely ignore
     *                                     the ThreadLocalTransaction. This is useful for using the AtomicTemplate in other
     *                                     environments that don't want to depend on threadlocals but do want to use the AtomicTemplate.
     * @throws NullPointerException if stm is null.
     */
    public AtomicTemplate(Stm stm, boolean ignoreThreadLocalTransaction) {
        if (stm == null) {
            throw new NullPointerException();
        }
        this.stm = stm;
        this.ignoreThreadLocalTransaction = ignoreThreadLocalTransaction;
    }

    /**
     * Returns the current attempt. Value will always be larger than zero and increases
     * everytime the transaction needs to be retried.
     *
     * @return the current attempt count.
     */
    public int getAttemptCount() {
        return attemptCount;
    }

    /**
     * Returns the number of retries that this AtomicTemplate is allowed to do. The returned
     * value will always be equal or larger than 0.
     *
     * @return the number of retries.
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * Sets the number of retries this AtomicTemplate is allowed to do.
     *
     * @param newRetryCount the new retryCount.
     * @throws IllegalArgumentException if retryCount smaller than 0.
     */
    public void setRetryCount(int newRetryCount) {
        if (newRetryCount < 0) {
            throw new IllegalArgumentException();
        }
        this.retryCount = newRetryCount;
    }

    /**
     * This is the method that needs to be implemented.
     *
     * @param t the transaction used for this execution.
     * @return the result of the execution.
     * @throws Exception
     */
    public abstract E execute(Transaction t) throws Exception;

    public final E execute() {
        return execute((String) null);
    }

    /**
     * Executes the template.
     *
     * @return the result of the {@link #execute(org.multiverse.api.Transaction)} method.
     * @throws InvisibleCheckedException if a checked exception was thrown while executing the
     *                                   {@link #execute(org.multiverse.api.Transaction)} method.
     */

    public final E execute(String familyName) {
        try {
            return executeChecked(familyName);
        } catch (Exception ex) {
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            } else {
                throw new AtomicTemplate.InvisibleCheckedException(ex);
            }
        }
    }

    public final E executeChecked() throws Exception {
        //todo: this method will be removed since familyName usage should be promoted
        return executeChecked(null);
    }

    /**
     * Executes the Template and rethrows the checked exception instead of wrapping it
     * in a InvisibleCheckedException.
     *
     * @return the result
     * @throws Exception the Exception thrown inside the {@link #execute(org.multiverse.api.Transaction)}
     *                   method.
     */
    public final E executeChecked(String familyName) throws Exception {
        Transaction t = getTransaction();
        if (t == null || t.getStatus() != TransactionStatus.active) {
            t = stm.startUpdateTransaction(familyName);
            setTransaction(t);
            try {
                attemptCount = 1;
                while (attemptCount <= retryCount) {
                    boolean abort = true;
                    try {
                        E result = execute(t);
                        t.commit();
                        abort = false;
                        return result;
                    } catch (RetryError e) {
                        t.abortAndRetry();
                        //since the abort is already done, no need to do it again.
                        abort = false;
                    } catch (CommitFailureException ex) {
                        //ignore, just retry the transaction
                    } catch (LoadException ex) {
                        //ignore, just retry the transaction
                    } finally {
                        if (abort) {
                            t.abort();
                            t.reset();
                        }
                    }
                    attemptCount++;
                }

                throw new TooManyRetriesException();
            } finally {
                setTransaction(null);
            }
        } else {
            return execute(t);
        }
    }

    /**
     * Gets the current Transaction stored in the TransactionThreadLocal.
     * <p/>
     * If the ignoreThreadLocalTransaction is set, the threadlocal stuff
     * is completeley ignored.
     *
     * @return the found transaction, or null if none is found.
     */
    private Transaction getTransaction() {
        return ignoreThreadLocalTransaction ? null : getThreadLocalTransaction();
    }

    /**
     * Stores the transaction in the TransactionThreadLocal.
     * <p/>
     * This call is ignored if the ignoreThreadLocalTransaction is true.
     *
     * @param t the transaction to set (is allowed to be null).
     */
    private void setTransaction(Transaction t) {
        if (!ignoreThreadLocalTransaction) {
            setThreadLocalTransaction(t);
        }
    }

    public static class InvisibleCheckedException extends RuntimeException {
        public InvisibleCheckedException(Exception cause) {
            super(cause);
        }

        @Override
        public Exception getCause() {
            return (Exception) super.getCause();
        }
    }
}
