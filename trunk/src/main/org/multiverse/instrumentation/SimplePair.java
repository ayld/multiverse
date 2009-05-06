package org.multiverse.instrumentation;

import org.multiverse.api.Dematerializable;
import org.multiverse.api.Handle;

@Dematerializable
public class SimplePair {
    public int left;
    public int right;
    public SimplePair a;
    public SimplePair b;
    public SimplePair c;

    public SimplePair() {
    }

    private class MyDematerializedPair {
        int left;
        int right;
        Handle a;
        Handle b;
        Handle c;
    }
}
