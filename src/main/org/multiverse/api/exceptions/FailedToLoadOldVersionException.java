package org.multiverse.api.exceptions;

import org.multiverse.stms.alpha.AlphaStmDebugConstants;

/**
 * A {@link LoadException} that indicates that the STM was not able to find an older version.
 *
 * @author Peter Veentjer.
 */
public class FailedToLoadOldVersionException extends LoadException {

    public final static FailedToLoadOldVersionException INSTANCE = new FailedToLoadOldVersionException();

    public static FailedToLoadOldVersionException create() {
        if (AlphaStmDebugConstants.REUSE_SnapshotTooOldException) {
            return FailedToLoadOldVersionException.INSTANCE;
        } else {
            return new FailedToLoadOldVersionException();
        }
    }
}
