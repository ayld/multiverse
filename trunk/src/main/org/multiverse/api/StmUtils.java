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
     * todo: method should be kicked to the stm.alpha package instrumention
     * depends on it for now.. so watch out
     * <p/>
     * Loads a Tranlocal using a transaction. The transaction is retrieved from the
     * TransactionThreadLocal. If no transaction is found, a RuntimeException is thrown.
     * <p/>
     * For more information see {@link Transaction#privatize(Object)} for more info.
     *
     * @param object
     * @return
     */
    public static Tranlocal privatize(Object object) {
        Transaction t = getThreadLocalTransaction();
        if (t == null) {
            throw new RuntimeException("No Transaction available");
        }

        return t.privatize(object);
    }

    /**
     * See {@link org.multiverse.api.Transaction#executePostCommit(Runnable)}
     *
     * @param task
     */
    public static void executePostCommit(Runnable task) {
        Transaction t = getThreadLocalTransaction();
        if (t == null) {
            throw new RuntimeException("No Transaction available");
        }

        t.executePostCommit(task);
    }


    public static Object getAtomicObject(Tranlocal t) {
        return t == null ? null : t.getAtomicObject();
    }

    /**
     * todo: method should be kicked to the stm.alpha package.. instrumention
     * depends on it for now.. so watch out
     * <p/>
     * Attaches a Tranlocal to a transaction. The transaction is retrieved from the
     * TransactionThreadLocal. If no transaction is found, a RuntimeException is thrown.
     * <p/>
     * For more information see
     *
     * @param object
     */
    public static void attachAsNew(Tranlocal object) {
        Transaction t = getThreadLocalTransaction();
        if (t == null) {
            throw new RuntimeException("No Transaction available");
        }

        t.attachNew(object);
    }

    public static void retry() {
        //todo: ipv hier direct een retry error op te gooien, zou je ook contact op kunnen nemen
        //de transactie.. die kan dan bepalen of hij wil spinnen oid.  this could reduce the overhead
        //for a retry significantly. .. short waits... spinning.. long waits... retry error and
        //de a serious block.
        throw RetryError.create();
    }

    //we don't want instances.
    private StmUtils() {
    }
}
