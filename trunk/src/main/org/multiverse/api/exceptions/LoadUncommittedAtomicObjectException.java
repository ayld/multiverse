package org.multiverse.api.exceptions;

/**
 * A {@link LoadException} that can be thrown when an object is loaded but has not been
 * committed yet. There 2 reasons this can happen:
 * <ol>
 * <li>
 * An AtomicObject has escaped and used by another transaction, before the transaction creating that object
 * has committed.
 * </li>
 * <li>
 * An AtomicObject is used after the transaction in which the object is created, is aborted.
 * </li>
 * </ol>
 * There is no reason for this exception to be reused is because this exception indicates a
 * programming failure, and you want to have good feedback when that happens.
 *
 * @author Peter Veentjer.
 */
public class LoadUncommittedAtomicObjectException extends LoadException {

    public LoadUncommittedAtomicObjectException() {
    }

    public LoadUncommittedAtomicObjectException(String message) {
        super(message);
    }

    public LoadUncommittedAtomicObjectException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoadUncommittedAtomicObjectException(Throwable cause) {
        super(cause);
    }
}
