package org.multiverse.api;

import org.multiverse.api.exceptions.NoTransactionFoundException;
import org.multiverse.api.exceptions.RetryError;
import static org.multiverse.utils.ThreadLocalTransaction.getRequiredThreadLocalTransaction;

/**
 * A utility class with convenience methods to access the {@link org.multiverse.api.Stm} or
 * {@link Transaction}. These methods can be imported using the static import for a less
 * ugly (but potentially more confusing) syntax.
 *
 * @author Peter Veentjer.
 */
public final class StmUtils {

    /**
     * Does a retry.
     * <p/>
     * Under the hood the retry throws an RetryError that will be caught up the chain
     * (by the AtomicTemplate for example).
     *
     * @throws NoTransactionFoundException if no transaction is found in the ThreadLocalTransaction.
     */
    public static void retry() {
        getRequiredThreadLocalTransaction();
        throw RetryError.create();
    }

    /**
     * Executes {@link Transaction#deferredExecute(Runnable)} on the transaction stored in the
     * ThreadLocalTransaction.
     * <p/>
     * See {@link Transaction#deferredExecute(Runnable)}
     *
     * @param task the task that is executed when the transaction commits.
     * @throws NoTransactionFoundException if no transaction is found in the ThreadLocalTransaction.
     */
    public static void deferredExecute(Runnable task) {
        Transaction t = getRequiredThreadLocalTransaction();
        t.deferredExecute(task);
    }

    /**
     * Executes {@link Transaction#compensatingExecute(Runnable)} on the transaction stored in the
     * ThreadLocalTransaction.
     * <p/>
     * See {@link Transaction#deferredExecute(Runnable)}
     *
     * @param task the task that is executed when the transaction commits.
     * @throws NoTransactionFoundException if no transaction is found in the ThreadLocalTransaction.
     */
    public static void compensatingExecute(Runnable task) {
        Transaction t = getRequiredThreadLocalTransaction();
        t.compensatingExecute(task);
    }

    //we don't want instances.
    private StmUtils() {
    }
}
