package org.multiverse.api;

import org.multiverse.multiversionedstm.MultiversionedStm;

/**
 * @author Peter Veentjer.
 */
public final class GlobalStmInstance {

    private static volatile Stm instance = new MultiversionedStm();

    public static Stm getInstance() {
        return instance;
    }

    public static void setInstance(Stm newInstance) {
        if (newInstance == null) {
            throw new NullPointerException();
        }
        instance = newInstance;
    }

    //we don't want instances
    private GlobalStmInstance() {
    }
}
