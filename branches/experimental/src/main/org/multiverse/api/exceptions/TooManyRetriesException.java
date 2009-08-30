package org.multiverse.api.exceptions;

/**
 * A {@link StmException} that is thrown when a transaction is retried too many times. Uncontrolled
 * retrying could lead to liveness problems like livelocks and starvation.
 *
 * @author Peter Veentjer.
 */
public class TooManyRetriesException extends StmException {

    public TooManyRetriesException() {
    }

    public TooManyRetriesException(String message) {
        super(message);
    }

    public TooManyRetriesException(String message, Throwable cause) {
        super(message, cause);
    }

    public TooManyRetriesException(Throwable cause) {
        super(cause);
    }
}
