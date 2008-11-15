package org.codehaus.multiverse.transaction;

public class AttachedToDifferentTransactionException extends RuntimeException{

    public AttachedToDifferentTransactionException(String msg){
        super(msg);
    }
}
