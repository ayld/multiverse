package org.multiverse.api.exceptions;

import static java.lang.Boolean.parseBoolean;

/**
 * A {@link LoadException} that indicates that a load was done, but the version needed could not
 * be found because it is too old (and doesn't exist anymore).
 *
 * @author Peter Veentjer.
 */
public class LoadTooOldVersionException extends LoadException {

    public final static LoadTooOldVersionException INSTANCE = new LoadTooOldVersionException();

    private final static boolean reuse = parseBoolean(System.getProperty("reuse." + LoadTooOldVersionException.class.getName(), "true"));

    public static LoadTooOldVersionException create() {
        if (reuse) {
            return LoadTooOldVersionException.INSTANCE;
        } else {
            return new LoadTooOldVersionException();
        }
    }

    public LoadTooOldVersionException() {
    }

    public LoadTooOldVersionException(String message) {
        super(message);
    }

    public LoadTooOldVersionException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoadTooOldVersionException(Throwable cause) {
        super(cause);
    }
}
