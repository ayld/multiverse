package org.multiverse.api.exceptions;

/**
 * A {@link LoadException} that is thrown when an object is loaded but has not been committed yet.
 * There are 2 reasons this can happen:
 * <ol>
 * <li>
 * an AtomicObject has escaped and is used by another transaction, before the transaction creating
 * that object has committed.
 * </li>
 * <li>
 * an AtomicObject is used after the transaction in which the atomicobject is created, is aborted.
 * </li>
 * </ol>
 * In either case, the cause is a misuse of the STM.
 * <p/>
 * There is no reason for this LoadException to be reused is because this exception indicates a
 * programming failure, and you want to have good feedback when that happens.
 *
 * @author Peter Veentjer.
 */
public class LoadUncommittedException extends LoadException {

    public LoadUncommittedException() {
    }

    public LoadUncommittedException(String message) {
        super(message);
    }

    public LoadUncommittedException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoadUncommittedException(Throwable cause) {
        super(cause);
    }
}
