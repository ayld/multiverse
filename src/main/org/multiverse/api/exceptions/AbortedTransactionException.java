package org.multiverse.api.exceptions;

public class AbortedTransactionException extends StmException {

    public AbortedTransactionException() {
    }

    public AbortedTransactionException(String msg) {
        super(msg);
    }
}
