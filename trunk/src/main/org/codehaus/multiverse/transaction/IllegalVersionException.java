package org.codehaus.multiverse.transaction;

import org.codehaus.multiverse.StmException;

import static java.lang.String.format;

/**
 * A {@link StmException} to indicate that an illegal version for the stm is used.
 *
 * @author Peter Veentjer.
 */
public class IllegalVersionException extends StmException {

    public IllegalVersionException() {
    }

    public IllegalVersionException(long version) {
        super(format("Illegal version %s", version));
    }

    public IllegalVersionException(String msg) {
        super(msg);
    }
}
