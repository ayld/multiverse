package org.multiverse.api.exceptions;

import org.multiverse.stms.alpha.AlphaStmDebugConstants;

/**
 * A {@link LoadException} that indicates that a load failed because the item was locked.
 *
 * @author Peter Veentjer.
 */
public class LoadLockedException extends LoadException {

    public final static LoadLockedException INSTANCE = new LoadLockedException();

    public static LoadLockedException create() {
        if (AlphaStmDebugConstants.REUSE_LockedException) {
            return LoadLockedException.INSTANCE;
        } else {
            return new LoadLockedException();
        }
    }
}
