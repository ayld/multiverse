package org.multiverse.api.exceptions;

import org.multiverse.stms.alpha.AlphaStmDebugConstants;

/**
 * An Error dat indicates that a retry should be done. 
 *
 * @author Peter Veentjer.
 */
public class RetryError extends Error {

    public final static RetryError INSTANCE = new RetryError();

    public static RetryError create() {
        if (AlphaStmDebugConstants.REUSE_RetryError) {
            return RetryError.INSTANCE;
        } else {
            return new RetryError();
        }
    }
}
