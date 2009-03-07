package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.multiversionedheap.AbstractDeflated;
import org.codehaus.multiverse.multiversionedstm.HandleGenerator;
import org.codehaus.multiverse.multiversionedstm.MyTransaction;
import org.codehaus.multiverse.multiversionedstm.StmObject;
import org.codehaus.multiverse.util.iterators.EmptyIterator;

import static java.lang.String.format;
import java.util.Iterator;

/**
 * The IntegerConstant is an example of a immutable StmObject.
 *
 * @author Peter Veentjer.
 */
public class IntegerConstant implements StmObject {
    private final int value;

    public IntegerConstant(int value) {
        this.handle = HandleGenerator.createHandle();
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public boolean equals(Object thatObj) {
        if (thatObj == this)
            return true;

        if (!(thatObj instanceof IntegerConstant))
            return false;

        IntegerConstant that = (IntegerConstant) thatObj;
        return that.value == this.value;
    }

    public String toString() {
        return format("IntegerConstant(get=%s)", value);
    }

    // ==================== generated ================

    private final long handle;
    private DehydratedIntegerConstant dehydrated;

    public long ___getHandle() {
        return handle;
    }

    public DehydratedIntegerConstant ___deflate(long commitVersion) {
        dehydrated = new DehydratedIntegerConstant(this, commitVersion);
        return dehydrated;
    }

    public Iterator<StmObject> ___getFreshOrLoadedStmMembers() {
        return EmptyIterator.INSTANCE;
    }

    public void ___onAttach(MyTransaction transaction) {
        throw new RuntimeException();
    }

    public MyTransaction ___getTransaction() {
        return null;
    }

    public boolean ___isDirtyIgnoringStmMembers() {
        return false;
    }

    public boolean ___isImmutableObjectGraph() {
        return true;
    }

    public static class DehydratedIntegerConstant extends AbstractDeflated {
        private final IntegerConstant instance;

        DehydratedIntegerConstant(IntegerConstant instance, long commitVersion) {
            super(instance.___getHandle(), commitVersion);
            this.instance = instance;
        }

        @Override
        public StmObject ___inflate(Transaction transaction) {
            return instance;
        }

        @Override
        public int hashCode() {
            return instance.hashCode();
        }

        @Override
        public boolean equals(Object thatObj) {
            if (thatObj == this)
                return true;

            if (!(thatObj instanceof DehydratedIntegerConstant))
                return false;

            DehydratedIntegerConstant that = (DehydratedIntegerConstant) thatObj;
            if (that.___getHandle() != ___getHandle())
                return false;

            if (that.___getVersion() != ___getVersion())
                return false;

            if (that.instance.value != instance.value)
                return false;

            return true;
        }

        @Override
        public String toString() {
            return format("DehydratedIntegerConstant(handle=%s,version=%s, value=%s)",
                    ___getHandle(), ___getVersion(), instance.getValue());
        }
    }
}
