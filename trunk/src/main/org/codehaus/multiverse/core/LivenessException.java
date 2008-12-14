package org.codehaus.multiverse.core;

/**
 * @author Peter Veentjer.
 */
public class LivenessException extends StmException {

    public LivenessException() {
    }

    public LivenessException(String message) {
        super(message);
    }

    public LivenessException(String message, Throwable cause) {
        super(message, cause);
    }

    public LivenessException(Throwable cause) {
        super(cause);
    }
}
