package org.multiverse.api.exceptions;

import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getProperty;

/**
 * A {@link LoadException} that indicates that a load failed because the item was locked.
 *
 * @author Peter Veentjer.
 */
public class LoadLockedException extends LoadException {

    public final static LoadLockedException INSTANCE = new LoadLockedException();

    private final static boolean reuse = parseBoolean(getProperty(LoadLockedException.class.getName()+".reuse", "true"));

    public static LoadLockedException create() {
        if (reuse) {
            return LoadLockedException.INSTANCE;
        } else {
            return new LoadLockedException();
        }
    }
}
