package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.multiversionedheap.AbstractDeflated;
import org.codehaus.multiverse.multiversionedstm.HandleGenerator;
import org.codehaus.multiverse.multiversionedstm.StmObject;
import org.codehaus.multiverse.util.iterators.EmptyIterator;

import static java.lang.String.format;
import java.util.Iterator;

/**
 * A Reference is an example of an StmObject that can be used to let a non stm object be placed in and retrieved
 * from an stm. The ref should not be shared concurrently ofcourse. It can be used for messagepassing for example
 * where each message is touched by only one thread at most at any give time. The ref should not be used inside
 * the transaction but only once the transaction completes.`
 *
 * @param <E>
 */
public class Reference<E> implements StmObject {
    private E ref;

    public Reference() {
        this((E) null);
    }

    public Reference(E ref) {
        this.ref = ref;
        this.handle = HandleGenerator.createHandle();
    }

    public void set(E newValue) {
        this.ref = newValue;
    }

    public E get() {
        return ref;
    }

    @Override
    public String toString() {
        return format("IntegerValue(get=%s)", ref);
    }

    @Override
    public int hashCode() {
        return ref == null ? 0 : ref.hashCode();
    }

    @Override
    public boolean equals(Object thatObj) {
        if (thatObj == this)
            return true;

        if (!(thatObj instanceof Reference))
            return false;

        Reference that = (Reference) thatObj;
        if (that.ref == null)
            return this.ref == null;

        return that.ref.equals(this.ref);
    }

    // ================ generated ======================
    private final long handle;
    private DehydratedReference<E> dehydrated;

    private Reference(DehydratedReference<E> dehydratedIntegerValue) {
        this.handle = dehydratedIntegerValue.___getHandle();
        this.ref = dehydratedIntegerValue.ref;
        this.dehydrated = dehydratedIntegerValue;
    }

    public long ___getHandle() {
        return handle;
    }

    public DehydratedReference<E> ___deflate(long version) {
        return dehydrated = new DehydratedReference<E>(this, version);
    }

    public Iterator<StmObject> ___getFreshOrLoadedStmMembers() {
        return EmptyIterator.INSTANCE;
    }

    public boolean ___isDirtyIgnoringStmMembers() {
        if (dehydrated == null)
            return true;

        if (dehydrated.ref != ref)
            return true;

        return false;
    }

    static class DehydratedReference<E> extends AbstractDeflated {
        private final E ref;

        DehydratedReference(Reference<E> reference, long version) {
            super(reference.___getHandle(), version);
            this.ref = reference.ref;
        }

        @Override
        public Reference<E> ___inflate(Transaction transaction) {
            return new Reference<E>(this);
        }
    }
}