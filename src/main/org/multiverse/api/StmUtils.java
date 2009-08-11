package org.multiverse.api;

import org.multiverse.api.exceptions.RetryError;
import static org.multiverse.utils.TransactionThreadLocal.getThreadLocalTransaction;

/**
 * A utility class with convenience methods to access {@link org.multiverse.api.Stm} or
 * {@link Transaction}.
 *
 * @author Peter Veentjer.
 */
public final class StmUtils {

    /**
     * See {@link org.multiverse.api.Transaction#executePostCommit(Runnable)}
     *
     * @param task  the task to execute postcommit.
     */
    public static void executePostCommit(Runnable task) {
        Transaction t = getThreadLocalTransaction();
        if (t == null) {
            throw new RuntimeException("No Transaction available");
        }

        t.executePostCommit(task);
    }

            
    public static void retry() {
        throw RetryError.create();
    }

    //we don't want instances.
    private StmUtils() {
    }

}
