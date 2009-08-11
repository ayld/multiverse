package org.multiverse.api.exceptions;

import org.multiverse.stms.alpha.AlphaStmDebugConstants;

/**
 * A {@link LoadException} that indicates that a {@link org.multiverse.stms.alpha.Tranlocal} could not be loaded
 * because it was locked.
 *
 * @author Peter Veentjer.
 */
public class LockedException extends LoadException {

    public final static LockedException INSTANCE = new LockedException();

    public static LockedException create() {
        if (AlphaStmDebugConstants.REUSE_LockedException) {
            return LockedException.INSTANCE;
        } else {
            return new LockedException();
        }
    }
}
