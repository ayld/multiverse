package org.multiverse.api;

import static org.multiverse.utils.TransactionThreadLocal.getThreadLocalTransaction;

/**
 * A utility class with convenience methods to access the {@link org.multiverse.api.Stm} or
 * {@link Transaction}.
 *
 * @author Peter Veentjer.
 */
public final class StmUtils {

    /**
     * See the {@link Transaction#retry()}
     */
    public static void retry() {
        Transaction t = getThreadLocalTransaction();
        if (t == null) {
            throw new RuntimeException("No Transaction available");
        }

        t.retry();
    }

    /**
     * See {@link Transaction#executePostCommit(Runnable)}
     *
     * @param task the task to execute postcommit.
     */
    public static void executePostCommit(Runnable task) {
        Transaction t = getThreadLocalTransaction();
        if (t == null) {
            throw new RuntimeException("No Transaction available");
        }

        t.executePostCommit(task);
    }

    //we don't want instances.
    private StmUtils() {
    }
}
