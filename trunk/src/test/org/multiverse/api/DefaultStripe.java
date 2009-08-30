package org.multiverse.api;

import org.multiverse.utils.TodoException;
import static org.multiverse.utils.TransactionThreadLocal.getThreadLocalTransaction;

/**
 * @author Peter Veentjer
 */
public class DefaultStripe<E> implements Stripe<E> {
    private final PredictingManagedRef[] refs;

    public DefaultStripe(int refCount) {
        this(getThreadLocalTransaction(), refCount);
    }

    public DefaultStripe(Transaction t, int refCount) {
        if (t == null) {
            throw new NullPointerException();
        }
        refs = new PredictingManagedRef[refCount];
    }

    @Override
    public PredictingManagedRef<E> getFreeRef() {
        throw new TodoException();
    }

    @Override
    public PredictingManagedRef<E> getOccupiedRef() {
        throw new TodoException();
    }

    @Override
    public boolean isFull() {
        throw new TodoException();
    }
}
