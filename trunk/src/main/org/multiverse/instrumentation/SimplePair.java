package org.multiverse.instrumentation;

import org.multiverse.api.Dematerializable;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;

@Dematerializable
public class SimplePair {
    public int left;
    public int right;
    public SimplePair a;
    public SimplePair b;
    public SimplePair c;

    public Handle handle;

    Handle getHandle() {
        return handle;
    }

    public SimplePair() {
    }

    public SimplePair(Object foo, Transaction t) {

    }

    SimplePair foo(Transaction t) {
        return new SimplePair(this, t);
    }
}
