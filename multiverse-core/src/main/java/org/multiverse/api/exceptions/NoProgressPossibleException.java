package org.multiverse.api.exceptions;

/**
 * An {@link IllegalStateException} that indicates that a retry is done, without the possibility of
 * progress, for example when the readset is empty.
 * <p/>
 * No reason to create a singleton for performance reasons since this exception should not
 * occur. So if it does, we want a complete stacktrace.
 *
 * @author Peter Veentjer.
 */
public class NoProgressPossibleException extends IllegalStateException {

    private static final long serialVersionUID = 0;

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

