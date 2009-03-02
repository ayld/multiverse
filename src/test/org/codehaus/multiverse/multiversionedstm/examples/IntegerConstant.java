package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.multiversionedheap.AbstractDeflated;
import org.codehaus.multiverse.multiversionedstm.HandleGenerator;
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

    public String toString() {
        return format("IntegerConstant(get=%s)", value);
    }

    // ==================== generated ================

    private final long handle;

    public long ___getHandle() {
        return handle;
    }

    public DehydratedIntegerConstant ___deflate(long commitVersion) {
        return new DehydratedIntegerConstant(this, commitVersion);
    }

    public Iterator<StmObject> ___getFreshOrLoadedStmMembers() {
        return EmptyIterator.INSTANCE;
    }

    public void ___onAttach(Transaction transaction) {
        throw new RuntimeException();
    }

    public Transaction ___getTransaction() {
        return null;
    }

    public boolean ___isDirty() {
        return false;
    }

    public boolean ___isImmutable() {
        return true;
    }

    private StmObject next;

    public void setNext(StmObject next) {
        this.next = next;
    }

    public StmObject getNext() {
        return next;
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
    }
}
