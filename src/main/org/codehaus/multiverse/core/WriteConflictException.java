package org.codehaus.multiverse.core;

/**
 * An {@link StmException} that is thrown when a write conflict is found.
 * <p/>
 * todo: should this be an error?
 *
 * @author Peter Veentjer.
 */
public class WriteConflictException extends StmException {

    //todo: create instance to prevent stacktrace buildup

    public WriteConflictException() {
    }

    public WriteConflictException(String msg) {
        super(msg);
    }
}
