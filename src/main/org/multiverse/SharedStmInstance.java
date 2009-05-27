package org.multiverse;

import org.multiverse.api.Stm;
import org.multiverse.multiversionedstm.MultiversionedStm;

/**
 * @author Peter Veentjer.
 */
public final class SharedStmInstance {

    private static volatile Stm instance = new MultiversionedStm();

    public static Stm getInstance() {
        return instance;
    }

    public static void setInstance(Stm instance) {
        SharedStmInstance.instance = instance;
    }

    //we don't want instances
    private SharedStmInstance() {
    }
}
