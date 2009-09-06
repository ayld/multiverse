package org.multiverse.stms.alpha;

import static org.multiverse.utils.TransactionThreadLocal.getThreadLocalTransaction;

/**
 * @author Peter Veentjer
 */
public final class AlphaStmUtils {

    /**
     * Gets the AtomicObject for the provided AlphaTranlocal.
     *
     * @param tranlocal the AlphaTranlocal.
     * @return the AlphaAtomicObject that belongs to the tranlocal, or null if tranlocal is null.
     */
    public static AlphaAtomicObject getAtomicObject(AlphaTranlocal tranlocal) {
        return tranlocal == null ? null : tranlocal.getAtomicObject();
    }

    /**
     * Attaches a Tranlocal to a transaction. The transaction is retrieved from the
     * TransactionThreadLocal. If no transaction is found, a RuntimeException is thrown.
     * <p/>
     * This method is called by instrumented atomicobjects.
     *
     * @param tranlocal the AlphaTranlocal to attach.
     */
    public static void attachAsNew(AlphaTranlocal tranlocal) {
        AlphaTransaction t = (AlphaTransaction) getThreadLocalTransaction();
        if (t == null) {
            throw new RuntimeException("No Transaction available");
        }

        t.attachNew(tranlocal);
    }

    /**
     * Loads a Tranlocal using a transaction. The transaction is retrieved from the
     * TransactionThreadLocal. If no transaction is found, a RuntimeException is thrown.
     * <p/>
     * For more information see {@link AlphaTransaction#load(AlphaAtomicObject)}
     * <p/>
     * This method is called by instrumented atomicobjects.
     *
     * @param atomicObject the AlphaAtomicObject.
     * @return the AlphaTranlocal
     */
    public static AlphaTranlocal load(Object atomicObject) {
        AlphaTransaction t = (AlphaTransaction) getThreadLocalTransaction();
        if (t == null) {
            throw new RuntimeException("No Transaction available");
        }

        return t.load((AlphaAtomicObject) atomicObject);
    }

    //we don't want instances
    private AlphaStmUtils() {
    }
}
