package org.multiverse.api.exceptions;

/**
 *
 */
public class TooManyRetriesException extends StmException {

    public TooManyRetriesException() {
    }

    public TooManyRetriesException(String msg) {
        super(msg);
    }
}
