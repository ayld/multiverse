package org.multiverse.api.exceptions;

/**
 * @author Peter Veentjer.
 */
public class StmException extends RuntimeException {

    public StmException() {
    }

    public StmException(String message) {
        super(message);
    }

    public StmException(String message, Throwable cause) {
        super(message, cause);
    }

    public StmException(Throwable cause) {
        super(cause);
    }
}
