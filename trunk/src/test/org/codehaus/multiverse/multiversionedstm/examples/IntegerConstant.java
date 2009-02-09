package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.multiversionedstm.DehydratedStmObject;
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
        return format("IntegerConstant(value=%s)", value);
    }

    // ==================== generated ================

    private final long handle;

    public long ___getHandle() {
        return handle;
    }

    public DehydratedStmObject ___dehydrate() {
        return new DehydratedIntegerConstant(this);
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

    public static class DehydratedIntegerConstant extends DehydratedStmObject {
        private final IntegerConstant instance;

        DehydratedIntegerConstant(IntegerConstant instance) {
            super(instance.___getHandle());
            this.instance = instance;
        }

        @Override
        public Iterator<Long> members() {
            return EmptyIterator.INSTANCE;
        }

        @Override
        public StmObject hydrate(Transaction transaction) {
            return instance;
        }
    }
}
