package org.multiverse.api.exceptions;

/**
 * An exception that can be thrown when a call to the {@link org.multiverse.api.Transaction#reset()}
 * failed.
 *
 * @author Peter Veentjer
 */
public class ResetFailureException extends StmException {

    public ResetFailureException() {
    }

    public ResetFailureException(Throwable cause) {
        super(cause);
    }

    public ResetFailureException(String message) {
        super(message);
    }

    public ResetFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
