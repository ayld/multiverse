package org.codehaus.stm.transaction;

public class AbortedException extends RuntimeException{

    public AbortedException(){}

    public AbortedException(String msg){
        super(msg);
    }
}
