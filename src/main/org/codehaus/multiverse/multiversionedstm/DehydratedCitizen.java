package org.codehaus.multiverse.multiversionedstm;


public interface DehydratedCitizen {

    Citizen hydrate(long ptr, MultiversionedStm.MultiversionedTransaction transaction);
}
