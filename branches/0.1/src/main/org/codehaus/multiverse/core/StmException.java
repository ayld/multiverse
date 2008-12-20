package org.codehaus.multiverse.core;

/**
 * A {@link RuntimeException} where subclasses are thrown by stm if STM related problems occur. Usefull if you want
 * to create a general purpose error handler for these exception.
 *
 * @author Peter Veentjer.
 */
public abstract class StmException extends RuntimeException {

    public StmException() {
    }

    public StmException(String message) {
        super(message);
    }

    public StmException(String message, Throwable cause) {
        super(message, cause);
    }

    public StmException(Throwable cause) {
        super(cause);
    }
}
