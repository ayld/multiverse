package org.multiverse.stms.gamma;

import org.multiverse.api.Transaction;

public interface GammaTransaction extends Transaction {

    String getFamilyName();

    GammaTranlocal privatize(GammaAtomicObject atomicObject);

    void attachAsNew(GammaTranlocal tranlocal);
}
