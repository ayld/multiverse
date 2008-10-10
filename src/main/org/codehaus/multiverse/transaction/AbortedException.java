package org.codehaus.multiverse.transaction;

public class AbortedException extends RuntimeException{

    public AbortedException(){}

    public AbortedException(String msg){
        super(msg);
    }
}
