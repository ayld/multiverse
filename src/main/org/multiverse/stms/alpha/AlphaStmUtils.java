package org.multiverse.stms.alpha;

import static org.multiverse.utils.TransactionThreadLocal.getThreadLocalTransaction;

/**
 * @author Peter Veentjer
 */
public final class AlphaStmUtils {

    /**
     * @param t
     * @return
     */
    public static Object getAtomicObject(AlphaTranlocal t) {
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
    public static void attachAsNew(AlphaTranlocal object) {
        AlphaTransaction t = (AlphaTransaction) getThreadLocalTransaction();
        if (t == null) {
            throw new RuntimeException("No Transaction available");
        }

        t.attachNew(object);
    }

    /**
     * Loads a Tranlocal using a transaction. The transaction is retrieved from the
     * TransactionThreadLocal. If no transaction is found, a RuntimeException is thrown.
     * <p/>
     * For more information see {@link AlphaTransaction#privatize(AlphaAtomicObject)}
     * for more info.
     *
     * @param object
     * @return
     */
    public static AlphaTranlocal privatize(Object object) {
        AlphaTransaction t = (AlphaTransaction) getThreadLocalTransaction();
        if (t == null) {
            throw new RuntimeException("No Transaction available");
        }

        return t.privatize((AlphaAtomicObject) object);
    }


    private AlphaStmUtils() {
    }
}
