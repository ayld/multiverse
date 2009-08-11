package org.multiverse.api.exceptions;

/**
 * An exception that can be thrown when a call to the {@link org.multiverse.api.Transaction#reset()}
 * failed.
 *
 * @author Peter Veentjer
 */
public class FailedToResetException extends StmException {

    public FailedToResetException() {
    }

    public FailedToResetException(Throwable cause) {
        super(cause);
    }

    public FailedToResetException(String message) {
        super(message);
    }

    public FailedToResetException(String message, Throwable cause) {
        super(message, cause);
    }
}
