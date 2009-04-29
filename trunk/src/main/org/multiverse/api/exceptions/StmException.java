package org.multiverse.api.exceptions;

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
