package org.multiverse.api.exceptions;

/**
 * A {@link RuntimeException} that indicates a failure while doing a {@link org.multiverse.api.Transaction#commit()}.
 *
 * @author Peter Veentjer
 */
public class CommitFailureException extends RuntimeException {

    public CommitFailureException() {
    }

    public CommitFailureException(String message) {
        super(message);
    }

    public CommitFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommitFailureException(Throwable cause) {
        super(cause);
    }
}
