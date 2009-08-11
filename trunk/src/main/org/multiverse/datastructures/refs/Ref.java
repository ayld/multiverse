package org.multiverse.datastructures.refs;

import org.multiverse.api.annotations.AtomicObject;
import static org.multiverse.api.StmUtils.retry;

import static java.lang.String.format;

/**
 * Default {@link org.multiverse.datastructures.refs.ManagedRef}. Changes on refs are atomic and consistent,
 * but not completely because a transaction could suffer from the ABA problem between transactions.
 * See the {@link org.multiverse.datastructures.refs.AbaRef} to solve this problem.
 *
 * It depends on the STM implementation if the ABA problem can occur btw. If the readset also
 * is included in the conflict detection, the ABA problem can't occur even with this implementation.
 *
 * @author Peter Veentjer
 */
@AtomicObject
public final class Ref<E> implements ManagedRef<E> {
    private E reference;

    /**
     * Creates a Ref with a null reference.
     */
    public Ref(){
        reference = null;
    }

    /**
     * Creates a new Ref with the provided reference. This reference is allowed to
     * be null.
     *
     * @param reference the reference to store in this Ref.
     */
    public Ref(E reference) {
        this.reference = reference;
    }

    @Override
    public E getOrAwait() {
        if (reference == null) {
            retry();
        }

        return reference;
    }

    @Override
    public boolean isNull() {
        return reference == null;
    }

    @Override
    public E get() {
        return reference;
    }

    @Override
    public E set(E newRef) {
        E oldRef = reference;
        this.reference = newRef;
        return oldRef;
    }

    @Override
    public E clear(){
        return set(null);
    }

    @Override
    public String toString(){
        if(reference == null){
            return "Ref(ref=null)";
        }else{
            return format("Ref(ref=%s)", reference);
        }
    }
}
