package org.multiverse.stms.gamma;

import org.multiverse.utils.spinning.SpinPolicy;

public interface GammaAtomicObject {

    GammaTranlocal privatize(long version, SpinPolicy spinPolicy);

    GammaTranlocal load(long version, SpinPolicy spinPolicy);

    /**
     * Loads the most recent written value.
     *
     * @return
     */
    GammaTranlocal load();

    boolean tryLock(GammaTransaction t);

    void releaseLock(GammaTransaction t);

    void storeAndReleaseLock(GammaTranlocal tranlocal);
}
