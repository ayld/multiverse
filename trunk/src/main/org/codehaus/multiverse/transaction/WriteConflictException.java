package org.codehaus.multiverse.transaction;

import org.codehaus.multiverse.StmException;

/**
 * An {@link StmException} that is thrown when a write conflict is found.
 *
 * @author Peter Veentjer.
 */
public class WriteConflictException extends StmException{
    public WriteConflictException(){}

    public WriteConflictException(String msg) {
        super(msg);
    }
}
