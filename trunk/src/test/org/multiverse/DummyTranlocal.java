package org.multiverse;

import org.multiverse.stms.alpha.AlphaAtomicObject;
import org.multiverse.stms.alpha.AlphaTranlocal;
import org.multiverse.stms.alpha.AlphaTranlocalSnapshot;
import org.multiverse.stms.alpha.DirtinessStatus;

public class DummyTranlocal extends AlphaTranlocal {

    @Override
    public AlphaTranlocalSnapshot takeSnapshot() {
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
    public AlphaAtomicObject getAtomicObject() {
        throw new RuntimeException();
    }
}
