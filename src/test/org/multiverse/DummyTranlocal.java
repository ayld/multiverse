package org.multiverse;

import org.multiverse.stms.alpha.DirtinessStatus;
import org.multiverse.stms.alpha.Tranlocal;
import org.multiverse.stms.alpha.TranlocalSnapshot;

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
