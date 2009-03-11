package org.codehaus.multiverse.api.exceptions;

/**
 * A {@link LivenessException} that indicates that a deadlock has occurred.
 *
 * @author Peter Veentjer.
 */
public class DeadlockException extends LivenessException {

    public DeadlockException() {
    }

    public DeadlockException(String msg) {
        super(msg);
    }
}
