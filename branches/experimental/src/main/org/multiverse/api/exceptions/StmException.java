package org.multiverse.api.exceptions;

/**
 * A {@link RuntimeException} that can be thrown when using STM's and transactions.
 *
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
