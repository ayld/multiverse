package org.codehaus.multiverse.api.exceptions;

/**
 * A {@link StmException} that can be thrown when a liveness problem occurs like a livelock or a deadlock.
 *
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
