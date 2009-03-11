package org.codehaus.multiverse.api.exceptions;

import static java.lang.String.format;

/**
 * A {@link StmException} to indicate that a bad version for the stm is used. Example: if an version of an
 * object is deleted, even though that version doesn't exist.
 *
 * @author Peter Veentjer.
 */
public class BadVersionException extends StmException {

    public BadVersionException() {
    }

    public BadVersionException(long version) {
        super(format("Bad version %s", version));
    }

    public BadVersionException(String msg) {
        super(msg);
    }
}
