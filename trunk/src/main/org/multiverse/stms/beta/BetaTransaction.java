package org.multiverse.stms.beta;

import org.multiverse.api.Transaction;

/**
 * A {@link Transaction} interface tailored for the beta package.
 *
 * @author Peter Veentjer.
 */
public interface BetaTransaction extends Transaction {

    <E> void attachNew(BetaRefTranlocal<E> refTranlocal);

    <E> BetaRefTranlocal<E> privatize(BetaRef<E> ref);

    /**
     * @param ref
     * @param <E>
     * @return
     * @throws NullPointerException if ref is null.
     */
    <E> BetaRefTranlocal<E> load(BetaRef<E> ref);
}
