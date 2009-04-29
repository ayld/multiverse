package org.multiverse.api.exceptions;

/**
 * A {@link StmException} that indicates ...
 *
 * @author Peter Veentjer.
 */
public class AbortedTransactionException extends StmException {

    public AbortedTransactionException() {
    }

    public AbortedTransactionException(String msg) {
        super(msg);
    }
}
