package org.multiverse.api.exceptions;

/**
 * A {@link StmException} that indicates that a write conflict has occurred.
 *
 * @author Peter Veentjer.
 */
public class WriteConflictException extends StmException {

    public final static WriteConflictException INSTANCE = new WriteConflictException();

    public WriteConflictException() {
    }

    public WriteConflictException(String msg) {
        super(msg);
    }
}
