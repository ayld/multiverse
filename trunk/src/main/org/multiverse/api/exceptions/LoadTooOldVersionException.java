package org.multiverse.api.exceptions;

import org.multiverse.stms.alpha.AlphaStmDebugConstants;

/**
 * A {@link LoadException} that indicates that a load was done, but the version needed could not
 * be found because it is too old (and doesn't exist anymore).
 *
 * @author Peter Veentjer.
 */
public class LoadTooOldVersionException extends LoadException {

    public final static LoadTooOldVersionException INSTANCE = new LoadTooOldVersionException();

    public static LoadTooOldVersionException create() {
        if (AlphaStmDebugConstants.REUSE_LoadTooOldVersionException) {
            return LoadTooOldVersionException.INSTANCE;
        } else {
            return new LoadTooOldVersionException();
        }
    }

    public LoadTooOldVersionException() {
    }

    public LoadTooOldVersionException(String message) {
        super(message);
    }

    public LoadTooOldVersionException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoadTooOldVersionException(Throwable cause) {
        super(cause);
    }
}
