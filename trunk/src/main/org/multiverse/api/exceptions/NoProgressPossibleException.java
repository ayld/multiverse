package org.multiverse.api.exceptions;

/**
 * A {@link StmException} that indicates that a retry is done, without the possibility of
 * progress.
 * <p/>
 * No reason to create a singleton for performance reasons since this exception should not
 * occur. So it it does, we want a complete stacktrace.
 *
 * @author Peter Veentjer.
 */
public class NoProgressPossibleException extends StmException {

    public NoProgressPossibleException() {
    }

    public NoProgressPossibleException(String message) {
        super(message);
    }

    public NoProgressPossibleException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoProgressPossibleException(Throwable cause) {
        super(cause);
    }
}

