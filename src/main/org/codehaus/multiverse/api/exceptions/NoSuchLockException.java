package org.codehaus.multiverse.api.exceptions;

/**
 * A StmException that is thrown when a Lock is looked up for an stm object that doesn't exist.
 *
 * @author Peter Veentjer
 */
public class NoSuchLockException extends StmException {

    public NoSuchLockException(String msg) {
        super(msg);
    }
}
