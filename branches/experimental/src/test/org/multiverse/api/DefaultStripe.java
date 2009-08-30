package org.multiverse.api;

import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.api.annotations.Exclude;
import static org.multiverse.api.StmUtils.retry;
import org.multiverse.datastructures.refs.ManagedRef;
import org.multiverse.datastructures.refs.Ref;
import org.multiverse.utils.TodoException;

/**
 * @author Peter Veentjer
 */
public class DefaultStripe<E> implements Stripe<E> {

    private final StripeRef[] stripe;

    public DefaultStripe(int cellCount) {
        stripe = new StripeRef[cellCount];

        for (int k = 0; k < cellCount; k++) {
            stripe[k] = new StripeRef();
        }
    }

    @Override
    public boolean isFull() {
        return false;  //todo
    }

    @Override
    public ManagedRef<E> getFreeRef() {

        //int preferedIndex = getPreferedIndex();
        //StripeRef<E> ref = stripe[preferedIndex];

        //if our location is free, just put it there.
        //if (!ref.writePending) {
        //    return (ManagedRef<E>) ref;
        //}

        //our location is not free, lets see if we
        //can find another location.
        throw new TodoException();
    }

    @Override
    public ManagedRef<E> getOccupiedRef() {
        return null;  //todo
    }

    static class Cell<E> {
        volatile boolean writePending = false;

        final Ref<E> ref = new Ref<E>();


    }

    @AtomicObject
    static class StripeRef<E> implements ManagedRef<E> {

        @Exclude
        private volatile boolean spacePending;
        @Exclude
        private volatile boolean itemPending;

        private E ref;

        @Override
        public E clear() {
            return set(null);
        }

        @Override
        public E get() {
            return ref;
        }

        @Override
        public E getOrAwait() {
            if(ref == null){
                retry();
            }

            return ref;
        }

        @Override
        public E set(E newRef) {
            if (ref == newRef) {
                return ref;
            }

            E oldRef = ref; 
            ref = newRef;
            return oldRef;
        }

        @Override
        public boolean isNull() {
            return ref == null;
        }
    }
}
