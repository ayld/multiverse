package org.multiverse.api.exceptions;

public class NoCommittedDataFoundException extends StmException {

    public NoCommittedDataFoundException() {
    }

    public NoCommittedDataFoundException(String message) {
        super(message);
    }

    public NoCommittedDataFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoCommittedDataFoundException(Throwable cause) {
        super(cause);
    }
}
