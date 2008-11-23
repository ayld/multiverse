package org.codehaus.multiverse.transaction;

import org.codehaus.multiverse.StmException;

import static java.lang.String.format;

/**
 * A {@link StmException} to indicate that an action is done on a non existing object.
 *
 * @author Peter Veentjer.
 */
public class NoSuchObjectException extends StmException {

    public NoSuchObjectException() {
    }

    public NoSuchObjectException(long handle) {
        super(format("Object with handle %d does not exist", handle));
    }

    public NoSuchObjectException(String s) {
        super(s);
    }
}
