package org.multiverse.api.exceptions;

/**
 * A {@link RuntimeException} that can be thrown when a call to the
 * {@link org.multiverse.api.Transaction#restart()}
 * failed.
 *
 * @author Peter Veentjer
 */
public class ResetFailureException extends RuntimeException {

    private static final long serialVersionUID = 0;

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
