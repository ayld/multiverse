package org.codehaus.stm.multiversionedstm;


public interface DehydratedCitizen {

    Citizen hydrate(long ptr, MultiversionedStm.MultiversionedTransaction transaction);
}
