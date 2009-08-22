package org.multiverse.api.exceptions;

/**
 * A {@link StmException} that indicates that an action is executed on a transaction
 * that is not active.
 * <p/>
 * todo: should the deadtransactionexception extend from the illegalstate exception instead
 * of the StmException
 *
 * @author Peter Veentjer.
 */
public class DeadTransactionException extends StmException {

    public DeadTransactionException() {
    }

    public DeadTransactionException(String message) {
        super(message);
    }

    public DeadTransactionException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeadTransactionException(Throwable cause) {
        super(cause);
    }
}
