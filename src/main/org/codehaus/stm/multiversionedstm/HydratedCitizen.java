package org.codehaus.stm.multiversionedstm;


public interface HydratedCitizen {

    //int size();

    Citizen dehydrate(long ptr, MultiversionedStm.MultiversionedTransaction transaction);
}
