package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.multiversionedheap.AbstractDeflated;
import org.codehaus.multiverse.multiversionedstm.HandleGenerator;
import org.codehaus.multiverse.multiversionedstm.StmObject;
import org.codehaus.multiverse.util.iterators.EmptyIterator;

import static java.lang.String.format;
import java.util.Iterator;

/**
 * An example of a simple mutable StmObject. The IntegerValue is a container for an int.
 *
 * @author Peter Veentjer.
 */
public class IntegerValue implements StmObject {
    private int value;

    public IntegerValue() {
        this(0);
    }

    public IntegerValue(int value) {
        this.value = value;
        this.handle = HandleGenerator.createHandle();
    }

    public void inc() {
        value++;
    }

    public void setValue(int newValue) {
        this.value = newValue;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return format("IntegerValue(value=%s)", value);
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public boolean equals(Object thatObj) {
        if (thatObj == this)
            return true;

        if (!(thatObj instanceof IntegerValue))
            return false;

        IntegerValue that = (IntegerValue) thatObj;
        return that.value == this.value;
    }

    // ================ generated ======================
    private final long handle;
    private Transaction transaction;
    private DehydratedIntegerValue dehydrated;

    private IntegerValue(DehydratedIntegerValue dehydratedIntegerValue, Transaction transaction) {
        this.handle = dehydratedIntegerValue.___getHandle();
        this.value = dehydratedIntegerValue.value;
        this.dehydrated = dehydratedIntegerValue;
    }

    public long ___getHandle() {
        return handle;
    }

    public DehydratedIntegerValue ___deflate(long version) {
        return new DehydratedIntegerValue(this, version);
    }

    public Iterator<StmObject> ___getFreshOrLoadedStmMembers() {
        return EmptyIterator.INSTANCE;
    }

    public void ___onAttach(Transaction transaction) {
        this.transaction = transaction;
    }

    public Transaction ___getTransaction() {
        return transaction;
    }

    public boolean ___isDirty() {
        if (dehydrated == null)
            return true;

        if (dehydrated.value != value)
            return true;

        return false;
    }

    public boolean ___isImmutable() {
        return false;
    }

    private StmObject next;

    public void setNext(StmObject next) {
        this.next = next;
    }

    public StmObject getNext() {
        return next;
    }

    static class DehydratedIntegerValue extends AbstractDeflated {
        private final int value;

        DehydratedIntegerValue(IntegerValue integerValue, long version) {
            super(integerValue.___getHandle(), version);
            this.value = integerValue.value;
        }

        @Override
        public StmObject ___inflate(Transaction transaction) {
            return new IntegerValue(this, transaction);
        }
    }
}
