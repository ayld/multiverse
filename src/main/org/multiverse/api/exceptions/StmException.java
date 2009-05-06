package org.multiverse.api.exceptions;

/**
 * An superclass for all RuntimeExceptions specific to the {@link Stm}.
 *
 * @author Peter Veentjer.
 */
public abstract class StmException extends RuntimeException {

    public StmException() {
        super();
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
