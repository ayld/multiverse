package org.codehaus.multiverse.transaction;

import org.codehaus.multiverse.StmException;

import static java.lang.String.format;

/**
 * A {@link StmException} to indicate that an action is done on a non existing object.
 *
 * @author Peter Veentjer.
 */
public class ObjectDoesNotExistException extends StmException {

    public ObjectDoesNotExistException() {
    }

    public ObjectDoesNotExistException(long handle) {
        super(format("Cell with handle %d does not exist", handle));
    }

    public ObjectDoesNotExistException(String s) {
        super(s);
    }
}
