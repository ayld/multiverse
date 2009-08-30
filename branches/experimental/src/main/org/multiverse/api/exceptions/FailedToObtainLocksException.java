package org.multiverse.api.exceptions;

import org.multiverse.stms.alpha.AlphaStmDebugConstants;

/**
 * A {@link org.multiverse.api.exceptions.CommitFailureException} that indicates that
 * the locks could not be acquired while doing a {@link org.multiverse.api.Transaction#commit}.
 *
 * @author Peter Veentjer
 */
public class FailedToObtainLocksException extends CommitFailureException {

    public final static FailedToObtainLocksException INSTANCE = new FailedToObtainLocksException();

    public static FailedToObtainLocksException create() {
        if (AlphaStmDebugConstants.REUSE_FailedToObtainLocksException) {
            return FailedToObtainLocksException.INSTANCE;
        } else {
            return new FailedToObtainLocksException();
        }
    }

    public FailedToObtainLocksException() {
    }

    public FailedToObtainLocksException(String message) {
        super(message);
    }

    public FailedToObtainLocksException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailedToObtainLocksException(Throwable cause) {
        super(cause);
    }
}
