package org.multiverse.api.exceptions;

import static java.lang.Boolean.parseBoolean;

/**
 * A {@link LoadException} that indicates that a load failed because the item was locked.
 *
 * @author Peter Veentjer.
 */
public class LoadLockedException extends LoadException {

    public final static LoadLockedException INSTANCE = new LoadLockedException();

    private final static boolean reuse = parseBoolean(System.getProperty("reuse." + LoadLockedException.class.getName(), "true"));

    public static LoadLockedException create() {
        if (reuse) {
            return LoadLockedException.INSTANCE;
        } else {
            return new LoadLockedException();
        }
    }
}
