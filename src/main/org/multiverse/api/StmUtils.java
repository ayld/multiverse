package org.multiverse.api;

import org.multiverse.api.exceptions.RetryError;

/**
 * Contains utility methods for the {@link Stm}.
 *
 * @author Peter Veentjer.
 */
public final class StmUtils {

    //we don't want instances.
    private StmUtils() {
    }

    public static void retry() {
        throw new RetryError();
    }
}