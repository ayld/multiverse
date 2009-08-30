package org.multiverse.stms.beta;

import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.stms.AbstractTransaction;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A readonly BetaTransaction implementation. This implementation doesn't increase in size with long transactions
 * (unlike update transactions).
 *
 * @author Peter Veentjer.
 */
public class ReadonlyBetaTransaction extends AbstractTransaction implements BetaTransaction {

    public ReadonlyBetaTransaction(String familyName, AtomicLong clock) {
        super(familyName, clock, null);

        init();
    }

    @Override
    public <E> void attachNew(BetaRefTranlocal<E> refTranlocal) {
        throw new ReadonlyException();
    }

    @Override
    public <E> BetaRefTranlocal<E> privatize(BetaRef<E> ref) {
        throw new ReadonlyException();
    }

    @Override
    public <E> BetaRefTranlocal<E> load(BetaRef<E> ref) {
        switch (status) {
            case active:
                return ref == null ? null : ref.load(readVersion);
            case committed:
                throw new DeadTransactionException("Can't load from a committed transaction");
            case aborted:
                throw new DeadTransactionException("Can't load from an aborted transaction");
            default:
                throw new IllegalStateException();
        }
    }
}
