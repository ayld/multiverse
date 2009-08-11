package org.multiverse.api.exceptions;

/**
 * A {@link StmException} that indicates that the load of a {@link org.multiverse.stms.alpha.Tranlocal} failed.
 *
 * @author Peter Veentjer.
 */
public class LoadException extends StmException {

    public LoadException() {
    }

    public LoadException(String message) {
        super(message);
    }

    public LoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoadException(Throwable cause) {
        super(cause);
    }
}
