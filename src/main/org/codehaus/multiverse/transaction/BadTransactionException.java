package org.codehaus.multiverse.transaction;

import org.codehaus.multiverse.StmException;

import static java.lang.String.format;

/**
 * A {@link StmException} to indicate that a bad transaction is used. For example
 * the attach of an item to a transaction, that already is attached to another transaction.
 *
 * @author Peter Veentjer.
 */
public class BadTransactionException extends StmException{

    public static BadTransactionException createAttachedToDifferentTransaction(Object item){
        return new BadTransactionException(format("%s is attached to a different Transaction", item));
    }

    public static BadTransactionException createNoTransaction(Object item){
        return new BadTransactionException(format("%s is not attached to a Transaction", item));
    }

    public BadTransactionException(){}

    public BadTransactionException(String msg){
        super(msg);
    }
}
