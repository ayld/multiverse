package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.util.iterators.EmptyIterator;
import org.codehaus.multiverse.core.Transaction;

import java.util.Iterator;

public class DummyDehydratedStmObject extends DehydratedStmObject {

    public DummyDehydratedStmObject() {
    }

    public DummyDehydratedStmObject(long handle){
        super(handle);
    }

    public Iterator<Long> members() {
        return EmptyIterator.INSTANCE;
    }

    public StmObject hydrate(Transaction transaction) {
        throw new RuntimeException();
    }
}
