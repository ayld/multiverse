package org.multiverse.api.exceptions;

import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getProperty;

/**
 * A {@link CommitFailureException} that indicates that a write conflict happened while
 * doing a {@link org.multiverse.api.Transaction#commit()}.
 *
 * @author Peter Veentjer.
 */
public class WriteConflictException extends CommitFailureException {

    public final static WriteConflictException INSTANCE = new WriteConflictException();

    private final static boolean reuse = parseBoolean(getProperty(WriteConflictException.class.getName()+".reuse", "true"));

    public static WriteConflictException create() {
        if (reuse) {
            return WriteConflictException.INSTANCE;
        } else {
            return new WriteConflictException();
        }
    }
}
