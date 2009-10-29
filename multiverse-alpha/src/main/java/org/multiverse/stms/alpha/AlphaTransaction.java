package org.multiverse.stms.alpha;

import org.multiverse.api.Transaction;

/**
 * A {@link Transaction} interface tailored for the Alpha STM.
 * <p/>
 * AlphaTransaction is just like the Transaction not thread safe.
 *
 * @author Peter Veentjer.
 */
public interface AlphaTransaction extends Transaction {

    /**
     * Loads the Tranlocal for the specified atomicObject. It depends on the transaction if it
     * can be used for updates or not. But because each tranlocal is protected against updates
     * after it has been committed, nothing can go wrong if a committed version is 'abused'
     * (it is immutable after commit).
     * <p/>
     * If atomicObject is null, the return value is null.
     *
     * @param atomicObject the atomicObject to get the tranlocal for.
     * @return the loaded Tranlocal. If atomicObject is null, the returned value will be null. Otherwise
     *         a tranlocal is returned.
     * @throws org.multiverse.api.exceptions.LoadException
     *          if something goes wrong while loading.
     * @throws org.multiverse.api.exceptions.DeadTransactionException
     *          if this transaction already is committed or aborted.
     */
    AlphaTranlocal load(AlphaAtomicObject atomicObject);

    /**
     * Attaches the tranlocal to this Transaction. This call needs to be done when the initial
     * AlphaAtomicObject is created.
     * <p/>
     * When a Transaction is aborted or if the AtomicObject is used inside another transaction
     * before this one commits, we get in a strange situation because there is no Tranlocal content
     * for that AtomicObject in the other transactions. So you get
     * {@link org.multiverse.api.exceptions.LoadUncommittedException}. So it is very important
     * that the atomicobject doesn't escape the transaction.
     * <p/>
     * Essentially this is a specialization of a more common case: letting the atomicobject
     * escape from a transaction. In principle this is no problem because the atomic object
     * essentially has become immutable. It is even desirable, but the receiving transaction will
     * receive its own tranlocal will not see changes made in other concurrent transactions.
     *
     * @param tranlocal the Tranlocal to attach.
     * @throws NullPointerException if tranlocal is null.
     * @throws org.multiverse.api.exceptions.DeadTransactionException
     *                              if this transaction already is committed or aborted.
     */
    void attachNew(AlphaTranlocal tranlocal);

    /**
     * @param atomicObject
     * @return {@code true} iff the atomicobject is attached
     */
    boolean isAttached(AlphaAtomicObject atomicObject);
}
