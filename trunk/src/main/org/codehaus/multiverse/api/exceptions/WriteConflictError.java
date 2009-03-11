package org.codehaus.multiverse.api.exceptions;

/**
 * An Error that is thrown when a write conflict occurrs. This is an error because it should not
 * be caught, it is created to regulate control flow. Normally this would be a bad thing, but
 * in this case it is acceptable because java doesn't provide any other ways to break from
 * control flow apart from returning from methods.
 * <p/>
 * The WriteConflictError in essence is an optimistic locking failure.
 *
 * @author Peter Veentjer.
 * @see org.codehaus.multiverse.api.exceptions.RetryError
 */
public class WriteConflictError extends Error {

    public static WriteConflictError INSTANCE = new WriteConflictError();

    public WriteConflictError() {
    }

    public WriteConflictError(String msg) {
        super(msg);
    }
}
