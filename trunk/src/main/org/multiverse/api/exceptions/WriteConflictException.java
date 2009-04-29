package org.multiverse.api.exceptions;

public class WriteConflictException extends StmException {

    public WriteConflictException() {
    }

    public WriteConflictException(String msg) {
        super(msg);
    }
}
