package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.util.iterators.EmptyIterator;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

public class DummyDehydratedStmObject extends DehydratedStmObject {

    public final static AtomicLong counter = new AtomicLong();

    public DummyDehydratedStmObject() {
        super(counter.incrementAndGet());
    }

    public DummyDehydratedStmObject(long handle) {
        super(handle);
    }

    public Iterator<Long> members() {
        return EmptyIterator.INSTANCE;
    }

    public StmObject hydrate(Transaction transaction) {
        throw new RuntimeException();
    }
}
