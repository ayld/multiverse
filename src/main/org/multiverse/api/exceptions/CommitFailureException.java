package org.multiverse.api.exceptions;

/**
 * A {@link StmException} that indicates a failure while doing a
 * {@link org.multiverse.api.Transaction#commit()}.
 *
 * @author Peter Veentjer
 */
public class CommitFailureException extends StmException {

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
