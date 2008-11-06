package org.codehaus.multiverse.multiversionedstm2;

public abstract class DehydratedCitizen {

    private long pointer;

    public abstract Citizen hydrate();

    public long getPointer(){
        return pointer;
    }
}
