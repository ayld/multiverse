package org.multiverse.stms.alpha;

import org.multiverse.api.Transaction;
import static org.multiverse.utils.TransactionThreadLocal.getThreadLocalTransaction;

/**
 * @author Peter Veentjer
 */
public final class AlphaStmUtils {

    /**
     * @param t
     * @return
     */
    public static Object getAtomicObject(Tranlocal t) {
        return t == null ? null : t.getAtomicObject();
    }

    /**
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

    /**
     * Loads a Tranlocal using a transaction. The transaction is retrieved from the
     * TransactionThreadLocal. If no transaction is found, a RuntimeException is thrown.
     * <p/>
     * For more information see {@link org.multiverse.api.Transaction#privatize(Object)} for more info.
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


    private AlphaStmUtils() {
    }
}
