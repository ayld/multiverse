package org.multiverse.utils;

import org.multiverse.api.Stm;
import org.multiverse.stms.alpha.AlphaStm;

/**
 * A singleton that can be used for easy access to the {@link org.multiverse.api.Stm} that is used globally.
 * Once it has been set, it should not be changed while running the system.
 * <p/>
 * Using the GlobalStm imposes some limitations (like 1 global stm instance that is used by everything) but makes
 * the system a lot easier to use. But if the GlobalStm should not be used, but a 'private' stm, you need to carry
 * around the stm reference yourself and just ignore this GlobalStm.
 * <p/>
 * Atm it always uses an {@link AlphaStm} by default. In the future this choice should be made configurable.
 *
 * @author Peter Veentjer
 */
public final class GlobalStmInstance {

    private static volatile Stm instance = new AlphaStm();

    /**
     * Gets the global {@link Stm} instance. The returned value will never be null.
     *
     * @return the global STM instance.
     */
    public static Stm get() {
        return instance;
    }

    /**
     * Sets the global Stm instance.
     *
     * @param newInstance the instance to set.
     * @throws NullPointerException if newInstance is null. No need to allow for an illegal reference.
     */
    public static void set(Stm newInstance) {
        if (newInstance == null) {
            throw new NullPointerException();
        }
        instance = newInstance;
    }

    //we don't want instances.
    private GlobalStmInstance() {
    }
}
