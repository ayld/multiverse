package org.multiverse.api.exceptions;

/**
 * A {@link StmException} that indicates that a write conflict has occurred.
 *
 * @author Peter Veentjer.
 */
public class WriteConflictException extends StmException {

    public WriteConflictException() {
    }

    public WriteConflictException(String msg) {
        super(msg);
    }
}
