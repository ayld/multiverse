package org.codehaus.multiverse.transaction;

/**
 * A {@link RuntimeException} to indicate that a {@link Transaction#commit} can't be executed, and therefor
 * the transaction is aborted.
 *
 * @author Peter Veentjer.
 */
public class AbortedException extends RuntimeException{

    public AbortedException(){}

    public AbortedException(String msg){
        super(msg);
    }

    public AbortedException(String msg, Exception cause){
        super(msg, cause);
    }
}
