package org.multiverse.stms.gamma;

import org.multiverse.stms.alpha.DirtinessStatus;
import org.multiverse.utils.commitlock.CommitLock;


/**
 * To prevent creating an additional object, the GammaTranlocal also extends the {@link CommitLock}.
 * so that it can be used with the atomicobjectlock functionality.
 */
public interface GammaTranlocal<A extends GammaAtomicObject> extends CommitLock {

    A getAtomicObject();

    DirtinessStatus getDirtinessStatus();

    long getVersion();

    void storeAndReleaseLock(long commitVersion);
}
