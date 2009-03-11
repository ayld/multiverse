package org.codehaus.multiverse.api.exceptions;

/**
 * A {@link LivenessException} that is thrown when a livelock is encountered. A livelock could be detected
 * for example when a retry limit is reached.
 *
 * @author Peter Veentjer.
 */
public class LivelockException extends LivenessException {

    public LivelockException() {
    }

    public LivelockException(String msg) {
        super(msg);
    }
}
