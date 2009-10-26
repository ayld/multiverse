package org.multiverse.stms.alpha;

import static org.multiverse.utils.ThreadLocalTransaction.getThreadLocalTransaction;

import static java.lang.String.format;

/**
 * @author Peter Veentjer
 */
public final class AlphaStmUtils {

    public static String getLoadUncommittedMessage(AlphaAtomicObject atomicObject) {
        return format("Load uncommitted on atomicobject '%s' ", toAtomicObjectString(atomicObject));
    }

    /**
     * Debug string representation of the atomicobject that belongs to the tranlocal.
     *
     * @param tranlocal
     * @return
     */
    public static String toAtomicObjectString(AlphaTranlocal tranlocal) {
        return toAtomicObjectString(tranlocal.getAtomicObject() == null ? null : tranlocal.getAtomicObject());
    }


    /**
     * Debug representation of the atomicobject.
     *
     * @param atomicObject
     * @return
     */
    public static String toAtomicObjectString(AlphaAtomicObject atomicObject) {
        if (atomicObject == null) {
            return "null";
        }
        return format("%s@%s", atomicObject.getClass().getName(), System.identityHashCode(atomicObject));
    }

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
     * ThreadLocalTransaction. If no transaction is found, a RuntimeException is thrown.
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
     * Checks if there already is a tranlocal attached for the atomicobject.
     * <p/>
     * This method is called by instrumented atomicobjects.
     *
     * @param atomicObject the AtomicObject to check
     * @return true if the atomicObject is attached, false otherwise.
     */
    public static boolean isAttached(AlphaAtomicObject atomicObject) {
        AlphaTransaction t = (AlphaTransaction) getThreadLocalTransaction();
        if (t == null) {
            throw new RuntimeException("No Transaction available");
        }

        return t.isAttached(atomicObject);
    }


    /**
     * Loads a Tranlocal using a transaction. The transaction is retrieved from the
     * ThreadLocalTransaction. If no transaction is found, a RuntimeException is thrown.
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
