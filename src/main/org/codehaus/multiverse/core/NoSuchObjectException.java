package org.codehaus.multiverse.core;

import org.codehaus.multiverse.core.StmException;

import static java.lang.String.format;

/**
 * A {@link StmException} to indicate that an action is done on a non existing object.
 *
 * @author Peter Veentjer.
 */
public class NoSuchObjectException extends StmException {

    public NoSuchObjectException() {
    }

    public NoSuchObjectException(long handle, long version){
        super(format("Object with handle %d and version %d does not exist", handle, version));
    }

    public NoSuchObjectException(long handle) {
        super(format("Object with handle %d does not exist", handle));
    }

    public NoSuchObjectException(String s) {
        super(s);
    }
}
