package org.multiverse;

import org.multiverse.api.DirtinessStatus;
import org.multiverse.api.Tranlocal;
import org.multiverse.api.TranlocalSnapshot;

public class DummyTranlocal extends Tranlocal {

    @Override
    public TranlocalSnapshot takeSnapshot() {
        throw new RuntimeException();
    }

    @Override
    public DirtinessStatus getDirtinessStatus() {
        throw new RuntimeException();
    }

    @Override
    public void prepareForCommit(long writeVersion) {
        throw new RuntimeException();
    }

    @Override
    public Object getAtomicObject() {
        throw new RuntimeException();
    }
}
