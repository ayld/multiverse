package org.multiverse.stms.alpha;

import org.multiverse.api.Transaction;

/**
 * A {@link Transaction} interface tailored for the alpha STM.
 *
 * @author Peter Veentjer.
 */
public interface AlphaTransaction extends Transaction {

    /**
     * Loads an immutable Tranlocal for the specified atomicObject. It could be that a mutable
     * version is given to provide transaction level read consistency..
     * <p/>
     * If item is null, the return value is null.
     * <p/>
     * The reason why the owner is not of type {@link org.multiverse.stms.alpha.AlphaAtomicObject} is that while developing this interface
     * is not placed on Atomic objects.
     * <p/>
     *
     * @param atomicObject AtomicObject to get the Tranlocal for.
     * @return the loaded Tranlocal.
     *         todo:  @throws IllegalArgumentException if the owner is not a {@link org.multiverse.stms.alpha.AlphaAtomicObject}.
     * @throws org.multiverse.api.exceptions.LoadException
     *          if something goes wrong while loading.
     * @throws org.multiverse.api.exceptions.DeadTransactionException
     *          if this transaction already is committed or aborted
     * @see #privatize(AlphaAtomicObject)
     */
    AlphaTranlocal load(AlphaAtomicObject atomicObject);

    /**
     * Loads a privatized Tranlocal for the specified owner. This privatized Tranlocal can be
     * used for updates.
     * <p/>
     * If item is null, the return value is null.
     * <p/>
     * The reason why the owner is not of type {@link org.multiverse.stms.alpha.AlphaAtomicObject} is that while developing this interface
     * is not placed on Atomic objects (this is done by instrumentation).
     * <p/>
     *
     * @param owner AtomicObject to get the transaction local state for.
     * @return the loaded Tranlocal.
     *         todo: @throws IllegalArgumentException if the owner is not a {@link org.multiverse.stms.alpha.AlphaAtomicObject}.
     * @throws org.multiverse.api.exceptions.LoadException
     *          if something goes wrong while loading.
     * @throws org.multiverse.api.exceptions.DeadTransactionException
     *          if this transaction already is committed or aborted.
     * @see #load(AlphaAtomicObject)
     */
    AlphaTranlocal privatize(AlphaAtomicObject owner);

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
