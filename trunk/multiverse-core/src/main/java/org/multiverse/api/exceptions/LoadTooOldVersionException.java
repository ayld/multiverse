package org.multiverse.api.exceptions;

import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getProperty;

/**
 * A {@link LoadException} that indicates that a load was done, but the version needed could not
 * be found because it is too old (and doesn't exist anymore).
 *
 * In the future version history will be added so that multiple version could be alive at
 * any given moment.
 *
 * @author Peter Veentjer.
 */
public class LoadTooOldVersionException extends LoadException {

    public final static LoadTooOldVersionException INSTANCE = new LoadTooOldVersionException();

    private final static boolean reuse = parseBoolean(getProperty(LoadTooOldVersionException.class.getName()+".reuse", "false"));

    public static LoadTooOldVersionException create(String msg) {
        if (reuse) {
            return LoadTooOldVersionException.INSTANCE;
        } else {
            return new LoadTooOldVersionException(msg);
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