package org.multiverse.multiversionedstm;

import org.multiverse.api.Transaction;
import org.multiverse.instrumentation.SimplePair;

public class DummyDematerializedObject implements DematerializedObject {

    public DummyDematerializedObject(SimplePair simplePair, Transaction t) {

    }

    @Override
    public MultiversionedHandle getHandle() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public MaterializedObject rematerialize(Transaction t) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
