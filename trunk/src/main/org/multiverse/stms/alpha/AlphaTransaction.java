package org.multiverse.stms.alpha;

import org.multiverse.api.Transaction;

/**
 * A {@link Transaction} interface tailored for the alpha STM.
 *
 * @author Peter Veentjer.
 */
public interface AlphaTransaction extends Transaction {

    /**
     * Loads the Tranlocal for the specified owner. It depends on the transaction if it
     * can be used for updates or not. But because each tranlocal is protected against updates
     * after it has been committed, nothing can go wrong if a committed version is 'abused'
     * <p/>
     * If item is null, the return value is null.
     * <p/>
     *
     * @param owner AtomicObject to get the transaction local state for.
     * @return the loaded Tranlocal.
     *         todo: @throws IllegalArgumentException if the owner is not a {@link org.multiverse.stms.alpha.AlphaAtomicObject}.
     * @throws org.multiverse.api.exceptions.LoadException
     *          if something goes wrong while loading.
     * @throws org.multiverse.api.exceptions.DeadTransactionException
     *          if this transaction already is committed or aborted.
     */
    AlphaTranlocal load(AlphaAtomicObject owner);

    /**
     * Attaches the Tranlocal to this Transaction. This call is needed for newly created AtomicObjects
     * so that the first tranlocal is registerd
     *
     * @param tranlocal the Tranlocal to attach.
     * @throws NullPointerException if tranlocal is null.
     * @throws org.multiverse.api.exceptions.DeadTransactionException
     *                              if this transaction already is committed or aborted.
     */
    void attachNew(AlphaTranlocal tranlocal);
}
