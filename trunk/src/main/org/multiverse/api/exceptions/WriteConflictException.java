package org.multiverse.api.exceptions;

import static java.lang.Boolean.parseBoolean;

/**
 * A {@link CommitFailureException} that indicates that a write conflict happened while
 * doing a {@link org.multiverse.api.Transaction#commit()}.
 *
 * @author Peter Veentjer.
 */
public class WriteConflictException extends CommitFailureException {

    public final static WriteConflictException INSTANCE = new WriteConflictException();

    private final static boolean reuse = parseBoolean(System.getProperty(WriteConflictException.class.getName(), "true"));

    public static WriteConflictException create() {
        if (reuse) {
            return WriteConflictException.INSTANCE;
        } else {
            return new WriteConflictException();
        }
    }
}
