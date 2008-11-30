package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.util.iterators.EmptyIterator;
import org.codehaus.multiverse.transaction.Transaction;

import java.util.Iterator;

public class DummyDehydratedStmObject extends DehydratedStmObject {

    public DummyDehydratedStmObject() {
    }

    public DummyDehydratedStmObject(long handle, long version) {
        super(handle, version);
    }

    public Iterator<Long> getDirect() {
        return EmptyIterator.INSTANCE;
    }

    public StmObject hydrate(Transaction transaction) {
        throw new RuntimeException();
    }
}
